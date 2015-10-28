package net.blurcast.tracer.interfaces.helper;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;

import net.blurcast.tracer.app.Geotracer;
import net.blurcast.tracer.app.ServiceUiHelper;
import net.blurcast.tracer.callback.IpcSubscriber;
import net.blurcast.tracer.callback.Subscriber;

/**
 * Created by blake on 1/8/15.
 */
public class Offspring {

    private ServiceUiHelper mService;
    private Proxy mProxy;
    private Activity mActivity;

    public static interface Proxy {
        public Activity getActivity();
        public void init();
        public void setup();
        public void subscribe(IpcSubscriber subscriber);
    }


    public Offspring(Proxy proxy) {
        mProxy = proxy;
        mActivity = proxy.getActivity();

        //
        mService = ServiceUiHelper.getInstance(mActivity);
        Log.e("Offspring", "Offspring attempting to connect to service");
//        mService.connect();
    }

    public void ready() {
        mProxy.init();
        mProxy.setup();
    }


    // helper functions

    //
    public void subscribe(IpcSubscriber subscriber) {
        Intent intent = mActivity.getIntent();
        int encoderId = intent.getIntExtra(Geotracer.INTENT_ENCODER_ID, -1);
        String eventTypeClassName = intent.getStringExtra(Geotracer.INTENT_EVENT_TYPE);
        try {
            Class<? extends Parcelable> eventTypeClass = (Class<? extends Parcelable>) Class.forName(eventTypeClassName);
            mService.subscribe(encoderId, eventTypeClass, subscriber);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
