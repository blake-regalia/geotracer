package net.blurcast.tracer.app;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import net.blurcast.android.input.InputBinder;
import net.blurcast.tracer.R;
import net.blurcast.tracer.callback.Attempt;
import net.blurcast.tracer.callback.Expectation;
import net.blurcast.tracer.interfaces.DataInterfaces;
import net.blurcast.tracer.interfaces.Gps_Interface;
import net.blurcast.tracer.interfaces._Interface;
import net.blurcast.tracer.interfaces._Location_Interface;
import net.blurcast.tracer.logger.SdCard_Logger;
import net.blurcast.tracer.logger.Upload_Logger;
import net.blurcast.tracer.logger._Logger;

import java.util.ArrayList;

public class Activity_Main extends Activity {

    private static final String TAG = Activity_Main.class.getSimpleName();

    private Class<? extends _Logger> mLoggerType;
    private int iLogger;
    private Bundle mLoggerArgs = new Bundle();

    private Activity_Main mSelf;
    private ServiceUiHelper mService;

    private _Interface.InterfaceSet mInterfaces;
    private _Location_Interface mLocationInterface;

    public static final int RESULT_ENABLE_GPS = 0x01;

    private int bRecordingStatus = Geotracer.RECORDING_STATUS_READY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mSelf = this;

        // set logger type and args
        mLoggerType = Upload_Logger.class;
        mLoggerArgs.putString(Geotracer.ARG_LOGGER_UPLOAD_URL, "http://stko-testing.geog.ucsb.edu/blake/geotracer/upload.php");
//        mLoggerType = SdCard_Logger.class;


        // bind to the service
        mService = ServiceUiHelper.getInstance(mSelf);

        // create interfaces
        mLocationInterface = new Gps_Interface();
        mInterfaces = new _Interface.InterfaceSet(mService);

        // bind interface inputs
        bindInput();

        // enable interfaces
        mLocationInterface.enable();
        mInterfaces.enable();
    }


    private void start() {

        // set recording status
        setRecordingStatus(Geotracer.RECORDING_STATUS_STARTING);

        // location provider must be enabled and prepared first
        mLocationInterface.prepare(new Attempt() {

            // location provider is ready to start
            @SuppressWarnings("unchecked")
            @Override
            public void ready() {

                // notify data loggers to prepare
                mInterfaces.prepare(true, new Attempt() {
                    @Override
                    public void ready() {

                        // continue starting recording process
                        mSelf.createLoggers();
                    }
                    @Override
                    public void error(int reason) {
                        switch(reason) {

                            // needs help enabling wifi
                            case Geotracer.PREPARE_ERROR_WIFI_DISABLED:
                                Toast.makeText(Activity_Main.this, "Please enable WiFi first", Toast.LENGTH_LONG).show();
                                break;

                            // needs help enabling bluetooth
                            case Geotracer.PREPARE_ERROR_BLUETOOTH_DISABLED:
                                Toast.makeText(Activity_Main.this, "Please enable Bluetooth first", Toast.LENGTH_LONG).show();
                                break;
                        }
                    }
                });
            }

            // location provider needs help preparing
            @Override
            public void error(int reason) {

                // reason this failed
                if(Geotracer.PREPARE_ERROR_GPS_DISABLED == reason) {

                    // prompt user to enable gps;
                    mSelf.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), RESULT_ENABLE_GPS);
                }
            }
        });
    }


    // requests to create loggers from service
    private void createLoggers() {

        // create main logger
        mService.createLogger(mLoggerType, mLoggerArgs, new Expectation() {
            @Override
            public void ready(int logger) {

                Log.d(TAG, "Using logger #"+logger+" ");

                // store main logger
                iLogger = logger;

                // continue starting recording process
                mSelf.createEncoders();
            }
        });
    }


    // requests to create encoders from service
    private void createEncoders() {

        // use main logger for creating location encoder
        mLocationInterface.createEncoder(iLogger, new Attempt() {
            @Override
            public void ready() {

                // start the location interface
                mLocationInterface.start();

                // use main logger for creating data encoders too
                mInterfaces.createEncoders(iLogger, new Attempt() {
                    @Override
                    public void ready() {

                        // start data interfaces
                        mInterfaces.start();

                        // now we can stop
                        setRecordingStatus(Geotracer.RECORDING_STATUS_RECORDING);
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestId, int resultCode, Intent data) {
        switch(requestId) {
            case RESULT_ENABLE_GPS:

                // gps still not enabled
                if(!mLocationInterface.isPrepared()) {

                    // let the user know how important this is
                    startActivity(new Intent(this, Alert_EnableGps.class));
                }
                break;
        }
    }

    private void stop() {

        // update recording status
        setRecordingStatus(Geotracer.RECORDING_STATUS_STOPPING);

        // let location provider know we're stopping soon
        mLocationInterface.prepareToStop();

        // stop all data interfaces
        mInterfaces.stop(new Attempt() {
            @Override
            public void ready() {

                // now stop location interface
                mLocationInterface.stop(new Attempt() {
                    @Override
                    public void ready() {

                        // close main logger file
                        mService.closeLogger(iLogger, new Attempt() {
                            @Override
                            public void ready() {

                                // ready to record again!
                                setRecordingStatus(Geotracer.RECORDING_STATUS_READY);
                            }
                        });

                    }
                });
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        // (re)connect to service
        Log.i(TAG, "onStart()");
        mService.connect(new Expectation() {
            @Override
            public void ready(int recordingStatus) {
                Log.i(TAG, "Recieved recording status: "+recordingStatus);
                setRecordingStatus(recordingStatus);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
    }


    @Override
    protected void onStop() {
        super.onStop();

        Log.i(TAG, "onStop()");

        // not recording & not starting to record
        if(bRecordingStatus != Geotracer.RECORDING_STATUS_RECORDING && bRecordingStatus != Geotracer.RECORDING_STATUS_STARTING) {
            mService.disconnect();
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();

        Log.i(TAG, "onRestart()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mService.disconnect();
    }



    private ArrayList<Switch> mSwitches = new ArrayList<Switch>();

    private void bindInput() {

        InputBinder mInputBinder = new InputBinder(this);

        Resources resources = getResources();
        String idResourceType = "id";
        String stringResourceType = "string";
        String packageName = getPackageName();

        // bind all interfaces
        for(final _Interface dataInterface: DataInterfaces.getInterfaces()) {
            String sResource = "app.main.logger." + dataInterface.getResourceKey();
            int resourceIdFace = resources.getIdentifier(sResource, idResourceType, packageName);
            int resourceIdTitle = resources.getIdentifier(sResource + ".title", idResourceType, packageName);
            int resourceIdSwitch = resources.getIdentifier(sResource + ".switch", idResourceType, packageName);

            dataInterface.putView("face", mInputBinder.click(resourceIdFace, new View.OnClickListener() {
                public void onClick(View view) {
                    dataInterface.onFaceClick(view, Geotracer.RECORDING_STATUS_RECORDING == bRecordingStatus);
                }
            }));
            dataInterface.putView("title", mInputBinder.asText(resourceIdTitle));
            dataInterface.putView("toggle", mInputBinder.check(resourceIdSwitch, new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton button, boolean b) {
                    dataInterface.onToggleSwitch(button, b);
                }
            }));
            dataInterface.putString("title", getString(resources.getIdentifier(sResource + ".title", stringResourceType, packageName)));
            mInterfaces.add(dataInterface);

            mSwitches.add((Switch) findViewById(resourceIdSwitch));
        }

        // do the same for location provider
        String sLocationResource = "app.main.location." + mLocationInterface.getResourceKey();
        int locationResourceIdFace = resources.getIdentifier(sLocationResource, idResourceType, packageName);
        int locationResourceIdTitle = resources.getIdentifier(sLocationResource+".title", idResourceType, packageName);
        int locationResourceIdSwitch = resources.getIdentifier(sLocationResource+".switch", idResourceType, packageName);

        mLocationInterface.putView("face", mInputBinder.click(locationResourceIdFace, new View.OnClickListener() {
            public void onClick(View view) {
                mLocationInterface.onFaceClick(view, Geotracer.RECORDING_STATUS_RECORDING == bRecordingStatus);
            }
        }));
        mLocationInterface.putView("title", mInputBinder.asText(locationResourceIdTitle));
        mLocationInterface.putView("toggle", mInputBinder.check(locationResourceIdSwitch, new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton button, boolean b) {
                mLocationInterface.onToggleSwitch(button, b);
            }
        }));
        mLocationInterface.putString("title", getString(resources.getIdentifier(sLocationResource+".title", stringResourceType, packageName)));
        mLocationInterface.bind(mService);

        mSwitches.add((Switch) findViewById(locationResourceIdSwitch));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        mRecordButton = menu.findItem(R.id.app_main_start);

        // now we're ready to record!
        setRecordingStatus(bRecordingStatus);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.app_main_start:
                if(Geotracer.RECORDING_STATUS_READY == bRecordingStatus) {
                    start();
                }
                else if(Geotracer.RECORDING_STATUS_RECORDING == bRecordingStatus) {
                    stop();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private MenuItem mRecordButton;

    private void setRecordingStatus(int status) {
        bRecordingStatus = status;

        if(mRecordButton == null) {
            Log.e(TAG, "Record button not initialized yet");
            return;
        }

        switch(status) {
            case Geotracer.RECORDING_STATUS_READY:
                mRecordButton.setEnabled(true);
                mRecordButton.setIcon(R.drawable.fi_media_record_outline);
                break;

            case Geotracer.RECORDING_STATUS_STARTING:
                mRecordButton.setEnabled(false);
                mRecordButton.setIcon(R.drawable.fi_media_stop_outline);
                break;

            case Geotracer.RECORDING_STATUS_RECORDING:
                mRecordButton.setEnabled(true);
                mRecordButton.setIcon(R.drawable.fi_media_stop_outline);
                break;

            case Geotracer.RECORDING_STATUS_STOPPING:
                mRecordButton.setEnabled(false);
                mRecordButton.setIcon(R.drawable.fi_media_record_outline);
                break;
        }
    }
}
