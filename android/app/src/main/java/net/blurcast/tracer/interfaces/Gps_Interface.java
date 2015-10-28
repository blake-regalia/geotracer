package net.blurcast.tracer.interfaces;

import android.location.Location;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

import net.blurcast.android.util.Timeout;
import net.blurcast.tracer.R;
import net.blurcast.tracer.app.Geotracer;
import net.blurcast.tracer.callback.Attempt;
import net.blurcast.tracer.callback.EventDetails;
import net.blurcast.tracer.callback.IpcSubscriber;
import net.blurcast.tracer.driver.Gps_Driver;
import net.blurcast.tracer.encoder.location.Gps_Location_Encoder;

/**
 * Created by blake on 12/29/14.
 */
public class Gps_Interface extends _Location_Interface<Gps_Location_Encoder, Location> {

    // declare a gps fix stale after 5 seconds
    private static final long T_STALE_GPS_DURATION = 5000L;

    private TextView mTitle;
    private Runnable mStaleGpsRunner;
    private Gps_Driver mGps;


    public Gps_Interface() {
        super("GPS Location", "gps", Gps_Location_Encoder.class, Location.class);

        // create a runnable for declaring stale gps fixes
        mStaleGpsRunner = new Runnable() {
            public void run() {

                // calculate time since last gps fix
                int age = (int) Math.floor((SystemClock.elapsedRealtime()-tLastFix) / 1000.f);

                // update the title to notify this age
                mTitle.setText(sCoordinates+"; "+age+"s old");

                // update fix age w/ half-second precision
                iStaleTimer = Timeout.setTimeout(mStaleGpsRunner, 500, mActivity);

                // update gps icon
                mTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_location_off, 0, 0, 0);
            }
        };
    }

    @Override
    protected void onBind() {
        mTitle = getTextView("title");
        mGps = Gps_Driver.getInstance(mContext);
    }

    @Override
    public boolean isPrepared() {
        return mGps.isGpsEnabled();
    }

    @Override
    public void prepare(final Attempt attempt) {
        mGps.enableGps(new Attempt() {
            @Override
            public void ready() {
//                mTitle.setText("GPS is ready");
                attempt.ready();
            }
            @Override
            public void error(int reason) {
                attempt.error(Geotracer.PREPARE_ERROR_GPS_DISABLED);
            }
        });
    }


    private String sCoordinates;
    private int iStaleTimer;
    private long tLastFix;

    @Override
    public void start() {

        //
        mTitle.setText("Acquiring GPS fix...");

        // subscribe to location events
        startEncoder(new IpcSubscriber<Location>() {
            @Override
            public void event(Location location, EventDetails eventDetails) {

                // cancel stale timer
                Timeout.clearTimeout(iStaleTimer);

                // set last fix to now
                tLastFix = SystemClock.elapsedRealtime();

                // get latitude/longitude
                String latitude = Location.convert(location.getLatitude(), Location.FORMAT_SECONDS);
                String longitude = Location.convert(location.getLongitude(), Location.FORMAT_SECONDS);

                // replace characters
                int iFirstColon = latitude.indexOf(":");
                int iSecondColon = latitude.indexOf(":", iFirstColon + 1);
                int iDecimal = latitude.lastIndexOf(".") + 3;
                latitude = latitude.substring(0, iFirstColon) + "°" + latitude.substring(iFirstColon + 1, iSecondColon) + "'" + latitude.substring(iSecondColon + 1, iDecimal) + "\"";
                iFirstColon = longitude.indexOf(":");
                iSecondColon = longitude.indexOf(":", iFirstColon + 1);
                iDecimal = longitude.lastIndexOf(".") + 3;
                longitude = longitude.substring(0, iFirstColon) + "°" + longitude.substring(iFirstColon + 1, iSecondColon) + "'" + longitude.substring(iSecondColon + 1, iDecimal) + "\"";

                // store to variable
                sCoordinates = latitude + ", " + longitude;

                // commit to text view
                mTitle.setText(sCoordinates);

                // update gps icon
                mTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_location_found, 0, 0, 0);

                // start timer for marking stale gps fixes
                iStaleTimer = Timeout.setTimeout(mStaleGpsRunner, T_STALE_GPS_DURATION, mActivity);
            }
        });
    }

    @Override
    public void prepareToStop() {

        // let user know what we are waiting for
        mTitle.setText("Waiting for data loggers...");
    }

    @Override
    public void stop(final Attempt attempt) {

        // cancel stale timer
        Timeout.clearTimeout(iStaleTimer);

        // now we are shutting down
        mTitle.setText("Closing log file...");

        // forward request to encoder
        stopEncoder(new Attempt() {
            @Override
            public void ready() {

                // reset title
                resetText("title");

                // update gps icon
                mTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_location_searching, 0, 0, 0);

                // all done!
                attempt.ready();
            }
        });
    }
}
