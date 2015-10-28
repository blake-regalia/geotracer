package net.blurcast.geotracer_decoder.decoder;

import net.blurcast.geotracer_decoder.logger.ByteDecodingFileReader;
import net.blurcast.geotracer_decoder.surrogate.Env_Surrogate;
import net.blurcast.geotracer_decoder.surrogate._Surrogate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by blake on 1/10/15.
 */
public class Env extends _Decoder {


    public static final byte SENSOR_TYPE_TEMPERATURE = 0x00;
    public static final byte SENSOR_TYPE_LIGHT       = 0x01;
    public static final byte SENSOR_TYPE_PRESSURE    = 0x02;
    public static final byte SENSOR_TYPE_HUMIDITY    = 0x03;

    public LinkedHashMap<Integer, ArrayList<SensorEvent>> events = new LinkedHashMap<Integer, ArrayList<SensorEvent>>();
    public HashMap<Integer, SensorInfo> sensors = new HashMap<Integer, SensorInfo>();

    public Class<? extends _Decoder> getRealClass() {
        return this.getClass();
    }

    @Override
    public _Surrogate getSurrogate() {
        return new Env_Surrogate(this);
    }

    @SuppressWarnings("unused")
    public void decode_sensorInfo(ByteDecodingFileReader source) {

        //
        int sensorIndex = source.read_int_2();

        //
        sensors.get(sensorIndex).extend(source);
    }

    public class SensorInfo {
        public int androidType;
        public int localType;
        public int version;
        public String vendor;
        public String name;

        public SensorInfo(int sensorType) {
            localType = sensorType;
        }

        public void extend(ByteDecodingFileReader source) {
            androidType = source.read();
            version = source.read_int();
            vendor = source.read_string();
            name = source.read_string();
        }
    }

    @SuppressWarnings("unused")
    public void decode_sensorAccuracy(ByteDecodingFileReader source) {

        //
        int sensorId = source.read();

        //
        int newAccuracy = source.read();

        // TODO: sensor accuracy change
//        System.err.println("Environmental Sensor Accuracy Change Event ["+sensorId+"] => "+newAccuracy+" "+source.offset());
    }

    @SuppressWarnings("unused")
    public void decode_temperatureEvent(ByteDecodingFileReader source) {
        decodeSensorEvent(SENSOR_TYPE_TEMPERATURE, source);
    }

    @SuppressWarnings("unused")
    public void decode_lightEvent(ByteDecodingFileReader source) {
        decodeSensorEvent(SENSOR_TYPE_LIGHT, source);
    }

    @SuppressWarnings("unused")
    public void decode_pressureEvent(ByteDecodingFileReader source) {
        decodeSensorEvent(SENSOR_TYPE_PRESSURE, source);
    }

    @SuppressWarnings("unused")
    public void decode_humidityEvent(ByteDecodingFileReader source) {
        decodeSensorEvent(SENSOR_TYPE_HUMIDITY, source);
    }


    //
    public void decodeSensorEvent(byte sensorType, ByteDecodingFileReader source) {

        //
        int sensorId = source.read();

        // first time seeing this sensor
        if(!sensors.containsKey(sensorId)) {

            // store sensor info
            sensors.put(sensorId, new SensorInfo(sensorType));
        }

        // fetch values
        ArrayList<SensorEvent> events;

        // device does not exist yet
        if((events= this.events.get(sensorId)) == null) {

            // create arraylist for values to come from this device
            events = new ArrayList<SensorEvent>();

            // put into hashmap
            this.events.put(sensorId, events);
        }

        // create event
        SensorEvent sensorEvent = new SensorEvent(source);

//        System.out.println("read event "+Integer.toHexString(sensorType)+" ending "+source.offset()+": "+sensorEvent);

        // update hashmap
        events.add(sensorEvent);
    }

    public class SensorEvent {
        public int elapsed;
        public float value;

        public SensorEvent(ByteDecodingFileReader source) {

            // time since start
            elapsed = source.read_int_3();

            // sensor observation
            value = source.read_float();
        }

        public String toString() {
            return "{elapsed: "+elapsed+"; value:"+value+"}";
        }
    }

}
