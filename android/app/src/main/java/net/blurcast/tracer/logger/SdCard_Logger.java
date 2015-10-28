package net.blurcast.tracer.logger;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import net.blurcast.tracer.app.Geotracer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by blake on 10/11/14.
 */
public class SdCard_Logger extends File_Logger {

    private static final String TAG = SdCard_Logger.class.getSimpleName();

    private static final String PATH_DEFAULT_SD_DIRECTORY = "./geotracer-files/";

    protected File mTraceDir;

    public SdCard_Logger(Context context, Bundle args) {
        super(context, args);

        // prepare sd card directory
        File sdCardDir = Environment.getExternalStorageDirectory();
        mTraceDir = new File(sdCardDir, PATH_DEFAULT_SD_DIRECTORY);
        Log.d(TAG, "Preparing directory: "+mTraceDir.getAbsolutePath());
        mTraceDir.mkdir();
    }

    @Override
    protected void finish(Bundle data) {

        // optional file name suffix & global file-name suffix
        String sSuffix = data.getString(Geotracer.EXTRA_LOGGER_FILE_SUFFIX, "")+Geotracer.LOG_FILE_GLOBAL_SUFFIX;

        // prepare to move file to external sdcard
        File sdCardPath = new File(mTraceDir, mLogFile.getName()+sSuffix);
        Log.d(TAG, "copying trace file to: " + sdCardPath.getAbsolutePath());

        try {
            this.copy(mLogFile, sdCardPath);
            mLogFile.delete();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        byte[] buf = new byte[2048];
        int len;
        while((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
}
