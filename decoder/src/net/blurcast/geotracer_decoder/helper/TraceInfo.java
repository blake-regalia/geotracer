package net.blurcast.geotracer_decoder.helper;

import java.util.concurrent.TimeUnit;

/**
 * Created by blake on 1/21/15.
 */
public class TraceInfo {

    private String sDeviceId;
    private String sFileName;
    private long lStartTime;

    public TraceInfo(String deviceId, String fileName, long startTime) {
        sDeviceId = deviceId;
        sFileName = fileName;
        lStartTime = startTime;
    }

    public String getDeviceId() {
        return sDeviceId;
    }

    public String getFileName() {
        return sFileName;
    }

    public String getTraceId() {
        return sDeviceId+"/"+sFileName;
    }

    public long getStartTime() {
        return (lStartTime / 1000);
    }

    public double timeOf(long elapsed) {
        return (lStartTime + elapsed) / 1000.d;
    }
}
