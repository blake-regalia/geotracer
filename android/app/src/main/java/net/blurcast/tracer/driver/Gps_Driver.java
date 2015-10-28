package net.blurcast.tracer.driver;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import net.blurcast.android.util.Timeout;
import net.blurcast.tracer.app.Geotracer;
import net.blurcast.tracer.callback.Attempt;
import net.blurcast.tracer.callback.AttemptQueue;
import net.blurcast.tracer.callback.EventDetails;
import net.blurcast.tracer.callback.Subscriber;
import net.blurcast.tracer.callback.SubscriberSet;

/**
 * Created by blake on 10/8/14.
 */
public class Gps_Driver {

    // constants
    private static final String TAG = Gps_Driver.class.getSimpleName();
    private static final long GPS_MINIMUM_TIME_GAP = 0L;
    private static final float GPS_MINIMUM_DISTANCE = 0.f;

    // public constants
    public static final String ERR_DISABLED = "gps-disabled";
    public static final String STATUS_OUT_OF_SERVICE = "gps-out-of-service";
    public static final String STATUS_TEMPORARILY_UNAVAILABLE = "gps-temporarily-unavailable";
    public static final String STATUS_AVAILABLE = "gps-available";

    // primitive data-types
    private boolean bActive = false;
    private int iGpsEventTimeout = -1;

    // resources
    private Context mContext;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private GpsStatus.Listener mGpsStatusListener;
    private EventDetails oGpsStatusEventDetails = new EventDetails();

    // data structures
    private AttemptQueue mEnableAttempts = new AttemptQueue();
    private SubscriberSet<Location> mLocationSubscriberSet = new SubscriberSet<Location>();
    private SubscriberSet<GpsStatus> mGpsStatusSubscriberSet = new SubscriberSet<GpsStatus>();

    // static resources
    private static Gps_Driver mInstance;
    private static int bGpsToggleExploitAvailable = -1;


    // single-instance interfacing
    public static Gps_Driver getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new Gps_Driver(context);
        }
        return mInstance;
    }


    // constructor
    public Gps_Driver(Context context) {
        mContext = context;
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // prepare a location listener to request updates with
        mLocationListener = new LocationListener() {

            // location update event
            public void onLocationChanged(Location location) {

                // forward event information to subscribers
                mLocationSubscriberSet.event(location, null);
            }

            // gps status change
            public void onStatusChanged(String provider, int status, Bundle extras) {
                switch(status) {
                    case LocationProvider.OUT_OF_SERVICE:
                        mGpsStatusSubscriberSet.error(STATUS_OUT_OF_SERVICE);
                        break;
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        mGpsStatusSubscriberSet.error(STATUS_TEMPORARILY_UNAVAILABLE);
                        break;
                    case LocationProvider.AVAILABLE:
                        mGpsStatusSubscriberSet.error(STATUS_AVAILABLE);
                        break;
                    default:
                        mGpsStatusSubscriberSet.error(null);
                        break;
                }
            }

            // don't care about this
            public void onProviderEnabled(String provider) {}

            // this is bad news
            public void onProviderDisabled(String provider) {

                // cancel location updates
                mLocationManager.removeUpdates(mLocationListener);

                // propagate error to subscriber(s)
                mLocationSubscriberSet.error(ERR_DISABLED);

                // remove all subscribers
                mLocationSubscriberSet.empty();
            }
        };

        // prepare status listener to request updates with
        mGpsStatusListener = new GpsStatus.Listener() {
            public void onGpsStatusChanged(int eventId) {

                // satellites!
                if(eventId == GpsStatus.GPS_EVENT_SATELLITE_STATUS || eventId == GpsStatus.GPS_EVENT_FIRST_FIX) {

                    // stop the clock
                    oGpsStatusEventDetails.endTimeSpan();

                    // fetch gps status from location manager
                    GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);

                    // send to subscribers
                    mGpsStatusSubscriberSet.event(gpsStatus, oGpsStatusEventDetails);

                    // reset event clock
                    oGpsStatusEventDetails.reset();
                }
            }
        };

        // check for exploit availability yet
        if(bGpsToggleExploitAvailable == -1) bGpsToggleExploitAvailable = checkGpsToggleExploitAvailability();

    }


    public boolean isGpsEnabled() {
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Attempt to enable gps programatically
     * @param attempt
     */
    public void enableGps(final Attempt attempt) {

        // gps is already enabled
        if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            attempt.ready();
        }
        // gps is not yet enabled
        else {

            // create attempt that triggers original attempt 'ready' as soon gps is enabled
            final Attempt attemptWaitEnable = new Attempt() {

                @Override
                public void ready() {
                    attempt.ready();
                }

                // gps cannot be enabled programatically
                @Override
                public void error(int reason) {

                    // wait for gps to be enabled
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0.f, new LocationListener() {
                        public void onLocationChanged(Location location) {}
                        public void onStatusChanged(String s, int i, Bundle bundle) {}
                        public void onProviderEnabled(String provider) {
                            mLocationManager.removeUpdates(this);
                            attempt.ready();
                        }
                        public void onProviderDisabled(String provider) {}
                    });

                    // notify original method attempting
                    attempt.error(Geotracer.PREPARE_ERROR_GPS_DISABLED);
                }
            };

            // toggle exploit available
            if(bGpsToggleExploitAvailable == 1) {

                // attempt to use exploit
                attemptGpsToggleExploit(attemptWaitEnable);
            }
            // toggle exploit not available
            else {
                // TODO: consider enabling for rooted device

                // fail
                attemptWaitEnable.error();
            }
        }
    }

    /**
     * Request to be notified of location update events
     * @param locationSubscriber
     */
    public void requestUpdates(Subscriber<Location> locationSubscriber, Subscriber<GpsStatus> gpsStatusSubscriber) {

        // add subcribers to their respective sets
        mLocationSubscriberSet.add(locationSubscriber);
        mGpsStatusSubscriberSet.add(gpsStatusSubscriber);

        // not receiving updates yet
        if(!bActive) {

            // declare that we are now
            bActive = true;

            // request location updates
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MINIMUM_TIME_GAP, GPS_MINIMUM_DISTANCE, mLocationListener);

            // start event details clock
            oGpsStatusEventDetails.reset();

            // request gps status updates
            mLocationManager.addGpsStatusListener(mGpsStatusListener);
        }
    }


    public void cancelUpdates(Subscriber<Location> locationSubscriber, Subscriber<GpsStatus> gpsStatusSubscriber) {

        // remove gps status subscriber
        mGpsStatusSubscriberSet.remove(gpsStatusSubscriber);

        // remove location subscriber from our set, returns true if empty now
        if(mLocationSubscriberSet.remove(locationSubscriber)) {

            // indeed we are requesting updates
            if(bActive) {

                // declare that we are not requesting updates anymore
                bActive = false;

                // cancel updates
                mLocationManager.removeUpdates(mLocationListener);
                mLocationManager.removeGpsStatusListener(mGpsStatusListener);
            }
        }
    }



    /**
     * Checks if the gps toggle exploit is available
     * @return 1 if available, 0 otherwise
     */
    private byte checkGpsToggleExploitAvailability() {

        PackageManager pacman = mContext.getPackageManager();
        PackageInfo pacInfo = null;

        try {
            pacInfo = pacman.getPackageInfo("com.android.settings", PackageManager.GET_RECEIVERS);
        } catch (PackageManager.NameNotFoundException e) {
            // package not found
            return 0;
        }

        if (pacInfo != null) {
            for (ActivityInfo actInfo : pacInfo.receivers) {
                // test if receiver is exported. if so, we can toggle PROVIDER_GPS
                if (actInfo.name.equals("com.android.settings.widget.SettingsAppWidgetProvider") && actInfo.exported) {
                    return 1;
                }
            }
        }

        return 0;
    }


    /**
     * Attempt to use gps toggle exploit
     * @param attempt
     */
    private void attemptGpsToggleExploit(final Attempt attempt) {

        // notify attempt-er that we're trying
        attempt.progress(1);

        // clear previous timeout if any
        Timeout.clearTimeout(iGpsEventTimeout);

        // exploit a bug in the power manager widget
        final Intent poke = new Intent();
        poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider"); //$NON-NLS-1$//$NON-NLS-2$
        poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
        poke.setData(Uri.parse("3")); //$NON-NLS-1$
        mContext.sendBroadcast(poke);

        // sit & wait for event, otherwise trigger backup safety time-out
        iGpsEventTimeout = Timeout.setTimeout(new Runnable() {
            public void run() {
                iGpsEventTimeout = -1;
                bGpsToggleExploitAvailable = 0;
                enableGps(attempt);
            }
        }, 5000);
    }




}
