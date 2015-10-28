package net.blurcast.tracer.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;

import net.blurcast.android.util.Encoder;
import net.blurcast.tracer.callback.Attempt;
import net.blurcast.tracer.callback.AttemptQueue;
import net.blurcast.tracer.callback.Attempt_GroupManager;
import net.blurcast.tracer.callback.EventDetails;
import net.blurcast.tracer.callback.Expectation;
import net.blurcast.tracer.callback.IpcSubscriber;
import net.blurcast.tracer.encoder._Encoder;
import net.blurcast.tracer.helper.ParcelableSensor;
import net.blurcast.tracer.logger._Logger;
import net.blurcast.tracer.service.MainService;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

/**
 * Created by blake on 1/6/15.
 */
public class ServiceUiHelper {

    private static final ParcelableSensor PARCELABLE_SENSOR = null;
    private static final String TAG = ServiceUiHelper.class.getSimpleName();

    private static final int STATE_UNSET   = 0x00;
    private static final int STATE_READY   = 0x01;
    private static final int STATE_BINDING = 0x01;
    private static final int STATE_BOUND   = 0x02;
    private static final int STATE_CRASH   = 0x03;
    private static final int STATE_QUIET   = 0x04;

    private int bBoundState = STATE_UNSET;

    private Queue<Message> mBackMessages = new LinkedList<Message>();

    private Messenger mOutgoing;
    private Messenger mIncoming;
    private ServiceConnection mConnection;

    private Activity mActivity;
    private Context mContext;

    private Intent mServiceBindIntent;

    private SparseArray<EncoderWrapper> mEncoderWrappers = new SparseArray<EncoderWrapper>();

    private Expectation mStatusExpectation;

    private ArrayList<Expectation> mExpectations = new ArrayList<Expectation>();
    private Attempt_GroupManager mAttempts = new Attempt_GroupManager(Geotracer.MAX_MESSAGE_OBJECTIVE+1);


    private class EncoderWrapper {
        private Stack<IpcSubscriber> mSubscribers = new Stack<IpcSubscriber>();
        private Class<? extends Parcelable> mEventType;
        public EncoderWrapper() {}
        public EncoderWrapper(IpcSubscriber subscriber, Class<? extends Parcelable> eventType) {
            this.pushSubscriber(subscriber, eventType);
        }
        public void pushSubscriber(IpcSubscriber subscriber, Class<? extends Parcelable> eventType) {
            mSubscribers.add(subscriber);
            mEventType = eventType;
        }
        public void popSubscriber() {
            mSubscribers.pop();
        }
        @SuppressWarnings("unchecked")
        public void event(Bundle data) {
            try {
                data.setClassLoader(EventDetails.class.getClassLoader());
                EventDetails eventDetails = data.getParcelable(Geotracer.EXTRA_EVENT_DETAILS);
                data.setClassLoader(mEventType.getClassLoader());
                mSubscribers.peek().event(mEventType.cast(data.getParcelable(Geotracer.EXTRA_EVENT_DATA)), eventDetails);
            } catch(ClassCastException x) {
                x.printStackTrace();
            }
        }

        @SuppressWarnings("unchecked")
        public void notice(Bundle data) {
            try {
                data.setClassLoader(Parcelable.class.getClassLoader());
                int noticeType = data.getInt(Geotracer.EXTRA_NOTICE_TYPE);
                int noticeValue = data.getInt(Geotracer.EXTRA_NOTICE_VALUE);
                Parcelable noticeData = null; //data.getParcelable(Geotracer.EXTRA_NOTICE_DATA);
                mSubscribers.peek().notice(noticeType, noticeValue, noticeData);
            } catch(ClassCastException x) {
                x.printStackTrace();
            }
        }

        @SuppressWarnings("unchecked")
        public void error(Bundle data) {
            try {
                String error = data.getString(Geotracer.EXTRA_ERROR);
                mSubscribers.peek().error(error);
            } catch(ClassCastException x) {
                x.printStackTrace();
            }
        }
    }

    private static ServiceUiHelper mInstance;
    public static ServiceUiHelper getInstance(Activity activity) {
        if(mInstance == null) {
            mInstance = new ServiceUiHelper(activity);
        }
        return mInstance;
    }

    private ServiceUiHelper(Activity activity) {

        mActivity = activity;
        mContext = activity.getApplicationContext();

        // for creating a new connection to service
        mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(TAG, "Service Connection established");
                mOutgoing = new Messenger(iBinder);

                // send messages from back queue
                while(!mBackMessages.isEmpty()) {
                    Message retry = mBackMessages.poll();

                    // try to send to service
                    try {
                        mOutgoing.send(retry);
                    } catch (RemoteException x) {
                        x.printStackTrace();
                    }
                }

                // now we are bound
                bBoundState = STATE_BOUND;
            }

            public void onServiceDisconnected(ComponentName componentName) {
                Log.w(TAG, "Service Disconnected");
                mOutgoing = null;
                bBoundState = STATE_CRASH;
            }
        };

        // for handling incoming messages from service
        mIncoming = new Messenger(new Handler() {
            @Override
            public void handleMessage(Message message) {
                if(bBoundState != STATE_BOUND) return;
                switch(message.what) {

                    // response to recording status request
                    case Geotracer.MESSAGE_OBJECTIVE_RECORDING_STATUS:
                        if(mStatusExpectation != null) {
                            mStatusExpectation.ready(message.arg2);
                        }
                        break;

                    // sensor-related event
                    case Geotracer.MESSAGE_TYPE_DATA:
                        mEncoderWrappers.get(message.arg1).event(message.getData());
                        break;

                    case Geotracer.MESSAGE_TYPE_ERROR:
                        mEncoderWrappers.get(message.arg1).error(message.getData());
                        break;

                    case Geotracer.MESSAGE_TYPE_NOTICE:
                        mEncoderWrappers.get(message.arg1).notice(message.getData());
                        break;

                    // response to request for creating logger / creating encoder
                    case Geotracer.MESSAGE_OBJECTIVE_LOGGER_CREATE:
                    case Geotracer.MESSAGE_OBJECTIVE_ENCODER_CREATE:
                    case Geotracer.MESSAGE_OBJECTIVE_ENCODER_SUBSCRIBE:

                        // get request id
                        int requestId = message.getData().getInt(Geotracer.EXTRA_REQUEST_ID);

                        // depending on the message
                        switch (message.arg1) {

                            // success
                            case Geotracer.OBJECTIVE_SUCCESS:

                                // pop first expectation and send result id
                                mExpectations.get(requestId).ready(message.arg2);
                                break;

                            // failure
                            case Geotracer.OBJECTIVE_FAILURE:
                                mExpectations.get(requestId).error(message.arg2);
                                break;
                        }
                        break;

                    // response to request for closing logger
                    case Geotracer.MESSAGE_OBJECTIVE_ENCODER_STOP:
                    case Geotracer.MESSAGE_OBJECTIVE_LOGGER_CLOSE:

                        // fetch attempt queue
                        AttemptQueue attemptQueue = mAttempts.fetch(message.arg2, message.what);

                        //
                        switch (message.arg1) {

                            // success
                            case Geotracer.OBJECTIVE_SUCCESS:
                                attemptQueue.ready();
                                break;

                            // failure
                            case Geotracer.OBJECTIVE_FAILURE:
                                String error = message.getData().getString(Geotracer.EXTRA_ERROR);
                                attemptQueue.error(error);
                                break;
                        }
                        break;
                }
            }
        });

        // service bind intent
        mServiceBindIntent = new Intent(mActivity, MainService.class);

        // ready for binding
        bBoundState = STATE_READY;
    }


    public void connect(Expectation expectation) {
        if(bBoundState == STATE_READY) {
            mContext.bindService(mServiceBindIntent, mConnection, Context.BIND_AUTO_CREATE);
            bBoundState = STATE_BINDING;
        }
        getRecordingStatus(expectation);
    }

    public void disconnect() {
        if(bBoundState == STATE_BOUND || bBoundState == STATE_QUIET) {
            mContext.unbindService(mConnection);
            bBoundState = STATE_READY;
        }
    }


    // fetch recording status from service
    public void getRecordingStatus(Expectation expectation) {

        // store this recording status request
        mStatusExpectation = expectation;

        // send request
        send(Geotracer.MESSAGE_OBJECTIVE_RECORDING_STATUS, null);
    }


    // requests to create a new logger
    public void createLogger(Class<? extends _Logger> loggerClass, Bundle args, Expectation expectation) {

        // load
        Bundle data = new Bundle();
        data.putString(Geotracer.EXTRA_LOGGER_CLASS, loggerClass.getCanonicalName());
        data.putInt(Geotracer.EXTRA_REQUEST_ID, mExpectations.size());
        data.putBundle(Geotracer.EXTRA_LOGGER_ARGS, args);

        // store this logger creation request
        mExpectations.add(expectation);

        // send data
        send(Geotracer.MESSAGE_OBJECTIVE_LOGGER_CREATE, data);
    }


    // requests to create an encoder
    public void createEncoder(Class<? extends _Encoder> encoderClass, int loggerId, final Expectation expectation) {

        // load data bundle with necessary information
        Bundle data = new Bundle();
        data.putInt(Geotracer.EXTRA_INPUT_ID, loggerId);
        data.putString(Geotracer.EXTRA_ENCODER_CLASS, encoderClass.getCanonicalName());
        data.putInt(Geotracer.EXTRA_REQUEST_ID, mExpectations.size());

        // encoder creation request
        mExpectations.add(new Expectation() {
            @Override
            public void ready(int encoderId) {
                mEncoderWrappers.put(encoderId, new EncoderWrapper());
                expectation.ready(encoderId);
            }
        });

        // send data
        send(Geotracer.MESSAGE_OBJECTIVE_ENCODER_CREATE, data);
    }

    // requests to start encoder
    public void startEncoder(int encoderId) {

        // send raw message
        send(Geotracer.MESSAGE_OBJECTIVE_ENCODER_START, encoderId, 0);
    }


    // requests to start encoder with curious subscriber
    public void startEncoder(int encoderId,  Class<? extends Parcelable> eventType, IpcSubscriber subscriber) {

        // fetch this curious subscriber
        mEncoderWrappers.get(encoderId).pushSubscriber(subscriber, eventType);

        Log.d(TAG, "[] EncoderWrapper["+encoderId+"] => "+eventType.getSimpleName());

        // start an encoder and send updates
        send(Geotracer.MESSAGE_OBJECTIVE_ENCODER_START, encoderId, Geotracer.MESSAGE_ARG_REQUEST_OPTION);
    }

    // subscribes to an existing encoder
    public void subscribe(Class<? extends Encoder> encoderClass, final Class<? extends Parcelable> eventType, final IpcSubscriber subscriber) {

        // find the encoder by name
        mExpectations.add(new Expectation() {
            @Override
            public void ready(int encoderId) {
                mEncoderWrappers.put(encoderId, new EncoderWrapper(subscriber, eventType));
            }
        });

        Bundle data = new Bundle();
        data.putString(Geotracer.EXTRA_ENCODER_CLASS, encoderClass.getCanonicalName());
        data.putInt(Geotracer.EXTRA_REQUEST_ID, mExpectations.size());

        // request to subscribe to encoder by name
        send(Geotracer.MESSAGE_OBJECTIVE_ENCODER_SUBSCRIBE, data);
    }

    // subscribes to an existing encoder
    public void subscribe(int encoderId, final Class<? extends Parcelable> eventType, final IpcSubscriber subscriber) {

        //
        mEncoderWrappers.put(encoderId, new EncoderWrapper(subscriber, eventType));

        //
        Bundle data = new Bundle();
        data.putInt(Geotracer.EXTRA_REQUEST_ID, mExpectations.size());

        // request to subscribe to encoder by id
        send(Geotracer.MESSAGE_OBJECTIVE_ENCODER_SUBSCRIBE, encoderId, 0);
    }

    // requests to stop encoder
    public void stopEncoder(int encoderId, Attempt attempt) {

        //
        mAttempts.add(encoderId, Geotracer.MESSAGE_OBJECTIVE_ENCODER_STOP, attempt);

        // post to stop this encoder
        send(Geotracer.MESSAGE_OBJECTIVE_ENCODER_STOP, encoderId, Geotracer.MESSAGE_ARG_REQUEST_OPTION);
    }

    // requests to close logger
    public void closeLogger(int loggerId, Attempt attempt) {

        //
        mAttempts.add(loggerId, Geotracer.MESSAGE_OBJECTIVE_LOGGER_CLOSE, attempt);

        // send request
        send(Geotracer.MESSAGE_OBJECTIVE_LOGGER_CLOSE, loggerId, Geotracer.MESSAGE_ARG_REQUEST_OPTION);
    }

    // requests to close logger with a log file suffix
    public void closeLogger(int loggerId, String suffix, Attempt attempt) {

        //
        mAttempts.add(loggerId, Geotracer.MESSAGE_OBJECTIVE_LOGGER_CLOSE, attempt);

        // add data
        Bundle data = new Bundle();
        data.putString(Geotracer.EXTRA_LOGGER_FILE_SUFFIX, suffix);

        // send request
        send(Geotracer.MESSAGE_OBJECTIVE_LOGGER_CLOSE, data, loggerId, Geotracer.MESSAGE_ARG_REQUEST_OPTION);
    }



    // send with input and flag
    private void send(int objectiveId, int inputId, int flag) {

        // send simple message
        send(Message.obtain(null, objectiveId, inputId, flag));
    }

    // send with data
    private void send(int objectiveId, Bundle data) {

        // create a message
        Message message = Message.obtain(null, objectiveId);
        message.setData(data);

        // send it!
        send(message);
    }

    // send with input and flag
    private void send(int objectiveId, Bundle data, int inputId, int flag) {

        // create message
        Message message = Message.obtain(null, objectiveId, inputId, flag);
        message.setData(data);

        // send it
        send(message);
    }

    // send message physical
    private void send(Message message) {

        // reply to incoming messenger
        message.replyTo = mIncoming;

        // service is not bound yet
        if(bBoundState != STATE_BOUND) {

            // push message to back-queue
            mBackMessages.add(message);
        }
        // service is bound!
        else {

            // try to send to service
            try {
                mOutgoing.send(message);
            } catch (RemoteException x) {
                x.printStackTrace();
            }
        }
    }



    public Activity getActivity() {
        return mActivity;
    }

}
