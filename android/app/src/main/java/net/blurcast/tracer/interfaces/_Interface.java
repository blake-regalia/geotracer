package net.blurcast.tracer.interfaces;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.provider.Settings;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import net.blurcast.android.util.Counter;
import net.blurcast.tracer.app.Activity_Main;
import net.blurcast.tracer.app.Geotracer;
import net.blurcast.tracer.app.ServiceUiHelper;
import net.blurcast.tracer.callback.Attempt;
import net.blurcast.tracer.callback.Expectation;
import net.blurcast.tracer.callback.IpcSubscriber;
import net.blurcast.tracer.encoder._Encoder;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by blake on 12/28/14.
 */
public abstract class _Interface<EncoderType extends _Encoder, EventType extends Parcelable> {

    private static final String TAG = _Interface.class.getSimpleName();

    protected String sResourceKey;
    protected String sName;
    protected Activity mActivity;
    protected Context mContext;
    private Class<EncoderType> mEncoderClass;
    private Class<EventType> mEventType;
    private boolean bEnabled = false;

    protected int iEncoderId;

    protected ServiceUiHelper mService;

    protected HashMap<String, View> mViews = new HashMap<String, View>();
    protected HashMap<String, String> mStrings = new HashMap<String, String>();

    public _Interface(String name, String resourceKey, Class<EncoderType> encoderClass, Class<EventType> eventType) {
        sResourceKey = resourceKey;
        sName = name;
        mEncoderClass = encoderClass;
        mEventType = eventType;
    }

    // getters for constants defined by interface
    public final String getResourceKey() {
        return sResourceKey;
    }
    public final String getTitle() {
        return sName;
    }

    // setters for resources defined by xml layouts
    public final void putView(String key, View view) {
        mViews.put(key, view);
    }
    public final void putString(String key, String value) {
        mStrings.put(key, value);
    }

    // getters for subclass instances
    protected final View getView(String key) {
        return mViews.get(key);
    }
    protected final TextView getTextView(String key) {
        return (TextView) mViews.get(key);
    }
    protected final Switch getSwitch(String key) {
        return (Switch) mViews.get(key);
    }
    protected final String getString(String key) {
        return mStrings.get(key);
    }

    // binds the context to this instance, allows subclasses to set values on views and interact with user
    public final void bind(ServiceUiHelper service) {
        mService = service;
        mActivity = service.getActivity();
        mContext = mActivity.getApplicationContext();
        onBind();
    }

    // attempts to enable the interface
    public final void enable() {
        final Switch toggle = this.getSwitch("toggle");
        checkAvailability(new Attempt() {

            // available
            @Override
            public void ready() {

                // attempt to prepare interface
                toggle.setEnabled(false);
                _Interface.this.prepare(new Attempt() {

                    // success!
                    @Override
                    public void ready() {
                        toggle.setEnabled(true);
                        toggle.setChecked(true);
                    }

                    // interface could not be enabled
                    @Override
                    public void error(int reason) {
                        toggle.setEnabled(true);
                        toggle.setChecked(false);
                        Toast.makeText(mContext, "Failed to enable " + sName, Toast.LENGTH_LONG).show();
                    }
                });
            }

            // not available!
            @Override
            public void error(int reason) {
                toggle.setEnabled(false);
                toggle.setChecked(false);
                bEnabled = false;
                Toast.makeText(mContext, sName+" not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // helper functions for subclass instances
    protected final void resetText(String key) {
        getTextView(key).setText(mStrings.get(key));
    }
    protected final void startOffspring(Class<? extends Activity> activityClass) {
        Intent intent = new Intent(mContext, activityClass);
        intent.putExtra(Geotracer.INTENT_ENCODER_ID, iEncoderId);
        intent.putExtra(Geotracer.INTENT_EVENT_TYPE, mEventType.getCanonicalName());
        mActivity.startActivity(intent);
    }
    protected final void startEncoder(IpcSubscriber<EventType> subscriber) {
        getSwitch("toggle").setEnabled(false);
        mService.startEncoder(iEncoderId, mEventType, subscriber);
    }
    protected final void stopEncoder(Attempt attempt) {
        mService.stopEncoder(iEncoderId, attempt);
        getSwitch("toggle").setEnabled(true);
    }

    // default functionality for override-able methods
    public void onFaceClick(View view, boolean live) {
        Toast.makeText(mContext, "Settings for " + sName, Toast.LENGTH_SHORT).show();
    }

    public void prepare(Attempt attempt) {
        attempt.ready();
    }
    public void checkAvailability(Attempt attempt) {
        attempt.ready();
    }

    // override-able methods
    protected void onBind() {}
    public abstract void start();
    public abstract void stop(Attempt attempt);


    // instantiates an encoder on behalf the subclass
    public final void createEncoder(int loggerId, final Attempt attempt) {
        if(!bEnabled) return;

        //
        mService.createEncoder(mEncoderClass, loggerId, new Expectation() {
            @Override
            public void ready(int encoderId) {
                iEncoderId = encoderId;
                attempt.ready();
            }
        });
    }

    //
    public final void onToggleSwitch(CompoundButton button, boolean b) {

        // toggling interface on
        if(!bEnabled) {

            // temporarily disable toggle
            final Switch toggle = (Switch) getSwitch("toggle");
            toggle.setEnabled(false);

            // make sure interface is prepared
            prepare(new Attempt() {

                // ready!
                @Override
                public void ready() {

                    // re-enable toggle
                    toggle.setEnabled(true);

                    // set enabled mode
                    bEnabled = true;
                }

                // interface could not be enabled
                @Override
                public void error(int reason) {

                    // set toggle off
                    toggle.setChecked(false);
                    toggle.setEnabled(true);

                    // ensure enabled mode is off
                    bEnabled = false;

                    // reason this failed
                    switch(reason) {

                        // prompt user to enable gps;
                        case Geotracer.PREPARE_ERROR_GPS_DISABLED:
                            mActivity.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), Activity_Main.RESULT_ENABLE_GPS);
                            break;

                        // needs help enabling wifi
                        case Geotracer.PREPARE_ERROR_WIFI_DISABLED:
                            Toast.makeText(mActivity, "Please enable WiFi first", Toast.LENGTH_LONG).show();
                            break;

                        // needs help enabling bluetooth
                        case Geotracer.PREPARE_ERROR_BLUETOOTH_DISABLED:
                            Toast.makeText(mActivity, "Please enable Bluetooth first", Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            });
        }
        else {
            bEnabled = b;
        }
    }

    //
    public final boolean isEnabled() {
        return bEnabled;
    }



    // for dealing with a set of interfaces without having to iterate for every method call
    public static class InterfaceSet {
        private static final String TAG = InterfaceSet.class.getSimpleName();

        private ArrayList<_Interface> mInterfaces = new ArrayList<_Interface>();
        private ServiceUiHelper mService;
        public InterfaceSet(ServiceUiHelper service) {
            mService = service;
        }
        public void add(_Interface _interface) {
            mInterfaces.add(_interface);
            _interface.bind(mService);
        }
        public void createEncoders(int loggerId, final Attempt attempt) {
            final Counter kDataEncoders = new Counter(mInterfaces.size());

            // create encoders for each interface
            for(_Interface _interface: mInterfaces) {
                _interface.createEncoder(loggerId, new Attempt() {
                    @Override
                    public void ready() {
                        if(kDataEncoders.plus()) {
                            attempt.ready();
                        }
                    }
                });
            }
        }
        public void prepare(final boolean mustBeEnabled, final Attempt attempt) {
            final Counter kDataLoggers = new Counter(mInterfaces.size());

            // prepare all data loggers
            for(final _Interface _interface: mInterfaces) {
                if(mustBeEnabled && !_interface.bEnabled) {
                    if(kDataLoggers.plus()) attempt.ready();
                    continue;
                }

                _interface.prepare(new Attempt() {
                    @Override
                    public void ready() {

                        // forcibly enable this interface
//                        _interface.bEnabled = true;
                        _interface.getSwitch("toggle").setChecked(true);

                        // once last interface is prepared
                        if(kDataLoggers.plus()) {
                            attempt.ready();
                        }
                    }
                    @Override
                    public void error(int reason) {
                        attempt.error(reason);
                        if(!mustBeEnabled && kDataLoggers.plus()) {
                            attempt.ready();
                        }
                    }
                });
            }
        }
        public void start() {
            for(_Interface _interface: mInterfaces) {
                if(_interface.bEnabled) _interface.start();
            }
        }
        public void stop(final Attempt attempt) {
            final Counter kDataLoggers = new Counter(mInterfaces.size());

            // stop all data loggers
            for(_Interface _interface: mInterfaces) {
                if(!_interface.bEnabled) {
                    if(kDataLoggers.plus()) attempt.ready();
                    continue;
                }

                _interface.stop(new Attempt() {
                    @Override
                    public void ready() {

                        // once the last interface finished
                        if (kDataLoggers.plus()) {
                            attempt.ready();
                        }
                    }
                });
            }
        }

        public void enable() {
            for(_Interface _interface: mInterfaces) {
                _interface.enable();
            }
        }
    }

}
