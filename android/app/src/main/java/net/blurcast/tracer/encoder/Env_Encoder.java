package net.blurcast.tracer.encoder;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Message;

import net.blurcast.android.util.ByteBuilder;
import net.blurcast.android.util.Encoder;
import net.blurcast.tracer.app.Geotracer;
import net.blurcast.tracer.callback.Attempt;
import net.blurcast.tracer.callback.EventDetails;
import net.blurcast.tracer.callback.IpcSubscriber;
import net.blurcast.tracer.callback.Subscriber;
import net.blurcast.tracer.driver.Env_Driver;
import net.blurcast.tracer.helper.ParcelableSensor;
import net.blurcast.tracer.helper.ParcelableSensorEvent;
import net.blurcast.tracer.logger._Logger;

import java.util.ArrayList;

/**
 * Created by blake on 12/30/14.
 */
public class Env_Encoder extends _Encoder<ParcelableSensorEvent> {

    private static final String TAG = Env_Encoder.class.getSimpleName();

//    private static final byte SENSOR_TYPE_TEMPERATURE = 0x00;
//    private static final byte SENSOR_TYPE_LIGHT       = 0x01;
//    private static final byte SENSOR_TYPE_PRESSURE    = 0x02;
//    private static final byte SENSOR_TYPE_HUMIDITY    = 0x03;

    private Env_Driver mEnv;
    private Subscriber<SensorEvent> mTemperatureSubscriber;
    private Subscriber<SensorEvent> mLightSubscriber;
    private Subscriber<SensorEvent> mPressureSubscriber;
    private Subscriber<SensorEvent> mHumiditySubscriber;

    private IpcSubscriber<ParcelableSensorEvent> mCuriousTemperatureSubscriber;
    private IpcSubscriber<ParcelableSensorEvent> mCuriousLightSubscriber;
    private IpcSubscriber<ParcelableSensorEvent> mCuriousPressureSubscriber;
    private IpcSubscriber<ParcelableSensorEvent> mCuriousHumiditySubscriber;

    private ArrayList<Sensor> mSensors = new ArrayList<Sensor>(8);

    public Env_Encoder(Context context, _Logger logger) {
        super(logger);
        this.init(context);
    }


    // logs data for any environment sensor
    private void submitSensorData(SensorEvent eventData, EventDetails eventDetails, byte eventType) {

        // reference this sensor
        Sensor sensor = eventData.sensor;

        // lookup sensor
        int sensorIndex = mSensors.indexOf(sensor);

        // sensor not found
        if(sensorIndex == -1) {

            // index of new sensor
            sensorIndex = mSensors.size();

            // add to hash list
            mSensors.add(sensor);
        }

        // prepare byte builder
        ByteBuilder bytes = new ByteBuilder(1+1+3+4);

        // event type: 1 byte
        bytes.append(eventType);

        // sensor id: 1 byte
        bytes.append((byte) sensorIndex);

        // time (1000Hz resolution) since start: 3 bytes (279.6 minutes run-time)
        bytes.append_3(
                mLogger.encodeOffsetTime(eventDetails.getStartTime())
        );

        // sensor value: 4 bytes
        bytes.append_4(
                Encoder.encode_float(eventData.values[0])
        );

        // submit bytes
        mLogger.submit(bytes.getBytes());
    }


    // logs accuracy change for any environment sensor
    private void submitSensorAccuracy(int accuracy, Sensor sensor) {

        // get sensor index
        int sensorId = mSensors.indexOf(sensor);

        // first encounter with sensor
        if(sensorId == -1) {
            sensorId = mSensors.size();
            mSensors.add(sensor);
        }

        //
        ByteBuilder bytes = new ByteBuilder(1+1+1);

        // event type: 1 byte
        bytes.append(_Logger.TYPE_ENV_SENSOR_ACCURACY);

        // sensor id: 1 byte
        bytes.append((byte) sensorId);

        // new accuracy value: 1 byte
        bytes.append((byte) accuracy);

        // submit bytes
        mLogger.submit(bytes.getBytes());
    }



    // create subscriber for each sensor
    private void init(Context context) {
        mEnv = new Env_Driver(context);

        mTemperatureSubscriber = new Subscriber<SensorEvent>() {
            @Override
            public void event(SensorEvent eventData, EventDetails eventDetails) {

                //
                submitSensorData(eventData, eventDetails, _Logger.TYPE_ENV_TEMPERATURE_EVENT);

                // curious subscriber
                if(mCuriousTemperatureSubscriber != null) {
                    mCuriousTemperatureSubscriber.event(new ParcelableSensorEvent(eventData), eventDetails);
                }
            }

            @Override
            public void notice(int noticeType, int noticeValue, Object data) {

                // cast data to sensor
                Sensor sensor = (Sensor) data;

                if(Env_Driver.NOTICE_ACCURACY_CHANGE == noticeType) {

                    //
                    submitSensorAccuracy(noticeValue, sensor);
                }

                // forward to curious subscriber
                if(mCuriousTemperatureSubscriber != null) {
                    mCuriousTemperatureSubscriber.notice(noticeType, noticeValue, new ParcelableSensor(sensor));
                }
            }
        };

        mLightSubscriber = new Subscriber<SensorEvent>() {
            @Override
            public void event(SensorEvent eventData, EventDetails eventDetails) {

                //
                submitSensorData(eventData, eventDetails, _Logger.TYPE_ENV_LIGHT_EVENT);

                if(mCuriousLightSubscriber != null) {
                    mCuriousLightSubscriber.event(new ParcelableSensorEvent(eventData), eventDetails);
                }
            }

            @Override
            public void notice(int noticeType, int noticeValue, Object data) {

                // cast data to sensor
                Sensor sensor = (Sensor) data;

                if(Env_Driver.NOTICE_ACCURACY_CHANGE == noticeType) {

                    //
                    submitSensorAccuracy(noticeValue, sensor);
                }

                // forward to curious subscriber
                if(mCuriousLightSubscriber != null) {
                    mCuriousLightSubscriber.notice(noticeType, noticeValue, new ParcelableSensor(sensor));
                }
            }
        };

        mPressureSubscriber = new Subscriber<SensorEvent>() {
            @Override
            public void event(SensorEvent eventData, EventDetails eventDetails) {

                //
                submitSensorData(eventData, eventDetails, _Logger.TYPE_ENV_PRESSURE_EVENT);

                if(mCuriousPressureSubscriber != null) {
                    mCuriousPressureSubscriber.event(new ParcelableSensorEvent(eventData), eventDetails);
                }
            }

            @Override
            public void notice(int noticeType, int noticeValue, Object data) {

                // cast data to sensor
                Sensor sensor = (Sensor) data;

                if(Env_Driver.NOTICE_ACCURACY_CHANGE == noticeType) {

                    //
                    submitSensorAccuracy(noticeValue, sensor);
                }

                // forward to curious subscriber
                if(mCuriousPressureSubscriber != null) {
                    mCuriousPressureSubscriber.notice(noticeType, noticeValue, new ParcelableSensor(sensor));
                }
            }
        };

        mHumiditySubscriber = new Subscriber<SensorEvent>() {
            @Override
            public void event(SensorEvent eventData, EventDetails eventDetails) {

                //
                submitSensorData(eventData, eventDetails, _Logger.TYPE_ENV_HUMIDITY_EVENT);

                if(mCuriousHumiditySubscriber != null) {
                    mCuriousHumiditySubscriber.event(new ParcelableSensorEvent(eventData), eventDetails);
                }
            }

            @Override
            public void notice(int noticeType, int noticeValue, Object data) {

                // cast data to sensor
                Sensor sensor = (Sensor) data;

                if(Env_Driver.NOTICE_ACCURACY_CHANGE == noticeType) {

                    //
                    submitSensorAccuracy(noticeValue, sensor);
                }

                // forward to curious subscriber
                if(mCuriousHumiditySubscriber != null) {
                    mCuriousHumiditySubscriber.notice(noticeType, noticeValue, new ParcelableSensor(sensor));
                }
            }
        };
    }

    @Override
    public void start(IpcSubscriber<ParcelableSensorEvent> subscriber) {
        this.start(subscriber, subscriber, subscriber, subscriber);
    }

    public void start(IpcSubscriber<ParcelableSensorEvent> temperatureSubscriber, IpcSubscriber<ParcelableSensorEvent> lightSubscriber, IpcSubscriber<ParcelableSensorEvent> pressureSubscriber, IpcSubscriber<ParcelableSensorEvent> humiditySubscriber) {
        mCuriousTemperatureSubscriber = temperatureSubscriber;
        mCuriousLightSubscriber = lightSubscriber;
        mCuriousPressureSubscriber = pressureSubscriber;
        mCuriousHumiditySubscriber = humiditySubscriber;

        // sense the temperature
        mEnv.senseTemperature(mTemperatureSubscriber, SensorManager.SENSOR_DELAY_FASTEST);

        // sense the light
        mEnv.senseLight(mLightSubscriber, SensorManager.SENSOR_DELAY_NORMAL);

        // sense the pressure
        mEnv.sensePressure(mPressureSubscriber, SensorManager.SENSOR_DELAY_NORMAL);

        // sense the humidity
        mEnv.senseHumidity(mHumiditySubscriber, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void replaceCuriousSubscriber(IpcSubscriber<ParcelableSensorEvent> subscriber) {
        mCuriousHumiditySubscriber = subscriber;
        mCuriousLightSubscriber = subscriber;
        mCuriousPressureSubscriber = subscriber;
        mCuriousHumiditySubscriber = subscriber;
    }

    @Override
    public void stop(Attempt attempt) {

        // cancel listening
        mEnv.cancelTemperature(mTemperatureSubscriber);

        // cancel listening
        mEnv.cancelLight(mLightSubscriber);

        // cancel listening
        mEnv.cancelPressure(mPressureSubscriber);

        // cancel listening
        mEnv.cancelHumidity(mHumiditySubscriber);

        // append all sensor info
        this.close();

        // okay, we're done
        attempt.ready();
    }


    private void close() {

        // sensor index
        int iSensor = 0;

        // each sensor
        for(Sensor sensor: mSensors) {

            // vendor string
            byte[] aVendor = sensor.getVendor().getBytes(CHARSET_ISO_8859_1);

            // sensor name string
            byte[] aName = sensor.getName().getBytes(CHARSET_ISO_8859_1);

            //
            ByteBuilder bytes = new ByteBuilder(1+2+1+4
                    +1+aVendor.length
                    +1+aName.length);

            // event type: 1 byte
            bytes.append(_Logger.TYPE_ENV_SENSOR_INFO);

            // sensor index: 2 bytes
            bytes.append(
                    Encoder.encode_char(iSensor)
            );

            // android sensor type: 1 byte
            bytes.append((byte) sensor.getType());

            // version: 4 bytes
            bytes.append_4(
                    Encoder.encode_int(sensor.getVersion())
            );

            // vendor string
            bytes.append((byte) aVendor.length);
            bytes.append(aVendor);

            // name string
            bytes.append((byte) aName.length);
            bytes.append(aName);

            // submit
            mLogger.submit(bytes.getBytes());

            // increment sensor index
            iSensor += 1;
        }
    }

    @Override
    public String toString() {
        return TAG;
    }
}
