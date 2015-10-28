package net.blurcast.tracer.helper;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by blake on 1/6/15.
 */
public class ParcelableSensorEvent implements Parcelable {

    public int accuracy;
    public long timestamp;
    public ParcelableSensor sensor;
    public float[] values;

    public ParcelableSensorEvent(SensorEvent sensorEvent) {
        accuracy = sensorEvent.accuracy;
        timestamp = sensorEvent.timestamp;
        sensor = new ParcelableSensor(sensorEvent.sensor);
        values = sensorEvent.values;
    }

    public ParcelableSensorEvent(Parcel in) {
        accuracy = in.readInt();
        timestamp = in.readLong();
        sensor = in.readParcelable(ParcelableSensor.class.getClassLoader());
        int valueSize = in.readInt();
        values = new float[valueSize];
        in.readFloatArray(values);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int i) {
        out.writeInt(accuracy);
        out.writeLong(timestamp);
        out.writeParcelable(sensor, 0);
        out.writeInt(values.length);
        out.writeFloatArray(values);
    }

    public static final Creator<ParcelableSensorEvent> CREATOR = new Creator<ParcelableSensorEvent>() {
        public ParcelableSensorEvent createFromParcel(Parcel parcel) {
            return new ParcelableSensorEvent(parcel);
        }

        public ParcelableSensorEvent[] newArray(int size) {
            return new ParcelableSensorEvent[size];
        }
    };
}
