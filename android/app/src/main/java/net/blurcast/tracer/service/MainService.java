package net.blurcast.tracer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.os.Process;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;

import net.blurcast.tracer.R;
import net.blurcast.tracer.app.Activity_Main;
import net.blurcast.tracer.app.Geotracer;
import net.blurcast.tracer.encoder._Encoder;
import net.blurcast.tracer.logger._Logger;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by blake on 1/6/15.
 */
public class MainService extends Service {

    private static final String TAG = MainService.class.getSimpleName();

    private Context mContext;

    private Messenger mMessenger;
    private ServiceDelegate mHandler;

    private NotificationManager mNotificationManager;
    private Notification.Builder mNotification;
    private static final int iNotification = 1;

    private Looper mServiceLooper;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.w(TAG, "onCreate()");

        mContext = this.getApplicationContext();

        // notification manager
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        // start main activity only if it is not already at top of history stack
        Intent startMainActivity = new Intent(this, Activity_Main.class);
        startMainActivity.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // primary notification
        mNotification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.earth)
                .setContentIntent(
                        PendingIntent.getActivity(this, 0, startMainActivity, PendingIntent.FLAG_UPDATE_CURRENT)
                )
                .setContentTitle("Geotracer")
                .setContentText("Ready to record");

        // this is a foreground service
        startForeground(iNotification, mNotification.build());


        // Start up the thread running the service. we create a separate thread because the service normally runs in the process's
        // main thread, which we don't want to block (Activity's UI thread). we also make it foreground priority
        HandlerThread thread = new HandlerThread("MainServiceThread", Process.THREAD_PRIORITY_FOREGROUND);

        // start thread
        thread.start();

        // Get the new thread's looper and use it for our Handler
        mServiceLooper = thread.getLooper();

        // create a new message handler
        mHandler = new ServiceDelegate(this, mServiceLooper);

        // create a messenger
        mMessenger = new Messenger(mHandler);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w(TAG, "onStartCommand()");
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.w(TAG, "onBind()");
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.w(TAG, "onUnbind()");
        return false;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy()");
    }


    private int bRecordingStatus = Geotracer.RECORDING_STATUS_READY;

    public void startRecording() {
        switch(bRecordingStatus) {
            case Geotracer.RECORDING_STATUS_STARTING:
            case Geotracer.RECORDING_STATUS_STOPPING:
                break;

            case Geotracer.RECORDING_STATUS_RECORDING:
                break;

            case Geotracer.RECORDING_STATUS_READY:
                bRecordingStatus = Geotracer.RECORDING_STATUS_RECORDING;
                updateNotification("Geotracer Running", "Recording from sensors...", true);
                break;
        }
    }

    public void stopRecording() {
        switch(bRecordingStatus) {
            case Geotracer.RECORDING_STATUS_STARTING:
            case Geotracer.RECORDING_STATUS_STOPPING:
                break;

            case Geotracer.RECORDING_STATUS_RECORDING:
                bRecordingStatus = Geotracer.RECORDING_STATUS_READY;
                updateNotification("Geotracer", "Ready to record", false);
                break;

            case Geotracer.RECORDING_STATUS_READY:
                break;
        }
    }

    // getter for recording status
    public int getRecordingStatus() {
        return bRecordingStatus;
    }


    private void updateNotification(String title, String text, boolean chronometer) {
        mNotification
                .setUsesChronometer(chronometer)
                .setContentTitle(title)
                .setContentText(text);
        mNotificationManager.notify(iNotification, mNotification.build());
    }

    private int nLoggerId = 0;
    private int nEncoderId = 0;
    public SparseArray<_Logger> mLoggers = new SparseArray<_Logger>();
    public SparseArray<Pair<_Encoder, String>> mEncoders = new SparseArray<Pair<_Encoder, String>>();
    protected HashMap<String, Integer> mEncoderMap = new HashMap<String, Integer>();

    public int addLogger(_Logger logger) {
        mLoggers.put(nLoggerId, logger);
        startRecording();
        return nLoggerId++;
    }

    public void closeLogger(int index, Bundle data) {
        mLoggers.get(index).close(data);
        mLoggers.remove(index);
        if(mLoggers.size() == 0) stopRecording();
    }

    public _Logger getLogger(int index) {
        return mLoggers.get(index);
    }

    public int lookupEncoder(String className) {
        Integer find = mEncoderMap.get(className);
        return (find == null)? -1: find;
    }

    public int addEncoder(_Encoder encoder, String className) {
        Pair<_Encoder, String> pair = new Pair<_Encoder, String>(encoder, className);
        mEncoders.put(nEncoderId, pair);
        mEncoderMap.put(className, nEncoderId);
        return nEncoderId++;
    }

    public _Encoder getEncoder(int index) {
        return mEncoders.get(index).first;
    }
    public String getEncoderClassName(int index) {
        return mEncoders.get(index).second;
    }
    public void removeEncoder(int index) {
        mEncoders.remove(index);
    }
}
