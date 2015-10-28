package net.blurcast.geotracer_decoder.app;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import net.blurcast.geotracer_decoder.decoder.*;
import net.blurcast.geotracer_decoder.helper.*;
import net.blurcast.geotracer_decoder.logger.ByteDecodingFileReader;
import net.blurcast.geotracer_decoder.logger.Json_Log;
import net.blurcast.geotracer_decoder.logger.Sql_Log;
import net.blurcast.geotracer_decoder.logger._Log;
import net.blurcast.geotracer_decoder.runner.*;
import net.blurcast.geotracer_decoder.surrogate._Surrogate;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by blake on 1/10/15.
 */

public class GeotracerDecoder {

    private static final boolean B_CHMOD_UGO_ALL = false;


    public static final byte TYPE_OPEN       = 0x00;
    public static final byte TYPE_CLOSE      = 0x01;

    public static final byte TYPE_GPS_INIT       = 0x10;
    public static final byte TYPE_GPS_LOCATION   = 0x11;
    public static final byte TYPE_GPS_SATELLITES = 0x12;
    public static final byte TYPE_GPS_STATUS     = 0x13;

    public static final byte TYPE_WAP_EVENT  = 0x21;
    public static final byte TYPE_WAP_INFO   = 0x22;
    public static final byte TYPE_WAP_SSID   = 0x23;

    //    public static final byte TYPE_BTLE_EVENT  = 0x24;
//    public static final byte TYPE_BT_EVENT  = 0x25;
    public static final byte TYPE_BTLE_EVENT  = 0x26;
    public static final byte TYPE_BTLE_INFO   = 0x27;

    public static final byte TYPE_ENV_SENSOR_INFO       = 0x30;
    public static final byte TYPE_ENV_SENSOR_ACCURACY   = 0x31;
    public static final byte TYPE_ENV_TEMPERATURE_EVENT = 0x32;
    public static final byte TYPE_ENV_LIGHT_EVENT       = 0x33;
    public static final byte TYPE_ENV_PRESSURE_EVENT    = 0x34;
    public static final byte TYPE_ENV_HUMIDITY_EVENT    = 0x35;

    // construct decoder map
    private static DecoderResponsibilities mResponsibilities = new DecoderResponsibilities(
            new Responsibility(
                    Gps.class, new Hash(
                    TYPE_GPS_INIT, "init",
                    TYPE_GPS_LOCATION, "location",
                    TYPE_GPS_SATELLITES, "satellites",
                    TYPE_GPS_STATUS, "status"
            )),
            new Responsibility(
                    Wap.class, new Hash(
                    TYPE_WAP_EVENT, "event",
                    TYPE_WAP_INFO, "info",
                    TYPE_WAP_SSID, "ssid"
            )),
            new Responsibility(
                    Btle.class, new Hash(
                    TYPE_BTLE_EVENT, "event",
                    TYPE_BTLE_INFO, "info"
            )),
            new Responsibility(
                    Env.class, new Hash(
                    TYPE_ENV_SENSOR_INFO, "sensorInfo",
                    TYPE_ENV_SENSOR_ACCURACY, "sensorAccuracy",
                    TYPE_ENV_TEMPERATURE_EVENT, "temperatureEvent",
                    TYPE_ENV_LIGHT_EVENT, "lightEvent",
                    TYPE_ENV_PRESSURE_EVENT, "pressureEvent",
                    TYPE_ENV_HUMIDITY_EVENT, "humidityEvent"
            ))
    );

    // construct runner map
    private static _Runner.Map mRunners = new _Runner.Map(
            new _Runner.Pair("wap-map", WapMap_Runner.class),
            new _Runner.Pair("btle-rssi", BtleSignals_Runner.class),
            new _Runner.Pair("map-wap", GpsWapMap_Runner.class),
            new _Runner.Pair("map-env", EnvMap_Runner.class)
    );

    // construct logger map
    private static _Log.Map mLogMap = new _Log.Map(
            new _Log.Pair("json", Json_Log.class),
//            new _Log.Pair("csv", Csv_Log.class),
            new _Log.Pair("sql", Sql_Log.class)
    );


    // Geotracer-Decoder
    public GeotracerDecoder(GeotracerOptions options) {

        // all files options TODO fix this
        if(options.allFiles) die("Known bug when using -a option. Disabled");

        // fetch app key
        String sAppKey = options.appKey;

        // fetch device id
        File mInputDir = options.resolveDevice();
        String sDeviceId = mInputDir.getName();

        // fetch log file names
        File mInputFile = options.resolveFile(mInputDir);

        // set output directory
        File mOutputDir = new File("./output/"+sAppKey+"/"+sDeviceId);

        // mkdirs
        if(!mOutputDir.exists() && !mOutputDir.mkdirs()) die("Failed to mkdirs('"+mOutputDir.getAbsolutePath()+"')");

        // chmod new directory
        else if(!mOutputDir.canWrite() && !mOutputDir.setWritable(true, !B_CHMOD_UGO_ALL)) warn("Unable to chmod('"+mOutputDir.getAbsolutePath()+"')");

        //
        options.debug("Running "+options.appKey+" on:");

        {

            // determine output basename
            String sOutputFileName = mInputFile.getName().replaceFirst("\\.bin$", "");

            // set output file
            File mOutputFile = new File(mOutputDir, sOutputFileName);


            // open input file for reading
            ByteDecodingFileReader source = new ByteDecodingFileReader(mInputFile);

            // keep track of which decoders get used
            HashMap<Class<? extends _Decoder>, _Surrogate> mSurrogates = new HashMap<Class<? extends _Decoder>, _Surrogate>();

            //
            options.debug("===== "+mInputFile.getName()+" =====");

            //
            int iVersion;
            long lStartTime = 0;
            boolean bStop = false;

            // read to end
            while(source.hasBytes()) {

                // read this block type
                int eventType = source.read();
                switch(eventType) {

                    // open block
                    case TYPE_OPEN:
                        iVersion = source.read_int_2();
                        lStartTime = source.read_long();
                        options.debug("  version: "+iVersion);
                        options.debug("  started: "+lStartTime);
                        break;

                    // data block
                    default:

                        // parse this block
                        Pair<Class<? extends _Decoder>, _Surrogate> pair = mResponsibilities.handle(eventType, source);

                        // failed to handle event
                        if(pair == null) {
                            if(source.bytes_read == 0x16071) {
                                source.skipTo(0x1606e);
//                                bStop = true;
                                break;
                            }
                            else {
                                die("== No cover for event type 0x"+Integer.toHexString(eventType)+" @"+Integer.toHexString(source.bytes_read)+" ==");
                                return;
                            }
                        }

                        // parse success
                        else {
                            if(!mSurrogates.containsKey(pair.getKey())) {
                                options.debug("+"+pair.getKey().getSimpleName());
                                mSurrogates.put(pair.getKey(), pair.getValue());
                            }
                        }

                        if(bStop) die("user stop");
                        break;
                }
            }

            //
            options.debug("===== end =====");


            // try to fetch runner
            _Runner runner = mRunners.newInstance(sAppKey, mSurrogates, new TraceInfo(sDeviceId, mInputFile.getName(), lStartTime));
            if(runner == null) die("Failed to construct app: \""+sAppKey+"\"");

            // create logger
            _Log log = options.newLogger(runner, mOutputFile);

            // run app
            runner.run(log);

            // close output
            log.close();
        }
    }


    // creates a new instance of geotracer-decoder using given args
    public static void spawn(String[] argv) {
        new GeotracerDecoder(new GeotracerOptions(argv));
    }


    // options
    public static class GeotracerOptions {

        private JCommander jCommander;

        public GeotracerOptions(String[] argv) {
            jCommander = new JCommander(this);
            jCommander.setProgramName("android-geotracer-decoder", "agd");
            jCommander.parse(argv);
            this.checkArgs();
        }

        private void checkArgs() {
            StringBuilder b = new StringBuilder();
            String error = null;
            if(params.size() == 0) {
                error = "Must specify an app to use for processing input file";
            }
            else {
                appKey = params.get(0);
            }
            if(error != null) {}
            else if(filePath == null) {
                if(devicePrefix == null) {
                    error = "Must specify an input device";
                } else if(fileName == null && nthLatest <= 0) {
                    error = "Must specify an input file or positive nth-latest file integer";
                }
            }
            else if(appKey == null) {
                error = "Must specify an app to use for processing input file";
            }
            else if(mRunners.get(appKey) == null) {
                error = "No such app \""+appKey+"\" exists";
            }
            if(error != null) {
                b.append(error).append("\n\n");
                jCommander.usage(b);
                die(b.toString());
            }
        }

        @Parameter(names = {"-d", "--device"}, description = "specifies the input device by prefix")
        public String devicePrefix;

        @Parameter(names = {"-f", "--file"}, description = "specifies the input file")
        public String fileName;

        @Parameter(names = {"-p", "--path"}, description = "specifies path of input file including its parent directory")
        public String filePath;

        @Parameter(names = {"-n", "--nth-latest"}, description = "specifies the nth-latest input file for the given device")
        public int nthLatest = 1;

        @Parameter(description = "app")
        public List<String> params = new ArrayList<String>();

        public String appKey;

        @Parameter(names = {"-o", "--output"}, description = "set which output format to use")
        public String outputType;

        @Parameter(names = {"-a", "--all-files"}, description = "process all files for given device")
        public boolean allFiles = false;

        @Parameter(names = {"-l", "--stream-mode"}, description = "streams output to stdout")
        public boolean streamOutput = false;

        private static final FilenameFilter REAL_FILES = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !name.startsWith(".");
            }
        };

        public File resolveDevice() {

            // path given
            if(filePath != null) {
                File file = new File(filePath);
                File deviceDir = file.getParentFile();
                if(deviceDir == null) die("File path must include device id as parent directory");
                String deviceId = deviceDir.getName();
                if(deviceId == null || deviceId.length() == 0) die("File path must include device id as parent directory");
                if(!deviceDir.isDirectory()) die("No directory exists for device \""+deviceId+"\"");
                return deviceDir;
            }
            else {
                File pwd = new File(System.getProperty("user.dir"), "./input");
                String[] deviceDirs = pwd.list(REAL_FILES);
                if(deviceDirs == null) die("Input directory is not readable: \""+pwd.getPath()+"\"");
                ArrayList<String> candidateDevices = new ArrayList<String>();
                for(String device : deviceDirs) {
                    if(device.startsWith(devicePrefix)) {
                        candidateDevices.add(device);
                    }
                }
                if(candidateDevices.size() == 0) {
                    die("No devices match prefix: \""+devicePrefix+"\"");
                } else if(candidateDevices.size() != 1) {
                    die("Could not uniquely resolve device prefix: \""+devicePrefix+"\"");
                } else {
                    String deviceId = candidateDevices.get(0);
                    File deviceDir = new File(pwd, deviceId);
                    if(!deviceDir.isDirectory()) {
                        die("No directory exists for device \""+deviceId+"\"");
                    } else {
                        return deviceDir;
                    }
                }
            }
            return null;
        }

        public File resolveFile(File deviceDir) {
            String sFileName = fileName;
            if(filePath != null) {
                File file = new File(filePath);
                sFileName = file.getName();
            }
            if(sFileName != null) {
                File inputFile = new File(deviceDir, sFileName);
                if(!inputFile.isFile()) {
                    die("No such file \""+sFileName+"\" exists for device \""+deviceDir.getName()+"\"");
                } else {
                    return inputFile;
                }
            } else {
                String[] candidateFiles = deviceDir.list(REAL_FILES);
                Arrays.sort(candidateFiles);
                if(nthLatest > candidateFiles.length) {
                    die("nth-latest file index given is out of bounds: "+nthLatest+" > "+candidateFiles.length);
                } else {
                    return new File(deviceDir, candidateFiles[candidateFiles.length-nthLatest]);
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        public _Log newLogger(_Runner runner, File outputFile) {
            Class<? extends _Log> logClass;

            // no output type given
            if(outputType == null) {
                warn("No output type specified. Attempting to use default output type for \""+appKey+"\"");
                logClass = runner.getDefaultLogClass();
                if (logClass == null) {
                    die("Log class used by app \"" + runner.getClass().getSimpleName() + "\" does not implement a default output type. You must explicitly set an output type for this app");
                }
            }
            // use given output type
            else {
                logClass = mLogMap.get(outputType);
                if(logClass == null) {
                    die("No such output type: \"" + outputType + "\"");
                }
            }

            // instantiate log object
            _Log log = null;
            try {
                log = logClass.getConstructor().newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            if(log == null) {

                // user fault
                if(outputType != null) {
                    die("Output type \""+outputType+"\" incompatible with app \""+appKey+"\"");
                }
                //
                else {
                    die("Failed to instantiate log class \""+logClass.getSimpleName()+"\"");
                }
                return null;
            }
            else {
                // setup log (runner class)
                log.setup(runner.getClass().getSimpleName());

                // save output to file
                if(!streamOutput) {
                    log.saveOutput(outputFile);
                }
                return log;
            }
        }

        public void debug(String output) {
            if(streamOutput) {
                System.err.println(output);
            }
            else {
                System.out.println(output);
            }
        }
    }

    public static void warn(String warning) {
        System.err.println("Warning: "+warning);
    }

    public static void die(String error) {
        System.err.println("ERROR: "+error);
        System.exit(1);
    }
}