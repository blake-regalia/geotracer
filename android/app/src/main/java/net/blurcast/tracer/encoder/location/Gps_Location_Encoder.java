package net.blurcast.tracer.encoder.location;

import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;

import net.blurcast.android.util.ByteBuilder;
import net.blurcast.android.util.Encoder;
import net.blurcast.tracer.callback.Attempt;
import net.blurcast.tracer.callback.EventDetails;
import net.blurcast.tracer.callback.IpcSubscriber;
import net.blurcast.tracer.callback.Subscriber;
import net.blurcast.tracer.driver.Gps_Driver;
import net.blurcast.tracer.logger._Logger;

/**
 * Created by blake on 10/20/14.
 */
public class Gps_Location_Encoder extends _Location_Encoder<Location> {

    // constants
    private static final String TAG = Gps_Location_Encoder.class.getSimpleName();

    // primitive data types


    // resources
    private Gps_Driver mGps;

    // for handling location events
    private Subscriber<Location> mLocationUpdateSubscriber;

    // for handling gps status events
    private Subscriber<GpsStatus> mGpsStatusSubscriber;

    // for the curious user to see info
    private IpcSubscriber<Location> mCuriousLocationSubscriber;


    public Gps_Location_Encoder(Context context, _Logger logger) {
        super(context, logger);
        this.init();
    }

    private void init() {
        mGps = Gps_Driver.getInstance(mContext);

        // friending
        final Gps_Location_Encoder friend = this;

        // for location update events
        mLocationUpdateSubscriber = new Subscriber<Location>() {

            @Override
            public void event(Location eventData, EventDetails eventDetails) {

                // commit events to log
                friend.logLocation(eventData);
            }

            @Override
            public void error(String error) {

                // gps was disabled (we are automatically unsubscribed)
                if(Gps_Driver.ERR_DISABLED.equals(error)) {

                    // forward error to subscriber
                    if(mCuriousLocationSubscriber != null) mCuriousLocationSubscriber.error(error);
                }
            }
        };

        // for gps status events
        mGpsStatusSubscriber = new Subscriber<GpsStatus>() {

            @Override
            public void event(GpsStatus gpsStatus, EventDetails eventDetails) {

                // commit gps status to log
                friend.logGpsStatus(gpsStatus, eventDetails);
            }
        };
    }

    protected void logLocation(Location location) {

        // the curious user wants to see what's going on
        if(mCuriousLocationSubscriber != null) {
            mCuriousLocationSubscriber.event(location, null);
        }

        // prepare byte builder, generate data segment header & entry
        ByteBuilder bytes = new ByteBuilder(1+8+8+2+1+3);

        // event type: 1 byte
        bytes.append(_Logger.TYPE_GPS_LOCATION);

        // time of gps fix (1000Hz resolution) start offset: 3 bytes (279.6 minutes run-time)
        bytes.append_3(
                mLogger.encodeOffsetTimeFromNanos(location.getElapsedRealtimeNanos())
        );

        // latitude: 8 bytes
        bytes.append_8(
                Encoder.encode_double(location.getLatitude())
        );

        // longitude: 8 bytes
        bytes.append_8(
                Encoder.encode_double(location.getLongitude())
        );

        // altitude [-32,768, +32,767]: 2 bytes
        bytes.append_2(
                Encoder.encode_short(
                        (short) Math.round(location.getAltitude())
                )
        );

        // accuracy [0, 255]: 1 byte
        int accuracy = Math.round(location.getAccuracy());
        if(accuracy < 0) accuracy = 0;
        else if(accuracy > 255) accuracy = 255;
        bytes.append(
                Encoder.encode_byte(
                        accuracy
                )
        );

        // submit location data
        mLogger.submit(bytes.getBytes());
    }


    //
    private static final int N_BYTES_SATELLITE = 4+1+4+4+4;

    // for writing information about gps satellites
    protected void logGpsStatus(GpsStatus gpsStatus, EventDetails eventDetails) {

        // prepare a byte buffer to store all possible satellites
        ByteBuilder satellites = new ByteBuilder(gpsStatus.getMaxSatellites()*N_BYTES_SATELLITE);

        // fetch all satellites
        int cSatellites = 0;
        for(GpsSatellite satellite: gpsStatus.getSatellites()) {

            // pseudo-random number (satellite id)
            satellites.append_4(
                    Encoder.encode_int(satellite.getPrn())
            );

            // used in location fix? : 1 byte
            satellites.append(
                    (byte) (satellite.usedInFix() ? 1 : 0)
            );

            // azimuth
            satellites.append_4(
                    Encoder.encode_float(satellite.getAzimuth())
            );

            // elevation
            satellites.append_4(
                    Encoder.encode_float(satellite.getElevation())
            );

            // signal to noise ratio
            satellites.append_4(
                    Encoder.encode_float(satellite.getSnr())
            );

            cSatellites += 1;
        }

        // trim to real size
        byte[] satelliteBytes = satellites.getBytes(cSatellites * N_BYTES_SATELLITE);

        // prepare real byte builder
        ByteBuilder bytes = new ByteBuilder(1+3+3+1+ satelliteBytes.length);

        // event type: 1 byte
        bytes.append(_Logger.TYPE_GPS_SATELLITES);

        // fetch time span of satellite update
        long[] timeSpan = eventDetails.getTimeSpan();

        // time span (1000Hz resolution) start offset: 3 bytes (279.6 minutes run-time)
        bytes.append_3(
                mLogger.encodeOffsetTime(timeSpan[0])
        );

        // time span (1000Hz resolution) scan time: 3 bytes (279 minutes max)
        bytes.append_3(
                Encoder.encode_long_to_3_bytes(
                        timeSpan[1] - timeSpan[0]
                )
        );

        // satellite count
        bytes.append(
                Encoder.encode_byte(cSatellites)
        );

        // satellites
        bytes.append(satelliteBytes);

        // submit satellite data
        mLogger.submit(bytes.getBytes());
    }


    @Override
    public void prepare(final Attempt attempt) {

        // attempt to enable gps programatically
        mGps.enableGps(attempt);
    }

    @Override
    public boolean isPrepared() {
        return mGps.isGpsEnabled();
    }


    @Override
    public void start() {
        this.start(null);
    }


    @Override
    public void start(IpcSubscriber<Location> subscriber) {

        // allow optional curious subscriber
        mCuriousLocationSubscriber = subscriber;

        // request updates
        mGps.requestUpdates(mLocationUpdateSubscriber, mGpsStatusSubscriber);
    }

    @Override
    public void stop(Attempt attempt) {

        // stop immediately
        mGps.cancelUpdates(mLocationUpdateSubscriber, mGpsStatusSubscriber);

        // done!
        attempt.ready();
    }

}
