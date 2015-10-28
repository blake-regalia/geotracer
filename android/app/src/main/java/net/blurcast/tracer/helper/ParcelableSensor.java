package net.blurcast.tracer.helper;

import android.hardware.Sensor;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by blake on 1/6/15.
 */
public class ParcelableSensor implements Parcelable {

    private String name;
    private float power;
    private int type;
    private String vendor;
    private int version;

    public ParcelableSensor(Sensor sensor) {
        name = sensor.getName();
        power = sensor.getPower();
        type = sensor.getType();
        vendor = sensor.getVendor();
        version = sensor.getVersion();
    }

    public ParcelableSensor(Parcel in) {
        name = in.readString();
        power = in.readFloat();
        type = in.readInt();
        vendor = in.readString();
        version = in.readInt();
    }

    public String getName() {
        return name;
    }

    public float getPower() {
        return power;
    }

    public int getType() {
        return type;
    }

    public String getVendor() {
        return vendor;
    }

    public int getVersion() {
        return version;
    }


    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int i) {
        out.writeString(name);
        out.writeFloat(power);
        out.writeInt(type);
        out.writeString(vendor);
        out.writeInt(version);
    }

    public static final Creator<ParcelableSensor> CREATOR = new Creator<ParcelableSensor>() {
        public ParcelableSensor createFromParcel(Parcel parcel) {
            return new ParcelableSensor(parcel);
        }

        public ParcelableSensor[] newArray(int size) {
            return new ParcelableSensor[size];
        }
    };
}
