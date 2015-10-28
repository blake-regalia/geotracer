package net.blurcast.tracer.interfaces;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.TextView;

import net.blurcast.android.util.Bytes;
import net.blurcast.android.util.Timeout;
import net.blurcast.tracer.R;
import net.blurcast.tracer.app.Geotracer;
import net.blurcast.tracer.callback.Attempt;
import net.blurcast.tracer.callback.EventDetails;
import net.blurcast.tracer.callback.IpcSubscriber;
import net.blurcast.tracer.driver.Btle_Driver;
import net.blurcast.tracer.encoder.Btle_Encoder;
import net.blurcast.tracer.encoder._Encoder;
import net.blurcast.tracer.helper.ParcelableBluetoothDeviceInfo;
import net.blurcast.tracer.interfaces.helper._Dynamic_ListActivity;

import java.util.HashMap;

/**
 * Created by blake on 12/29/14.
 */
public class Btle_Interface extends _Interface<Btle_Encoder, Bundle> {

    private static final String TAG = Btle_Interface.class.getSimpleName();

    protected static final int T_STALE_BTLE_DURATION = 2500; // 2.5 seconds

    private TextView mTitle;

    public Btle_Interface() {
        super("Bluetooth Low Energy", "btle", Btle_Encoder.class, Bundle.class);
    }

    @Override
    public void onBind() {
        mTitle = getTextView("title");
    }

    @Override
    public void prepare(final Attempt attempt) {
        Btle_Driver bluetooth = Btle_Driver.getInstance(mContext);

        // bluetooth is enabled!
        if(bluetooth.isEnabled()) {
            attempt.ready();
        }
        // attempt to enable bluetooth
        else {
            bluetooth.enable(new Attempt() {
                @Override
                public void ready() {
                    attempt.ready();
                }
                @Override
                public void error(int reason) {
                    attempt.error(Geotracer.PREPARE_ERROR_BLUETOOTH_DISABLED);
                }
            });
        }
    }

    @Override
    public void start() {

        // set text status
        mTitle.setText("Scanning for LE devices...");

        // fire off encoder
        startEncoder(new IpcSubscriber<Bundle>() {
            @Override
            public void event(Bundle scan, EventDetails eventDetails) {

                //
                final int nSetSize = eventDetails.getStringArray().length;

                // update title
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        mTitle.setText(nSetSize + " device(s) discovered");
                    }
                });
            }
        });
    }

    @Override
    public void stop(final Attempt attempt) {

        // stop encoder
        stopEncoder(new Attempt() {
            @Override
            public void ready() {

                // okay, we're done
                attempt.ready();

                // reset text
                resetText("title");
            }
        });
    }

    @Override
    public void onFaceClick(View view, boolean live) {
        if(live) {
            startOffspring(BtleDetails.class);
        }
    }



    public static class BtleDetails extends _Dynamic_ListActivity {

        private static final String TAG = BtleDetails.class.getSimpleName();

        private static final String ITEM_ID = "id";
        private static final String ITEM_RSSI = "rssi";
        private static final String ITEM_DEVICE_TYPE = "device-type";
        private static final String ITEM_SIGNAL = "signal";

        private SparseArray<ParcelableBluetoothDeviceInfo> mDevices = new SparseArray<ParcelableBluetoothDeviceInfo>();
        private SparseIntArray mStaleTimers = new SparseIntArray();

        @Override
        public void setup() {
            createAdapter(R.layout.btle_list_item,
                    new String[]{
                            ITEM_ID, ITEM_RSSI, ITEM_DEVICE_TYPE, ITEM_SIGNAL
                    },
                    new int[]{
                            R.id.app_data_btle_item_id, R.id.app_data_btle_item_rssi, R.id.app_data_btle_item_device_type, R.id.app_data_btle_item_signal,
                    });

            subscribe(new IpcSubscriber<Bundle>() {
                @Override
                public void event(Bundle eventData, EventDetails eventDetails) {

                    // fetch service-assigned device id
                    int deviceId = eventData.getInt(Geotracer.DATA_ENTRY_ID);
                    int rssi = eventData.getInt(Btle_Driver.DATA_RSSI, 0);

                    // null rssi value, bail!
                    if(rssi == 0) return;

                    // cancel any timers for this device (if any exist)
                    Timeout.clearTimeout(mStaleTimers.get(deviceId, -1));

                    // prepare a reference for this device
                    ParcelableBluetoothDeviceInfo deviceInfo;

                    // never seen this device before
                    if(mDevices.get(deviceId) == null) {

                        // fetch device info from bundle
                        deviceInfo = (ParcelableBluetoothDeviceInfo) eventData.getParcelable(Geotracer.DATA_ENTRY_INFO);

                        // store pair
                        mDevices.put(deviceId, deviceInfo);

                        // add item to list
                        addItem(deviceId);
                    }
                    else {
                        // fetch device info from hash
                        deviceInfo = mDevices.get(deviceId);
                    }

                    // update that rows rssi value (distance)
                    final HashMap<String, String> row = mItemLookup.get(deviceId + "");

                    // calculate distance
                    if(deviceInfo.isTypeIBeacon()) {
                        double distance;
                        double ratio = rssi * 1.0 / ((int) deviceInfo.beaconAd[deviceInfo.beaconAd.length-1]);
                        if (ratio < 1.0) {
                            distance = Math.pow(ratio, 10);
                        } else {
                            distance = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
                        }
                        Log.i(TAG, "Distance [" + rssi + "] = " + distance);
                        row.put(ITEM_SIGNAL, (Math.round(distance*100)/100.0)+"m");
                    }
                    else {
                        row.put(ITEM_SIGNAL, rssi+"dBm");
                    }
                    row.put(ITEM_RSSI, (rssi+100)+"");

                    // update ui!
                    mBaseAdapter.notifyDataSetChanged();

                    // start timer for marking stale gps fixes
                    mStaleTimers.put(deviceId, Timeout.setTimeout(new Runnable() {
                        public void run() {
                            mDisplayItems.remove(row);
                        }
                    }, T_STALE_BTLE_DURATION, BtleDetails.this));
                }

                @Override
                public void notice(int noticeType, int deviceId, Parcelable data) {

                    // new device!
                    if(Geotracer.NOTICE_NEW_ENTRY == noticeType) {

                        // value
                        ParcelableBluetoothDeviceInfo deviceInfo = (ParcelableBluetoothDeviceInfo) data;

                        // store pair
                        mDevices.put(deviceId, deviceInfo);

                        // add item to list
                        addItem(deviceId);
                    }
                }
            });
        }

        private void addItem(int deviceId) {

            // prepare a row to insert
            HashMap<String, String> row = new HashMap<String, String>();

            // fetch row data
            ParcelableBluetoothDeviceInfo deviceInfo = mDevices.get(deviceId);

            // prepare a string label
            String sLabel = "??";

            // value: device type
            switch(deviceInfo.type) {
                case ParcelableBluetoothDeviceInfo.BLUETOOTH_TYPE_GIMBAL:
                    row.put(ITEM_DEVICE_TYPE, "Gimbal Beacon");
                    sLabel = Base64.encodeToString(deviceInfo.beaconAd, Base64.DEFAULT);
                    break;
                case ParcelableBluetoothDeviceInfo.BLUETOOTH_TYPE_IBEACON:
                    row.put(ITEM_DEVICE_TYPE, "iBeacon");
                    sLabel = Bytes.toHexString(deviceInfo.beaconId.substring(16).getBytes(_Encoder.CHARSET_ISO_8859_1), '/');
                    break;
                case ParcelableBluetoothDeviceInfo.BLUETOOTH_TYPE_OTHER:
                    row.put(ITEM_DEVICE_TYPE, "Other Low Energy Device");
                    sLabel = Bytes.toHexString(deviceInfo.beaconAd, ':');
                    break;
            }

            // value: latest rssi
            row.put(ITEM_RSSI, "0");
            row.put(ITEM_SIGNAL, "0");

            // value: item id
            row.put(ITEM_ID, sLabel);

            // store reference to this row even if it is not displayed
            mItemLookup.put(deviceId + "", row);

            // insert new row
            mDisplayItems.add(row);
        }

    }

}
