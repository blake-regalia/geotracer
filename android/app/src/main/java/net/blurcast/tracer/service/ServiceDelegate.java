package net.blurcast.tracer.service;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

import net.blurcast.tracer.app.Geotracer;
import net.blurcast.tracer.callback.Attempt;
import net.blurcast.tracer.callback.EventDetails;
import net.blurcast.tracer.callback.IpcSubscriber;
import net.blurcast.tracer.encoder._Encoder;
import net.blurcast.tracer.logger._Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by blake on 1/6/15.
 */
public class ServiceDelegate extends Handler {

    private static final String TAG = ServiceDelegate.class.getSimpleName();

    private Context mContext;
    private MainService mService;

    public ServiceDelegate(MainService service, Looper looper) {
        super(looper);
        mService = service;
        mContext = service.getApplicationContext();
    }


    @Override
    public void handleMessage(Message message) {

        // reference the data stored by this message
        Bundle data = message.getData();

        // reference input id
        final int inputId = message.arg1;

//        Log.i(TAG, "Received message["+message.arg1+"] => "+message.what);

        // depending on the objective of the message
        switch(message.what) {

            // get recording status
            case Geotracer.MESSAGE_OBJECTIVE_RECORDING_STATUS:
                success(message, mService.getRecordingStatus());
                break;

            // create a logger
            case Geotracer.MESSAGE_OBJECTIVE_LOGGER_CREATE:
                try {

                    // get class name
                    String className = data.getString(Geotracer.EXTRA_LOGGER_CLASS);

                    // reference class object
                    Class loggerClass = Class.forName(className);

                    // fetch constructors
                    Constructor constructor = loggerClass.getConstructors()[0]; //(Context.class, Bundle.class);

                    // fetch args bundle
                    Bundle args = data.getBundle(Geotracer.EXTRA_LOGGER_ARGS);

                    // instantiate logger
                    _Logger logger = (_Logger) constructor.newInstance(mContext, args);

                    // feed logger to service, get logger id
                    int loggerId = mService.addLogger(logger);

                    Log.d(TAG, "Constructed new _Logger: "+loggerId);

                    // send logger id back to activity
                    success(message, loggerId);

//                } catch(NoSuchMethodException x) {
//                    x.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch(IndexOutOfBoundsException x) {
                    x.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;


            // create an encoder
            case Geotracer.MESSAGE_OBJECTIVE_ENCODER_CREATE:

                try {
                    // get class name
                    String className = data.getString(Geotracer.EXTRA_ENCODER_CLASS);

                    // reference class object
                    Class encoderClass = Class.forName(className);

                    // fetch constructor
                    Constructor constructor = encoderClass.getConstructors()[0];

                    // fetch logger id
                    int loggerId = data.getInt(Geotracer.EXTRA_INPUT_ID);

                    // get logger
                    _Logger logger = mService.getLogger(loggerId);

                    // instantiate encoder
                    _Encoder encoder = (_Encoder) constructor.newInstance(mContext, logger);

                    // feed encoder to service, get encoder id
                    int encoderId = mService.addEncoder(encoder, className);

                    //
                    Log.d(TAG, "Constructed new _Encoder [#"+encoderId+"]: "+encoder+" using Logger #"+loggerId);

                    // send encoder id back to activity
                    success(message, encoderId);

                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch(IndexOutOfBoundsException x) {
                    x.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;


            //
            case Geotracer.MESSAGE_OBJECTIVE_ENCODER_START:

                // requester wants to receive updates
                if(Geotracer.MESSAGE_ARG_REQUEST_OPTION == message.arg2) {

                    // establish a final connection to the reply address
                    final Messenger replyTo = message.replyTo;

                    // start the encoder
                    mService.getEncoder(inputId).start(new IpcSubscriber() {
                        @Override
                        public void event(Parcelable eventData, EventDetails eventDetails) {
                            Bundle response = new Bundle();
                            response.putParcelable(Geotracer.EXTRA_EVENT_DATA, eventData);
                            response.putParcelable(Geotracer.EXTRA_EVENT_DETAILS, eventDetails);
                            data(replyTo, inputId, response);
                        }

                        @Override
                        public void notice(int noticeType, int noticeValue, Parcelable data) {
                            Bundle response = new Bundle();
                            response.putInt(Geotracer.EXTRA_NOTICE_TYPE, noticeType);
                            response.putInt(Geotracer.EXTRA_NOTICE_VALUE, noticeValue);
                            response.putParcelable(Geotracer.EXTRA_NOTICE_DATA, data);
//                            noticefy(replyTo, inputId, response);
                        }

                        @Override
                        public void error(String error) {
                            Bundle response = new Bundle();
                            response.putString(Geotracer.EXTRA_ERROR, error);
                            err(replyTo, inputId, response);
                        }
                    });
                }
                //
                else {
                    mService.getEncoder(inputId).start();
                }
                break;

            case Geotracer.MESSAGE_OBJECTIVE_ENCODER_STOP: {
                    // non-immediate success callbacks
                    final Messenger replyTo = message.replyTo;
                    final int objectiveId = message.what;
                    final int requestId = data.getInt(Geotracer.EXTRA_REQUEST_ID, -1);

                    //
                    mService.getEncoder(inputId).stop(new Attempt() {
                        @Override
                        public void ready() {
                            Log.w(TAG, "Stopped Encoder "+mService.getEncoder(inputId).getClass().getSimpleName());
                            mService.removeEncoder(inputId);
                            success(replyTo, objectiveId, requestId, inputId);
                            }
                    });
            } break;

            case Geotracer.MESSAGE_OBJECTIVE_LOGGER_CLOSE:
                mService.closeLogger(inputId, data);
                success(message, inputId);
                break;

            case Geotracer.MESSAGE_OBJECTIVE_ENCODER_SUBSCRIBE: {
//                String encoderClassName = data.getString(Geotracer.EXTRA_ENCODER_CLASS);
//                int requestId = data.getInt(Geotracer.EXTRA_REQUEST_ID, -1);
//
//                int encoderId = mService.lookupEncoder(encoderClassName);
                final Messenger replyTo = message.replyTo;

                mService.getEncoder(inputId).replaceCuriousSubscriber(new IpcSubscriber() {
                    @Override
                    public void event(Parcelable eventData, EventDetails eventDetails) {
                        Bundle response = new Bundle();
                        response.putParcelable(Geotracer.EXTRA_EVENT_DATA, eventData);
                        response.putParcelable(Geotracer.EXTRA_EVENT_DETAILS, eventDetails);
                        data(replyTo, inputId, response);
                    }

                    @Override
                    public void notice(int noticeType, int noticeValue, Parcelable data) {
                        Bundle response = new Bundle();
                        response.putInt(Geotracer.EXTRA_NOTICE_TYPE, noticeType);
                        response.putInt(Geotracer.EXTRA_NOTICE_VALUE, noticeValue);
                        response.putParcelable(Geotracer.EXTRA_NOTICE_DATA, data);
//                        noticefy(replyTo, inputId, response);
                    }

                    @Override
                    public void error(String error) {
                        Bundle response = new Bundle();
                        response.putString(Geotracer.EXTRA_ERROR, error);
                        err(replyTo, inputId, response);
                    }
                });
            } break;
        }
    }


    // wrapper for success method with result
    private void success(Message original) {
        success(original, 0);
    }

    // immediately send a success message back to sender with a result (result will get stored to message as arg2)
    private void success(Message original, int result) {

        // obtain new message using same 'what' from sender
        Message message = obtainMessage(original.what, Geotracer.OBJECTIVE_SUCCESS, result);
        message.setData(original.getData());

        // send response
        try {
            original.replyTo.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // immediately send a success message back to sender with a result
    private void success(Messenger replyTo, int objectiveId, int requestId, int resultId) {

        // obtain new message using same 'what' from sender
        Message message = obtainMessage(objectiveId, Geotracer.OBJECTIVE_SUCCESS, resultId);
        Bundle data = new Bundle();
        data.putInt(Geotracer.EXTRA_REQUEST_ID, requestId);
        message.setData(data);

        // send response
        try {
            replyTo.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    // send data message back to sender with data
    private void data(Messenger replyTo, int encoderId, Bundle data) {

        // obtain new message
        Message message = obtainMessage(Geotracer.MESSAGE_TYPE_DATA, encoderId, 0);
        message.setData(data);

        // send response
        send(replyTo, message);
    }

    // send notice message back to sender with data
    private void noticefy(Messenger replyTo, int encoderId, Bundle data) {

        // obtain new message
        Message message = obtainMessage(Geotracer.MESSAGE_TYPE_NOTICE, encoderId, 0);
        message.setData(data);

        // send response
        send(replyTo, message);
    }

    // send an error message back to sender
    private void err(Messenger replyTo, int encoderId, Bundle info) {

        // obtain new message
        Message message = obtainMessage(Geotracer.MESSAGE_TYPE_ERROR, encoderId, 0);
        message.setData(info);

        // send response
        send(replyTo, message);
    }


    //
    private void send(Messenger replyTo, Message message) {

        // send response
        try {
            replyTo.send(message);
        } catch(RemoteException e) {
            e.printStackTrace();
        }
    }

}
