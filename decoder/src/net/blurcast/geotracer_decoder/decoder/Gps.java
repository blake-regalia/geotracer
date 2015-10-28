package net.blurcast.geotracer_decoder.decoder;

import net.blurcast.geotracer_decoder.logger.ByteDecodingFileReader;
import net.blurcast.geotracer_decoder.surrogate.Gps_Surrogate;
import net.blurcast.geotracer_decoder.surrogate._Surrogate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by blake on 1/11/15.
 */
public class Gps extends _Decoder {

    public LinkedHashMap<Integer, Location> locations = new LinkedHashMap<Integer, Location>();
    public ArrayList<SatellitesEvent> satelliteEvents = new ArrayList<SatellitesEvent>();

    @Override
    public Class<? extends _Decoder> getRealClass() {
        return this.getClass();
    }

    @Override
    public _Surrogate getSurrogate() {
        return new Gps_Surrogate(this);
    }


    @SuppressWarnings("unused")
    public void decode_init(ByteDecodingFileReader source) {}

    @SuppressWarnings("unused")
    public void decode_location(ByteDecodingFileReader source) {
        int elapsed = source.read_int_3();
//        System.out.println("+location {elapsed: "+elapsed+"}");
        locations.put(elapsed, new Location(source));
    }

    public class Location {
        public double latitude;
        public double longitude;
        public short altitude;
        public int accuracy;

        public Location(ByteDecodingFileReader source) {
            latitude = source.read_double();
            longitude = source.read_double();
            altitude = source.read_short();
            accuracy = source.read();
//            System.out.println(this);
        }

        public String toString() {
            return "+location {lat:"+latitude+"; lng:"+longitude+"}";
        }
    }

    @SuppressWarnings("unused")
    public void decode_satellites(ByteDecodingFileReader source) {
        satelliteEvents.add(new SatellitesEvent(source));
//        System.out.println(satelliteEvents.get(satelliteEvents.size()-1));
    }


    public class SatellitesEvent {
        int elapsed;
        int duration;
        Satellite[] mSatellites;

        public SatellitesEvent(ByteDecodingFileReader source) {

            // time span (start offset and duration)
            elapsed = source.read_int_3();
            duration = source.read_int_3();

            // number of entries
            int numSatellites = source.read();

            // init array
            mSatellites = new Satellite[numSatellites];

            // loop
            for(int i=0; i<numSatellites; i++) {

                // new satellite
                mSatellites[i] = new Satellite(source);
//                System.out.println(mSatellites[i]);
            }
        }

        public String toString() {
            return "+Satellites {elapsed:"+elapsed+"; duration:"+duration+"; numSats:"+mSatellites.length+"}";
        }
    }

    public class Satellite {
        int satelliteId;
        boolean usedInFix;
        float azimuth;
        float elevation;
        float snr;

        public Satellite(ByteDecodingFileReader source) {
            satelliteId = source.read_int();
            usedInFix = (source.read() == 1);
            azimuth = source.read_float();
            elevation = source.read_float();
            snr = source.read_float();
        }

        public String toString() {
            return "+satellite["+satelliteId+"]: {used:"+usedInFix+"; azimuth:"+azimuth+"; elevation:"+elevation+"; snr:"+snr+"}";
        }
    }

    @SuppressWarnings("unused")
    public void decode_status(ByteDecodingFileReader source) {

    }
}
