package net.blurcast.geotracer_decoder.runner;

import net.blurcast.geotracer_decoder.callback.Eacher;
import net.blurcast.geotracer_decoder.decoder.Gps;
import net.blurcast.geotracer_decoder.decoder.Wap;
import net.blurcast.geotracer_decoder.logger._Hash_Log;
import net.blurcast.geotracer_decoder.surrogate.Gps_Surrogate;
import net.blurcast.geotracer_decoder.surrogate.Wap_Surrogate;

import java.util.ArrayList;

/**
 * Created by blake on 1/11/15.
 */
public class WapMap_Runner extends _Runner<_Hash_Log> {

    @Override
    protected Class<? extends _Hash_Log> getLogClass() {
        return _Hash_Log.class;
    }

    public void run(final _Hash_Log log) {
        final Wap_Surrogate waps = (Wap_Surrogate) mSurrogates.get(Wap.class);
        final Gps_Surrogate gps = (Gps_Surrogate) mSurrogates.get(Gps.class);

        // open the hash
        final _Hash_Log.Builder mWaps = log.object()
                .key("gps").array(new _Hash_Log.Runner() {
                    @Override
                    public void run(_Hash_Log.Builder builder) {
                        for(java.util.Map.Entry<Integer, Gps.Location> event: gps.own.locations.entrySet()) {
                            Gps.Location location = event.getValue();
                            builder.object()
                                    .key("time").value(event.getKey())
                                    .key("latlng").array()
                                        .value(location.latitude)
                                        .value(location.longitude)
                                        .end()
                                    .end();
                        }
                    }
                })
                .key("waps").array();

        waps.each(new Eacher<Integer, ArrayList<Wap.Event>>() {
            @Override
            public boolean each(Integer wapId, final ArrayList<Wap.Event> events) {

                final Wap.Info wapInfo = waps.own.infos.get(wapId);

                mWaps.object()
                        .key("bssid").value(wapInfo.getBssid())
                        .key("ssid").value(wapInfo.getSsid())
                        .key("frequency").value(wapInfo.frequency)
                        .key("security").value(wapInfo.getSecurity())
                        .key("events").array(new _Hash_Log.Runner() {
                    @Override
                    public void run(_Hash_Log.Builder builder) {

                        // no gps fixes available, skip events array
                        if(gps == null) return;

                        // iterate scan event concerning this wap
                        for(Wap.Event event : events) {
                            Wap.ScanInfo scanInfo = event.scanInfo;
                            Gps_Surrogate.EstimatedLocation location = gps.interpolate(scanInfo.elapsed); // scanInfo.scanDuration
                            if(location == null) continue;
                            builder.object()
                                    .key("latitude").value(location.latitude)
                                    .key("longitude").value(location.longitude)
                                    .key("confidence").value(location.confidence)
                                    .key("rssi").value(event.rssi)
                                    .key("time").value(scanInfo.elapsed)
                                    .key("duration").value(scanInfo.scanDuration)
                                    .end();
                        }
                    }
                }).end();

                return false;
            }
        });

        // close the array, then the hash
        mWaps.end().end();
    }
}
