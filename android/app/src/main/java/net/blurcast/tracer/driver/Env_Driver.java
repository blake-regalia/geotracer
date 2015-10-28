package net.blurcast.tracer.driver;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import net.blurcast.tracer.app.Geotracer;
import net.blurcast.tracer.callback.EventDetails;
import net.blurcast.tracer.callback.Subscriber;
import net.blurcast.tracer.callback.SubscriberSet;
import net.blurcast.tracer.helper.ParcelableSensor;

import java.util.List;

/**
 * Created by blake on 12/30/14.
 */
public class Env_Driver {

    private static final String TAG = Env_Driver.class.getSimpleName();

    public static final int NOTICE_ACCURACY_CHANGE = 1;


    //
    private static Env_Driver mInstance;
    public static Env_Driver getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new Env_Driver(context);
        }
        return mInstance;
    }


    //
    private Context mContext;
    private SensorManager mSensorManager;

    // constructor
    public Env_Driver(Context context) {
        mContext = context;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        this.init();
    }


    //
    private List<Sensor> mTemperatureSensors;
    private List<Sensor> mLightSensors;
    private List<Sensor> mPressureSensors;
    private List<Sensor> mHumiditySensors;

    //
    private SensorEventListener mTemperatureListener;
    private SensorEventListener mLightListener;
    private SensorEventListener mPressureListener;
    private SensorEventListener mHumidityListener;

    //
    private SubscriberSet<SensorEvent> mTemperatureSubscribers = new SubscriberSet<SensorEvent>();
    private SubscriberSet<SensorEvent> mLightSubscribers = new SubscriberSet<SensorEvent>();
    private SubscriberSet<SensorEvent> mPressureSubscribers = new SubscriberSet<SensorEvent>();
    private SubscriberSet<SensorEvent> mHumiditySubscribers = new SubscriberSet<SensorEvent>();

    //
    private boolean bTemperatureActive = false;
    private boolean bLightActive = false;
    private boolean bPressureActive = false;
    private boolean bHumidityActive = false;

    //
    private void init() {

        // temperature sensors
        mTemperatureSensors = mSensorManager.getSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if(mTemperatureSensors.size() == 0) mTemperatureSensors = null;
        else {
            mTemperatureListener = new SensorEventListener() {

                // new sensor data
                public void onSensorChanged(SensorEvent sensorEvent) {
                    mTemperatureSubscribers.event(sensorEvent, new EventDetails());
                }

                // sensor accuracy changed
                public void onAccuracyChanged(Sensor sensor, int i) {
                    noticeAccuracyChange(mTemperatureSubscribers, sensor, i);
                }
            };
        }

        // light sensors
        mLightSensors = mSensorManager.getSensorList(Sensor.TYPE_LIGHT);
        if(mLightSensors.size() == 0) mLightSensors = null;
        else {
            mLightListener = new SensorEventListener() {
                public void onSensorChanged(SensorEvent sensorEvent) {
                    mLightSubscribers.event(sensorEvent, new EventDetails());
                }
                public void onAccuracyChanged(Sensor sensor, int i) {
                    noticeAccuracyChange(mLightSubscribers, sensor, i);
                }
            };
        }

        // pressure sensor
        mPressureSensors = mSensorManager.getSensorList(Sensor.TYPE_PRESSURE);
        if(mPressureSensors.size() == 0) mPressureSensors = null;
        else {
            mPressureListener = new SensorEventListener() {
                public void onSensorChanged(SensorEvent sensorEvent) {
                    mPressureSubscribers.event(sensorEvent, new EventDetails());
                }
                public void onAccuracyChanged(Sensor sensor, int i) {
                    noticeAccuracyChange(mPressureSubscribers, sensor, i);
                }
            };
        }

        // humidity sensors
        mHumiditySensors = mSensorManager.getSensorList(Sensor.TYPE_RELATIVE_HUMIDITY);
        if(mHumiditySensors.size() == 0) mHumiditySensors = null;
        else {
            mHumidityListener = new SensorEventListener() {
                public void onSensorChanged(SensorEvent sensorEvent) {
                    mHumiditySubscribers.event(sensorEvent, new EventDetails());
                }
                public void onAccuracyChanged(Sensor sensor, int i) {
                    noticeAccuracyChange(mHumiditySubscribers, sensor, i);
                }
            };
        }
    }

    // requests to be notified of temperature changes
    public int senseTemperature(Subscriber<SensorEvent> subscriber, int sensorDelay) {
        mTemperatureSubscribers.add(subscriber);
        if(!bTemperatureActive) {
            bTemperatureActive = true;
            if (mTemperatureSensors == null) return 0;
            for (Sensor sensor : mTemperatureSensors) {
                mSensorManager.registerListener(mTemperatureListener, sensor, sensorDelay);
            }
        }
        return mTemperatureSensors.size();
    }

    public int senseLight(Subscriber<SensorEvent> subscriber, int sensorDelay) {
        mLightSubscribers.add(subscriber);
        if(!bLightActive) {
            bLightActive = true;
            if (mLightSensors == null) return 0;
            for (Sensor sensor : mLightSensors) {
                mSensorManager.registerListener(mLightListener, sensor, sensorDelay);
            }
        }
        return mLightSensors.size();
    }

    public int sensePressure(Subscriber<SensorEvent> subscriber, int sensorDelay) {
        mPressureSubscribers.add(subscriber);
        if(!bPressureActive) {
            bPressureActive = true;
            if (mPressureSensors == null) return 0;
            for (Sensor sensor : mPressureSensors) {
                mSensorManager.registerListener(mPressureListener, sensor, sensorDelay);
            }
        }
        return mPressureSensors.size();
    }

    public int senseHumidity(Subscriber<SensorEvent> subscriber, int sensorDelay) {
        mHumiditySubscribers.add(subscriber);
        if(!bHumidityActive) {
            bHumidityActive = true;
            if (mHumiditySensors == null) return 0;
            for (Sensor sensor : mHumiditySensors) {
                mSensorManager.registerListener(mHumidityListener, sensor, sensorDelay);
            }
        }
        return mHumiditySensors.size();
    }


    public void cancelTemperature(Subscriber<SensorEvent> subscriber) {
        if(mTemperatureSubscribers.remove(subscriber)) {
            mSensorManager.unregisterListener(mTemperatureListener);
        }
    }

    public void cancelLight(Subscriber<SensorEvent> subscriber) {
        if(mLightSubscribers.remove(subscriber)) {
            mSensorManager.unregisterListener(mLightListener);
        }
    }

    public void cancelPressure(Subscriber<SensorEvent> subscriber) {
        if(mPressureSubscribers.remove(subscriber)) {
            mSensorManager.unregisterListener(mPressureListener);
        }
    }

    public void cancelHumidity(Subscriber<SensorEvent> subscriber) {
        if(mHumiditySubscribers.remove(subscriber)) {
            mSensorManager.unregisterListener(mHumidityListener);
        }
    }

    private void noticeAccuracyChange(SubscriberSet subscriberSet, Sensor sensor, int accuracy) {
//        Bundle data = new Bundle();
//        data.putInt(Geotracer.DATA_NEW_VALUE, accuracy);
//        data.putParcelable(Geotracer.DATA_ENTRY_OWNER, new ParcelableSensor(sensor));
        subscriberSet.notice(NOTICE_ACCURACY_CHANGE, accuracy, sensor);
    }

}
