package net.blurcast.tracer.interfaces;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import net.blurcast.tracer.R;
import net.blurcast.tracer.callback.Attempt;
import net.blurcast.tracer.callback.EventDetails;
import net.blurcast.tracer.callback.IpcSubscriber;
import net.blurcast.tracer.encoder.Env_Encoder;
import net.blurcast.tracer.helper.ParcelableSensor;
import net.blurcast.tracer.helper.ParcelableSensorEvent;
import net.blurcast.tracer.interfaces.helper._Simple_ListActivity;

/**
 * Created by blake on 12/30/14.
 */
public class Env_Interface extends _Interface<Env_Encoder, ParcelableSensorEvent> {

    private static final String TAG = Env_Interface.class.getSimpleName();

    private TextView mTitle;

    private String sTemperature = "";
    private String sLight = "";
    private String sPressure = "";
    private String sHumidity = "";

    private boolean bSendToChild = false;


    public Env_Interface() {
        super("Environmental Sensors", "env", Env_Encoder.class, ParcelableSensorEvent.class);
    }

    @Override
    public void onBind() {
        mTitle = getTextView("title");
    }

    private static final float F_CONVERT_CELSIUS_TO_FAHRENHEIT = 9.f / 5.f;

    public static final String EXTRA_TEMPERATURE = "temperature";
    public static final String EXTRA_LIGHT = "light";
    public static final String EXTRA_PRESSURE = "pressure";
    public static final String EXTRA_HUMIDITY = "humidity";


    @Override
    public void start() {

        //
        mTitle.setText("Sensing environment...");
        mTitle.setTextAppearance(mContext, android.R.style.TextAppearance_Small);

        startEncoder(new IpcSubscriber<ParcelableSensorEvent>() {
            @Override
            public void event(ParcelableSensorEvent event, EventDetails eventDetails) {

                ParcelableSensor sensor = event.sensor;
                int iType = sensor.getType();
                float sensorValue = event.values[0];

                String sAccuracy = "" + event.accuracy;
                switch (event.accuracy) {
                    case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                        sAccuracy = "high";
                        break;
                    case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                        sAccuracy = "med";
                        break;
                    case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                        sAccuracy = "low";
                        break;
                }

                if (Sensor.TYPE_AMBIENT_TEMPERATURE == iType) {
                    sTemperature = (Math.round(100 * ((sensorValue * F_CONVERT_CELSIUS_TO_FAHRENHEIT) + 32.f)) / 100.f) + "°F";
                    Log.d(TAG, sensor.getVendor() + "/" + sensor.getName() + "; " + event.values[0] + "°C @" + sAccuracy);
                } else if (Sensor.TYPE_LIGHT == iType) {
                    sLight = sensorValue + " lux";
//                    Log.d(TAG, sensor.getVendor()+"/"+sensor.getName()+"; "+event.values[0]+" lux @"+sAccuracy);
                } else if (Sensor.TYPE_PRESSURE == iType) {
                    sPressure = sensorValue + " hPa";
//                    Log.d(TAG, sensor.getVendor()+"/"+sensor.getName()+"; "+event.values[0]+" hPa @"+sAccuracy);
                } else if (Sensor.TYPE_RELATIVE_HUMIDITY == iType) {
                    sHumidity = sensorValue + "%";
//                    Log.d(TAG, sensor.getVendor()+"/"+sensor.getName()+"; "+event.values[0]+"% humidity @"+sAccuracy);
                }

                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        mTitle.setText(sTemperature + " / " + sLight + "\n" + sPressure + " / " + sHumidity);
                    }
                });

            }
        });
    }

    @Override
    public void stop(final Attempt attempt) {

        stopEncoder(new Attempt() {
            @Override
            public void ready() {

                // all done!
                attempt.ready();

                resetText("title");
            }
        });
    }

    @Override
    public void onFaceClick(View view, boolean live) {
        if(live) {
            startOffspring(EnvironmentDetails.class);
        }
    }


    public static class EnvironmentDetails extends _Simple_ListActivity {

        private static final String TAG = EnvironmentDetails.class.getCanonicalName();

        @Override
        public void setup() {
            addBasicListItem(EXTRA_TEMPERATURE, "Temperature", "Degrees Fahrenheit");
            addBasicListItem(EXTRA_LIGHT, "Luminosity", "");
            addBasicListItem(EXTRA_PRESSURE, "Pressure", "");
            addBasicListItem(EXTRA_HUMIDITY, "Humidity", "Relative Humidity");

            subscribe(new IpcSubscriber<ParcelableSensorEvent>() {
                @Override
                public void event(ParcelableSensorEvent eventData, EventDetails eventDetails) {
                    ParcelableSensor sensor = eventData.sensor;
                    int iType = sensor.getType();
                    float sensorValue = eventData.values[0];

                    if (Sensor.TYPE_AMBIENT_TEMPERATURE == iType) {
                        String sTemperature = (Math.round(100 * ((sensorValue * F_CONVERT_CELSIUS_TO_FAHRENHEIT) + 32.f)) / 100.f) + "°F";
                        setValue(EXTRA_TEMPERATURE, sTemperature);
//                        Log.d(TAG, sensor.getVendor() + "/" + sensor.getName() + "; " + sensorValue + "°C @?");
                    }
                    else if(Sensor.TYPE_LIGHT == iType) {
                        setValue(EXTRA_LIGHT, sensorValue+" lux");
                    }
                    else if(Sensor.TYPE_PRESSURE == iType) {
                        setValue(EXTRA_PRESSURE, sensorValue+" hPa");
                    }
                    else if(Sensor.TYPE_RELATIVE_HUMIDITY == iType) {
                        setValue(EXTRA_HUMIDITY, sensorValue+" %");
                    }
                }
            });
        }
    }
}
