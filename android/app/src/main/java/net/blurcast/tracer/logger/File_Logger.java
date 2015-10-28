package net.blurcast.tracer.logger;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

/**
 * Created by blake on 10/11/14.
 */
public class File_Logger extends _Logger {

    private static final String TAG = File_Logger.class.getSimpleName();

    protected static final char CHAR_JOIN  = '-';
    protected static final char CHAR_SPLIT = '_';
    protected static final char CHAR_END   = '.';

    protected File mFilesDir;
    protected File mLogFile;
    protected BufferedOutputStream mTraceFileData;


    public File_Logger(Context context, Bundle args) {
        super(context, args);

        // set file directory
        mFilesDir = mContext.getFilesDir();

        // generate unique filename
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(System.currentTimeMillis());
        String fileName = ""+(start.get(Calendar.YEAR)+"")+CHAR_JOIN
                +zeroPadTwoDigits(start.get(Calendar.MONTH) + 1)+CHAR_JOIN
                +zeroPadTwoDigits(start.get(Calendar.DAY_OF_MONTH))+CHAR_SPLIT
                +zeroPadTwoDigits(start.get(Calendar.HOUR_OF_DAY))+CHAR_JOIN
                +zeroPadTwoDigits(start.get(Calendar.MINUTE))+CHAR_JOIN
                +zeroPadTwoDigits(start.get(Calendar.SECOND))+CHAR_SPLIT
                +zeroPadFourDigits(new Random().nextInt(9999));

        // create new file
        this.createAndOpenFile(fileName);

        // write file header
        this.writeHeader();
    }

    private String zeroPadTwoDigits(int d) {
        if(d < 10) {
            return "0"+d;
        }
        return ""+d;
    }

    private String zeroPadFourDigits(int d) {
        if(d < 10) {
            return "000"+d;
        }
        else if(d < 100) {
            return "00"+d;
        }
        else if(d < 1000) {
            return "0"+d;
        }
        return ""+d;
    }

    /**
     * Creates a new trace file in the current application's file directory
     * @param fileName
     */
    protected void createAndOpenFile(String fileName) {
        try {
            mLogFile = new File(mFilesDir, fileName);
            mTraceFileData = new BufferedOutputStream(mContext.openFileOutput(fileName, Context.MODE_PRIVATE));
        } catch (FileNotFoundException e) {
            Log.e(TAG, "No such file exists: \""+fileName+"\" within "+mFilesDir);
            Log.e(TAG, e.getMessage());
        }
    }


    /**
     * Writes data to the open trace file
     * @param data      byte[] of data to submit to the log file
     */
    @Override
    public void submit(byte[] data) {
        try {
            if(mTraceFileData == null) throw new NullPointerException("Trace file was never opened \""+mLogFile.getPath()+"\"");
            mTraceFileData.write(data);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }


    /**
     * Closes the trace file and moves it to the SD card
     */
    @Override
    public void close(Bundle options) {
        if(mTraceFileData != null) {
            try {
                mTraceFileData.flush();
                mTraceFileData.close();
                mTraceFileData = null;
                this.finish(options);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        else {
            Log.w(TAG, "No trace file to close");
        }
    }

    protected void finish(Bundle data) {}

    public String toString() {
        return "Log File \""+mLogFile.getPath()+"\"; ready: "+(mTraceFileData!=null);
    }
}
