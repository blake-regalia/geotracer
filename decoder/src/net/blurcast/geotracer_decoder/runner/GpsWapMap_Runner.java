package net.blurcast.geotracer_decoder.runner;

import net.blurcast.geotracer_decoder.callback.Eacher;
import net.blurcast.geotracer_decoder.decoder.Gps;
import net.blurcast.geotracer_decoder.decoder.Wap;
import net.blurcast.geotracer_decoder.helper.Sql;
import net.blurcast.geotracer_decoder.logger.Sql_Log;
import net.blurcast.geotracer_decoder.surrogate.Gps_Surrogate;
import net.blurcast.geotracer_decoder.surrogate.Wap_Surrogate;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by blake on 1/23/15.
 */
public class GpsWapMap_Runner extends _Runner<Sql_Log> {

    @Override
    protected Class<? extends Sql_Log> getLogClass() {
        return Sql_Log.class;
    }

    @Override
    public void run(final Sql_Log log) {
        final Gps_Surrogate gps = (Gps_Surrogate) mSurrogates.get(Gps.class);
        final Wap_Surrogate wap = (Wap_Surrogate) mSurrogates.get(Wap.class);

        // keep track of which events get skipped
        final HashSet<Integer> mSkippedEvents = new HashSet<Integer>();

        // start with trace
        final Sql.Core trace = log.getTrace(mInfo);

        // loop through all waps
        wap.each(new Eacher<Integer, ArrayList<Wap.Event>>() {
            @Override
            public boolean each(Integer wapId, ArrayList<Wap.Event> events) {

                // reference wap info
                Wap.Info wapInfo = wap.own.infos.get(wapId);

                if(wapInfo == null) log.warn("WapInfo["+wapId+"] is null");

                // construct wap row core & assert existence
                Sql.Core wap = log.table("wap")
                        .field("bssid").value(wapInfo.getBssid())
                        .field("ssid").value(wapInfo.getSsid())
                        .field("frequency").value(wapInfo.frequency)
                        .field("security").value(wapInfo.security)
                        .insert();

                // iterate each event
                for(Wap.Event event: events) {

                    // fetch scan info
                    Wap.ScanInfo scanInfo = event.scanInfo;

                    // interpolate location
                    Gps_Surrogate.EstimatedLocation location = gps.interpolate(scanInfo.elapsed);

                    // no location
                    if(location == null) {
                        int elapsed = scanInfo.elapsed;
                        if(!mSkippedEvents.contains(elapsed)) {
                            log.warn("Skipping all Wap events @"+elapsed);
                            mSkippedEvents.add(elapsed);
                        }
                        continue;
                    }

                    // insert rssi row
                    log.table("wap_sample")
                            .field("wap_id").using(wap, "local_id")
                            .field("trace_id").using(trace, "local_id")
                            .field("scan_time").timestamp(mInfo.timeOf(scanInfo.elapsed))
                            .field("scan_duration").interval(scanInfo.scanDuration)
                            .field("location").geom(location)
                            .field("rssi").value(event.rssi)
                            .insert();
                }

                // continue: do not break
                return false;
            }
        });
    }
}
