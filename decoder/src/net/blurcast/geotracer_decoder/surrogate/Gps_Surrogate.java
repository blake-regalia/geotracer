package net.blurcast.geotracer_decoder.surrogate;

import net.blurcast.geotracer_decoder.decoder.Gps;
import net.blurcast.geotracer_decoder.helper.Wkt;

import java.util.Map;

/**
 * Created by blake on 1/11/15.
 */
public class Gps_Surrogate extends _Surrogate<Integer, Gps.Location, Gps> {

    public Gps_Surrogate(Gps gps) {
        super(gps, gps.locations);
    }

    public EstimatedLocation interpolate(final int elapsed) {
        Map.Entry<Integer, Gps.Location> previousEvent = null;

        // iterate each event
        for(Map.Entry<Integer, Gps.Location> event: own.locations.entrySet()) {

//            System.out.println("Searching for elapsed{"+elapsed+"}; at["+event.getKey()+"]");

            // time the event occurred
            int currentTime = event.getKey();

//            System.out.println("elapsed: "+elapsed+"; currentTime: "+currentTime);

            // found where this event occurs
            if(elapsed <= currentTime) {

                // reference current location
                Gps.Location currentLocation = event.getValue();

                // no previous location available, return earliest fix
                if(previousEvent == null) return null; // TODO return estimated location before first fix

                // times match! (extremely rare)
                if(elapsed == currentTime) return new EstimatedLocation(currentLocation);

                // calculate the scaled time offset
                int previousTime = previousEvent.getKey();
                Gps.Location previousLocation = previousEvent.getValue();
                double scale = (elapsed - previousTime) / ((double) (currentTime - previousTime));

//                System.out.println("Using location ["+event.getKey()+"]");

                // perform interpolation between previous and current
                return new EstimatedLocation(previousLocation, currentLocation, scale);
            }

            previousEvent = event;
        }

        // TODO return estimated location beyond last fix
        return null;
    }

    public class EstimatedLocation implements Wkt.Geometry {
        public double latitude;
        public double longitude;
        public double confidence;

        public EstimatedLocation(Gps.Location location) {
            latitude = location.latitude;
            longitude = location.longitude;
            confidence = 1.0;
        }

        public EstimatedLocation(Gps.Location locationA, Gps.Location locationB, double scale) {
            latitude = (scale * (locationB.latitude - locationA.latitude)) + locationA.latitude;
            longitude = (scale * (locationB.longitude - locationA.longitude)) + locationB.longitude;
            confidence = Math.abs(scale - 0.5) / 0.5;
//            latitude = locationA.latitude;
//            longitude = locationA.longitude;
        }

        public String getWkt() {
            return Wkt.point(longitude, latitude);
        }
    }

    public Interpolation<Gps.Location> interpolateRange(int elapsed, int duration) {
        return null;
    }
}
