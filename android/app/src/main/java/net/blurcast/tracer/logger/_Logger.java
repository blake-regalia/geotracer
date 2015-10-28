package net.blurcast.tracer.logger;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;

import net.blurcast.android.util.ByteBuilder;
import net.blurcast.android.util.Encoder;

import java.util.concurrent.TimeUnit;

/**
 * Created by blake on 10/11/14.
 */
public abstract class _Logger {

    public static final byte TYPE_OPEN       = 0x00;
    public static final byte TYPE_CLOSE      = 0x01;

    public static final byte TYPE_GPS_INIT       = 0x10;
    public static final byte TYPE_GPS_LOCATION   = 0x11;
    public static final byte TYPE_GPS_SATELLITES = 0x12;
    public static final byte TYPE_GPS_STATUS     = 0x13;

    public static final byte TYPE_WAP_EVENT  = 0x21;
    public static final byte TYPE_WAP_INFO   = 0x22;
    public static final byte TYPE_WAP_SSID   = 0x23;

//    public static final byte TYPE_BTLE_EVENT  = 0x24;s
//    public static final byte TYPE_BT_EVENT  = 0x25;
    public static final byte TYPE_BTLE_EVENT  = 0x26;
    public static final byte TYPE_BTLE_INFO   = 0x27;

    public static final byte TYPE_ENV_SENSOR_INFO       = 0x30;
    public static final byte TYPE_ENV_SENSOR_ACCURACY   = 0x31;
    public static final byte TYPE_ENV_TEMPERATURE_EVENT = 0x32;
    public static final byte TYPE_ENV_LIGHT_EVENT       = 0x33;
    public static final byte TYPE_ENV_PRESSURE_EVENT    = 0x34;
    public static final byte TYPE_ENV_HUMIDITY_EVENT    = 0x35;


    private long lTimeStarted;
    private int iApkVersion;

    protected Context mContext;
    protected Bundle mArgs;

    public abstract void submit(byte[] data);
    public abstract void close(Bundle options);


    public _Logger(Context context, Bundle args) {
        mContext = context;
        mArgs = args;
        try {
            iApkVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch(PackageManager.NameNotFoundException e) {
            iApkVersion = -1;
        }
    }

    public void writeHeader() {
        long now = System.currentTimeMillis();
        lTimeStarted = SystemClock.elapsedRealtime();

        ByteBuilder bytes = new ByteBuilder(1+2+8);

        // this is an opening block
        bytes.append(TYPE_OPEN);

        // version info: 2 bytes
        bytes.append_2(
                Encoder.encode_char(iApkVersion)
        );

        // start time (real-world time): 8 bytes
        bytes.append_8(
                Encoder.encode_long(now)
        );

        // submit header
        this.submit(bytes.getBytes());
    }


    public byte[] encodeOffsetTime(long elapsed) {
        return Encoder.encode_long_to_3_bytes(
                elapsed - lTimeStarted
        );
    }

    public byte[] encodeOffsetTimeFromNanos(long elapsed) {
        return Encoder.encode_long_to_3_bytes(
                TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS) - lTimeStarted
        );
    }

}
