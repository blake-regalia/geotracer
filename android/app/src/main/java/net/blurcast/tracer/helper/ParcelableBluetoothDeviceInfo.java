package net.blurcast.tracer.helper;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by blake on 1/8/15.
 */
public class ParcelableBluetoothDeviceInfo implements Parcelable {

    public static final int BLUETOOTH_TYPE_IBEACON = 0x60;
    public static final int BLUETOOTH_TYPE_GIMBAL = 0x61;
    public static final int BLUETOOTH_TYPE_OTHER = 0x62;

    public int type;
    public String beaconId;
    public byte[] beaconAd;
    public BluetoothDevice device;

    public ParcelableBluetoothDeviceInfo(BluetoothDevice _device, int _type, String _beaconId, byte[] _beaconAd) {
        device = _device;
        type = _type;
        beaconId = _beaconId;
        beaconAd = _beaconAd;
    }

    public ParcelableBluetoothDeviceInfo(Parcel in) {
        type = in.readInt();
        beaconId = in.readString();
        device = in.readParcelable(BluetoothDevice.class.getClassLoader());
        int adSize = in.readInt();
        beaconAd = new byte[adSize];
        in.readByteArray(beaconAd);
    }

    public boolean isTypeGimbal() {
        return BLUETOOTH_TYPE_GIMBAL == type;
    }

    public boolean isTypeIBeacon() {
        return BLUETOOTH_TYPE_IBEACON == type;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int i) {
        out.writeInt(type);
        out.writeString(beaconId);
        out.writeParcelable(device, 0);
        out.writeInt(beaconAd.length);
        out.writeByteArray(beaconAd);
    }

    public static final Creator<ParcelableBluetoothDeviceInfo> CREATOR = new Creator<ParcelableBluetoothDeviceInfo>() {
        public ParcelableBluetoothDeviceInfo createFromParcel(Parcel parcel) {
            return new ParcelableBluetoothDeviceInfo(parcel);
        }

        public ParcelableBluetoothDeviceInfo[] newArray(int size) {
            return new ParcelableBluetoothDeviceInfo[size];
        }
    };
}
