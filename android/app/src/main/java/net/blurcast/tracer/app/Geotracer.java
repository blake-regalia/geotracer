package net.blurcast.tracer.app;

/**
 * Created by blake on 1/6/15.
 */
public class Geotracer {

    public static final String EXTRA_ENCODER_CLASS = "encoder-class";

    public static final String EXTRA_LOGGER_CLASS = "logger-class";
    public static final String EXTRA_LOGGER_FILE_SUFFIX = "logger-suffix";
    public static final String EXTRA_LOGGER_ARGS = "logger-args";

    public static final String EXTRA_EVENT_DATA = "event-data";
    public static final String EXTRA_EVENT_DETAILS = "event-details";

    public static final String EXTRA_INPUT_ID  = "input-id";
    public static final String EXTRA_REQUEST_ID = "request-id";
    public static final String EXTRA_RESULT_ID = "result-id";
    public static final String EXTRA_IS_CURIOUS = "curious";

    public static final String EXTRA_NOTICE_TYPE = "notice-type";
    public static final String EXTRA_NOTICE_VALUE = "notice-value";
    public static final String EXTRA_NOTICE_DATA = "notice-data";

    public static final String EXTRA_ERROR = "error";


    // argument options
    public static final String ARG_LOGGER_UPLOAD_URL = "upload-url";


    // service recording states
    public static final int RECORDING_STATUS_INIT      = 0;
    public static final int RECORDING_STATUS_READY     = 1;
    public static final int RECORDING_STATUS_STARTING  = 2;
    public static final int RECORDING_STATUS_RECORDING = 3;
    public static final int RECORDING_STATUS_STOPPING  = 4;


    // [ activity <=> service ] message objectives
    public static final int MESSAGE_OBJECTIVE_RECORDING_STATUS  = 0x01;
    public static final int MESSAGE_OBJECTIVE_LOGGER_CREATE     = 0x02;
    public static final int MESSAGE_OBJECTIVE_LOGGER_CLOSE      = 0x03;
    public static final int MESSAGE_OBJECTIVE_ENCODER_CREATE    = 0x04;
    public static final int MESSAGE_OBJECTIVE_ENCODER_START     = 0x05;
    public static final int MESSAGE_OBJECTIVE_ENCODER_STOP      = 0x06;
    public static final int MESSAGE_OBJECTIVE_ENCODER_SUBSCRIBE = 0x07;

    public static final int MAX_MESSAGE_OBJECTIVE = MESSAGE_OBJECTIVE_ENCODER_SUBSCRIBE;

    public static final int MESSAGE_ARG_REQUEST_OPTION = 1;

    public static final String ACTION_DATA = "net.blurcast.tracer#data";

    public static final String INTENT_EVENT_TYPE = "event-type";
    public static final String INTENT_ENCODER_ID = "encoder-id";

    public static final String DATA_NEW_VALUE = "new-value";
    public static final String DATA_ENTRY_OWNER = "entry-owner";
    public static final String DATA_ENTRY_ID = "entry-id";
    public static final String DATA_ENTRY_INFO = "entry-info";

    public static final int NOTICE_NEW_ENTRY = 0x01;

    public static final int OBJECTIVE_FAILURE = 0;
    public static final int OBJECTIVE_SUCCESS = 1;

    public static final int PREPARE_ERROR_GPS_DISABLED       = 0xd0;
    public static final int PREPARE_ERROR_WIFI_DISABLED      = 0xd1;
    public static final int PREPARE_ERROR_BLUETOOTH_DISABLED = 0xd2;

    public static final int MESSAGE_TYPE_DATA   = 0xf0;
    public static final int MESSAGE_TYPE_NOTICE = 0xf1;
    public static final int MESSAGE_TYPE_ERROR  = 0xf2;

    public static final String LOG_FILE_GLOBAL_SUFFIX = ".bin";
}
