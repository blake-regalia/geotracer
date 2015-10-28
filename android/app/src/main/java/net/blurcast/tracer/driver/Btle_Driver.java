package net.blurcast.tracer.driver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import net.blurcast.tracer.callback.Attempt;
import net.blurcast.tracer.callback.EventDetails;
import net.blurcast.tracer.callback.Subscriber;
import net.blurcast.tracer.callback.SubscriberSet;

/**
 * Created by blake on 12/28/14.
 */
public class Btle_Driver {

    private static final String TAG = Btle_Driver.class.getSimpleName();

    public static final String DATA_DEVICE = "device";
    public static final String DATA_RSSI = "rssi";
    public static final String DATA_SCAN_RECORD = "scanRecord";

    private static Btle_Driver mInstance;
    public static Btle_Driver getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new Btle_Driver(context);
        }
        return mInstance;
    }


    private Context mContext;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothAdapter.LeScanCallback mLeScanListener;

    private boolean bActive = false;
    private SubscriberSet<Bundle> mSubscribers = new SubscriberSet<Bundle>();

    private Btle_Driver(Context context) {
        mContext = context;
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        // prepare a listener for bluetooth le scan events
        mLeScanListener = new BluetoothAdapter.LeScanCallback() {
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                Bundle bundle = new Bundle(3);
                bundle.putParcelable(DATA_DEVICE, device);
                bundle.putInt(DATA_RSSI, rssi);
                bundle.putByteArray(DATA_SCAN_RECORD, scanRecord);
                mSubscribers.event(bundle, new EventDetails());
            }
        };
    }

    public boolean isEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    public void enable(final Attempt attempt) {

        // bluetooth already powered on
        if(mBluetoothAdapter.isEnabled()) {
            attempt.ready();
        }
        // need to power on bluetooth
        else {
            // prepare listener
            BroadcastReceiver powerOnListener = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    switch(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {

                        // bluetooth hardware enabled
                        case BluetoothAdapter.STATE_ON:

                            // notify attempter
                            attempt.ready();

                            // remove this listener
                            mContext.unregisterReceiver(this);
                            break;

                        // bluetooth hardware enabling
                        case BluetoothAdapter.STATE_TURNING_ON:
                            attempt.progress(1);
                            break;

                        // something else went wrong
                        default:
                            attempt.error();
                            break;
                    }
                }
            };

            // listen for bluetooth state changes
            mContext.registerReceiver(powerOnListener, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

            // attempt to enable, it cannot be enabled right now
            if(!mBluetoothAdapter.enable()) {
                mContext.unregisterReceiver(powerOnListener);
                attempt.error();
            }
            // otherwise: bluetooth will be enabled soon
        }
    }

    public void scan(Subscriber<Bundle> subscriber) {

        // add subscriber to set
        mSubscribers.add(subscriber);

        // not scanning yet
        if(!bActive) {

            // declare we are scanning now
            bActive = true;

            // start le scanning
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    mBluetoothAdapter.startLeScan(mLeScanListener);
                }
            });
        }
    }

    public void cancel(Subscriber<Bundle> subscriber) {

        // remove subscriber, they were the last one
        if(mSubscribers.remove(subscriber)) {

            // stop scanning
            mBluetoothAdapter.stopLeScan(mLeScanListener);

            // declare we are done now
            bActive = false;
        }
    }

}
