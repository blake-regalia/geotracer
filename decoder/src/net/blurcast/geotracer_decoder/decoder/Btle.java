package net.blurcast.geotracer_decoder.decoder;

import net.blurcast.geotracer_decoder.logger.ByteDecodingFileReader;
import net.blurcast.geotracer_decoder.surrogate.Btle_Surrogate;
import net.blurcast.geotracer_decoder.surrogate._Surrogate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by blake on 1/10/15.
 */
public class Btle extends _Decoder {

    public static final int BLUETOOTH_TYPE_IBEACON = 0x60;
    public static final int BLUETOOTH_TYPE_GIMBAL  = 0x61;
    public static final int BLUETOOTH_TYPE_OTHER   = 0x62;


    public LinkedHashMap<Integer, ArrayList<Event>> events = new LinkedHashMap<Integer, ArrayList<Event>>();
    public HashMap<Integer, Device> devices = new HashMap<Integer, Device>();


    @Override
    public Class<? extends _Decoder> getRealClass() {
        return this.getClass();
    }

    @Override
    public _Surrogate getSurrogate() {
        return new Btle_Surrogate(this);
    }

    @SuppressWarnings("unused")
    public void decode_event(ByteDecodingFileReader source) {

        //
        int deviceId = source.read_int_2();

        // find wap
        ArrayList<Event> aRssis = events.get(deviceId);

        // first time seeing wap
        if(aRssis == null) {
            aRssis = new ArrayList<Event>();
            events.put(deviceId, aRssis);
        }

        // new event
        Event btleEvent = new Event(source);

//        System.out.println(btleEvent+" "+source.offset());

        // add to set
        aRssis.add(btleEvent);
    }

    public class Event {
        public int elapsed;
        public byte rssi;

        public Event(ByteDecodingFileReader source) {
            elapsed = source.read_int_3();
            rssi = source.read_byte();
        }
        public String toString() {
            return "+btleEvent {elapsed:"+elapsed+"; rssi:"+rssi+"}";
        }
    }

    @SuppressWarnings("unused")
    public void decode_info(ByteDecodingFileReader source) {

        //
        int numDevices = source.read_int_2();

        // loop
        for(int i=0; i<numDevices; i++) {

            // get device id
            int deviceId = source.read_int_2();

            // create device object
            Device btleDevice = new Device(source);

//            System.out.println(btleDevice+" "+source.offset());

            // add to set
            devices.put(deviceId, btleDevice);
        }
    }

    public class Device {
        public int type;
        public byte[] beaconAd;
        public String name;

        public Device(ByteDecodingFileReader source) {
            type = source.read();
            int adLength = source.read();
            beaconAd = new byte[adLength];
            source.read(beaconAd);
            name = source.read_string();
        }

        public String toString() {
            return "+btle device";
        }

        public IBeaconInfo getIBeaconInfo() {
            return new IBeaconInfo(beaconAd);
        }

        public class IBeaconInfo {
            public byte[] proximity;
            public int major;
            public int minor;
            public byte txPower;

            public IBeaconInfo(byte[] ad) {
                proximity = Arrays.copyOf(ad, 16);
                major = (ad[16] << 8) | ad[17];
                minor = (ad[18] << 8) | ad[19];
                txPower = ad[20];
            }
        }
    }


//
//    public static final _OutputRenderer<Btle> OUTPUT_RENDERER_TEXT = new Text_OutputRenderer<Btle>() {
//
//        private Btle mDecoder;
//
//        @Override
//        public void setInstance(Btle decoder) {
//            mDecoder = decoder;
//        }
//
//        @Override
//        public void print(_Log log) {
//
//            //
//            log.out("\n===== Bluetooth Low Energy Devices =====");
//
//
//            for(Map.Entry<Integer, Device> entry: mDecoder.devices.entrySet()) {
//                Device device = entry.getValue();
//
//                StringBuilder stringBuilder = new StringBuilder();
//                for(Event event: mDecoder.events.get(entry.getKey())) {
//                    stringBuilder.append(event.rssi).append(", ");
//                }
//
//                log.out(mDecoder.hwToString(device.beaconAd)+" => {"+stringBuilder.toString()+"}");
//            }
//        }
//    };
}
