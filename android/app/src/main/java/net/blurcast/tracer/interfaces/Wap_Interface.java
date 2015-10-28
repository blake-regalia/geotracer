package net.blurcast.tracer.interfaces;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import net.blurcast.tracer.app.Geotracer;
import net.blurcast.tracer.callback.Attempt;
import net.blurcast.tracer.callback.EventDetails;
import net.blurcast.tracer.callback.IpcSubscriber;
import net.blurcast.tracer.driver.Wifi_Driver;
import net.blurcast.tracer.encoder.Wap_Encoder;
import net.blurcast.tracer.helper.ParcelableScanResultList;

/**
 * Created by blake on 12/28/14.
 */
public class Wap_Interface extends _Interface<Wap_Encoder, ParcelableScanResultList> {

    private TextView mTitle;
    private String sTitle;
    private Wifi_Driver mWifi;

    public Wap_Interface() {
        super("WiFi Access Points", "wap", Wap_Encoder.class, ParcelableScanResultList.class);
    }

    @Override
    protected void onBind() {
        mTitle = getTextView("title");
        sTitle = getString("title");
        this.resetText("title");
        mWifi = Wifi_Driver.getInstance(mContext);
    }

    @Override
    public void onFaceClick(View view, boolean live) {

    }

    @Override
    public void prepare(final Attempt attempt) {
        Wifi_Driver.getInstance(mContext).enable(new Attempt() {
            @Override
            public void ready() {
                attempt.ready();
            }
            @Override
            public void error(int reason) {
                attempt.error(Geotracer.PREPARE_ERROR_WIFI_DISABLED);
            }
        });
    }

    @Override
    public void start() {

        // set warm-up text
        mTitle.setText("Scanning for WAPs...");

        // fire off the encoder
        startEncoder(new IpcSubscriber<ParcelableScanResultList>() {
            @Override
            public void event(ParcelableScanResultList eventData, EventDetails eventDetails) {
                mTitle.setText(eventData.size() + " wap(s) detected");
            }
        });
    }

    @Override
    public void stop(final Attempt attempt) {

        stopEncoder(new Attempt() {
            @Override
            public void ready() {

                // notify done
                attempt.ready();

                // reset title
                resetText("title");
            }
        });
    }

}
