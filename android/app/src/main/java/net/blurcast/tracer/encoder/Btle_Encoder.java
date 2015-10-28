package net.blurcast.tracer.encoder;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import net.blurcast.android.util.ByteBuilder;
import net.blurcast.android.util.Bytes;
import net.blurcast.android.util.Encoder;
import net.blurcast.tracer.app.Geotracer;
import net.blurcast.tracer.callback.Attempt;
import net.blurcast.tracer.callback.EventDetails;
import net.blurcast.tracer.callback.IpcSubscriber;
import net.blurcast.tracer.callback.Subscriber;
import net.blurcast.tracer.driver.Btle_Driver;
import net.blurcast.tracer.helper.ParcelableBluetoothDeviceInfo;
import net.blurcast.tracer.logger._Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by blake on 12/28/14.
 */
public class Btle_Encoder extends _Encoder<Bundle> {

    private static final String TAG = Btle_Encoder.class.getSimpleName();
    private static final int MAX_AD_LENGTH = 30 - 19;

    private Btle_Driver mBluetooth;
    private Subscriber<Bundle> mScanResultSubscriber;
    private IpcSubscriber<Bundle> mCuriousSubscriber;
    private EventDetails oEventDetails;
    private Attempt mStopAttempt;

    public Btle_Encoder(Context context, _Logger logger) {
        super(logger);
        this.init(context);
    }

    private int cDeviceId = 0;
    private HashMap<String, Integer> mDeviceHash = new HashMap<String, Integer>();
    private ArrayList<ParcelableBluetoothDeviceInfo> mDeviceInfo = new ArrayList<ParcelableBluetoothDeviceInfo>();


    private void init(Context context) {
        mBluetooth = Btle_Driver.getInstance(context);

        mScanResultSubscriber = new Subscriber<Bundle>() {
            @Override
            public void event(Bundle data, EventDetails eventDetails) {

                // fetch bluetooth device
                BluetoothDevice device = (BluetoothDevice) data.get(Btle_Driver.DATA_DEVICE);

                // reference its rssi value
                int rssi = data.getInt(Btle_Driver.DATA_RSSI);

                // for identifying this device local to log file
                int deviceId;

                //inspect advertisement frame
                byte[] ad = data.getByteArray(Btle_Driver.DATA_SCAN_RECORD);

                // this is a Gimbal beacon transmitting in proprietary mode
                if(((int) ad[2] & 0xff) == 0xad
                        && ((int) ad[3] & 0xff) == 0x77
                        && ((int) ad[4] & 0xff) == 0x00
                        && ((int) ad[5] & 0xff) == 0xc6) {

                    // prepare to construct string identifier
                    byte[] gimbalId = Arrays.copyOfRange(ad, 22, 31);

                    // we've identified this beacon!
                    String beaconId = new String(gimbalId);

                    // lookup
                    Integer iBeaconId = mDeviceHash.get(beaconId);

                    // no such device yet
                    if(iBeaconId == null) {

                        // add device and get it's new id
                        deviceId = addBtleDevice(beaconId, gimbalId, device, ParcelableBluetoothDeviceInfo.BLUETOOTH_TYPE_GIMBAL);

                        Log.d(TAG, "** New Gimbal Beacon ** "+beaconId);
                    }
                    // device encountered before
                    else {
                        deviceId = iBeaconId;
                    }

//                    Log.d(TAG+"_di", "["+rssi+"] "+beaconId+" => "+bytesToHexStr(ad));
                }

                // iBeacon or standard bluetooth device
                else {

                    // start off assuming type other
                    int btleType = ParcelableBluetoothDeviceInfo.BLUETOOTH_TYPE_OTHER;

                    // prepare string for storing hash to distinguish this device
                    String sHash;

                    // sequence of bytes that identifies this device uniquely
                    byte[] beaconAd;

                    // iBeacon::  02 01 06 1A FF 4C 00 02 15
                    if(ad[0] == (byte) 0x02
                            && ad[1] == (byte) 0x01
                            && ad[2] == (byte) 0x06
                            && ad[3] == (byte) 0x1a
                            && ad[4] == (byte) 0xff
                            && ad[5] == (byte) 0x4c
                            && ad[6] == (byte) 0x00
                            && ad[7] == (byte) 0x02
                            && ad[8] == (byte) 0x15) {

                        // iBeacon!
                        btleType = ParcelableBluetoothDeviceInfo.BLUETOOTH_TYPE_IBEACON;

//                        // proximity
//                        byte[] proximityUid = Arrays.copyOfRange(ad, 9, 25);
//
//                        // major
//                        int major = (ad[25] << 8) | ad[26];
//                        int minor = (ad[27] << 8) | ad[28];
//
//                        // tx power
//                        int txPower = (int) ad[29];
//
//                        Log.w(TAG, "major: "+major+", minor: "+minor+"; txPower: "+txPower+"; proximity uid: "+bytesToHexStr(proximityUid)+"{"+proximityUid.length+"}");

                        //
                        beaconAd = Arrays.copyOfRange(ad, 9, 30);

                        // hash the proximity uid + major + minor
                        sHash = new String(Arrays.copyOfRange(ad, 9, 29), CHARSET_ISO_8859_1);

                    }
                    // other bluetooth low energy device
                    else {

                        // hash hardware address
                        sHash = device.getAddress();

                        // set beacon ad to hardware address bytes decoded
                        beaconAd = encode_hardware_address(sHash);
                    }

                    // lookup this device by hash
                    Integer iDeviceId = mDeviceHash.get(sHash);

                    // no such device yet
                    if (iDeviceId == null) {

                        // add device and get it's new id
                        deviceId = addBtleDevice(sHash, beaconAd, device, btleType);

                        Log.d(TAG, "** New Bluetooth device ** "+sHash+": "+device.getType());
                        Log.d(TAG, "["+deviceId+"] =======> "+ Bytes.toHexString(ad));
                    }
                    // device encountered before
                    else {
                        deviceId = iDeviceId;
//                        Log.d(TAG, deviceId + " ["+sHash+"]:: " + rssi+"\n\t"+bytesToHexStr(ad));
                    }
                }

                // prepare to build the bytes for encoding this
                ByteBuilder bytes = new ByteBuilder(1+2+3+1);

                // block type: 1 byte
                bytes.append(_Logger.TYPE_BTLE_EVENT);

                // device id: 2 bytes
                bytes.append_2(
                        Encoder.encode_char(deviceId)
                );

                // time (1000Hz resolution) since start: 3 bytes (279.6 minutes run-time)
                bytes.append_3(
                        mLogger.encodeOffsetTime(eventDetails.getStartTime())
                );

                // rssi: 1 byte
                bytes.append(
                        Encoder.encode_byte(rssi)
                );

                // submit bytes
                mLogger.submit(bytes.getBytes());


                // there is a curious subscriber
                if(mCuriousSubscriber != null) {

                    // set curious specific data
                    data.putInt(Geotracer.DATA_ENTRY_ID, deviceId);
                    data.putParcelable(Geotracer.DATA_ENTRY_INFO, mDeviceInfo.get(deviceId));

                    // forward event to curious subscriber
                    mCuriousSubscriber.event(data, oEventDetails);
                }

            }
        };
    }

    private int addBtleDevice(String sBeaconId, byte[] beaconId, BluetoothDevice device, int deviceType) {

        // assign new device id
        int deviceId = cDeviceId++;

        // add unique identifier to hash
        mDeviceHash.put(sBeaconId, deviceId);

        // create device info
        ParcelableBluetoothDeviceInfo deviceInfo = new ParcelableBluetoothDeviceInfo(device, deviceType, sBeaconId, beaconId);

        // store this device object
        mDeviceInfo.add(deviceInfo);

        // emit notice to curious subscriber
        if(mCuriousSubscriber != null) {
            mCuriousSubscriber.notice(Geotracer.NOTICE_NEW_ENTRY, deviceId, deviceInfo);
        }

        // return new device id
        return deviceId;
    }


    private void close() {

        ByteBuilder bytes = new ByteBuilder(1+2+ mDeviceHash.size()*(2+1+1+MAX_AD_LENGTH+32));

        // keep track of how many bytes we use
        int cBytes = 1+2;

        // info block: 1 byte
        bytes.append(_Logger.TYPE_BTLE_INFO);

        // number of entries: 2 bytes
        bytes.append(
                Encoder.encode_char(mDeviceHash.size())
        );

        // iterate all devices
        for(String deviceKey: mDeviceHash.keySet()) {

            //
            int deviceId = mDeviceHash.get(deviceKey);

            // device id: 2 bytes
            bytes.append_2(
                    Encoder.encode_char(deviceId)
            );

            // fetch device info
            ParcelableBluetoothDeviceInfo deviceInfo = mDeviceInfo.get(deviceId);

            // fetch device
            BluetoothDevice device = deviceInfo.device;

            // device type (gimbal, ibeacon, or other): 1 byte
            bytes.append((byte) deviceInfo.type);

            // beacon ad length: 1 byte
            bytes.append((byte) deviceInfo.beaconAd.length);

            // device unique identifier ([6,21] bytes)
            bytes.append(deviceInfo.beaconAd);

            // encode device name to bytes
            String name = device.getName();
            int nameLength = 0;

            // no name!
            if(name == null) {
                bytes.append((byte) 0);
            }
            else {
                byte[] nameBytes = name.getBytes(CHARSET_ISO_8859_1);
                nameLength = nameBytes.length;

                // encode length of ssid string: 1 byte
                bytes.append(
                        (byte) nameBytes.length
                );

                // encode sequence of chars
                bytes.append(
                        nameBytes
                );
            }

            // increment actual number of bytes used
            cBytes += 2+1+1+deviceInfo.beaconAd.length+1+nameLength;
        }

        // write to file
        mLogger.submit(bytes.getBytes(cBytes));

        // finished!
        mStopAttempt.ready();
    }

    @Override
    public void start(IpcSubscriber<Bundle> subscriber) {
        mCuriousSubscriber = subscriber;
        oEventDetails = new EventDetails();
        Set<String> deviceSet = mDeviceHash.keySet();
        oEventDetails.setStringSet(deviceSet);
        mBluetooth.scan(mScanResultSubscriber);
    }

    @Override
    public void stop(Attempt attempt) {
        mStopAttempt = attempt;
        mBluetooth.cancel(mScanResultSubscriber);
        this.close();
    }

    @Override
    public String toString() {
        return TAG;
    }
}
