package net.blurcast.geotracer_decoder.runner;

import net.blurcast.geotracer_decoder.callback.Eacher;
import net.blurcast.geotracer_decoder.decoder.Btle;
import net.blurcast.geotracer_decoder.logger._Hash_Log;
import net.blurcast.geotracer_decoder.surrogate.Btle_Surrogate;

import java.util.ArrayList;

/**
 * Created by blake on 1/13/15.
 */
public class BtleSignals_Runner extends _Runner<_Hash_Log> {

//    private static final int T_WINDOW_THRESHOLD_NS = 2000; // 250 milliseconds
    private static final int T_WINDOW_THRESHOLD_NS = 5000; // 500 milliseconds

    @Override
    protected Class<? extends _Hash_Log> getLogClass() {
        return _Hash_Log.class;
    }

    public void run(final _Hash_Log log) {
        final Btle_Surrogate btle = (Btle_Surrogate) mSurrogates.get(Btle.class);

        // open the hash
        final _Hash_Log.Builder mHash = log.array();

        // just get the data out of here
        btle.each(new Eacher<Integer, ArrayList<Btle.Event>>() {
            @Override
            public boolean each(Integer deviceId, final ArrayList<Btle.Event> item) {

                // grab device info
                Btle.Device deviceInfo = btle.own.devices.get(deviceId);

                // skip anything not iBeacon
                if (Btle.BLUETOOTH_TYPE_IBEACON != deviceInfo.type)
                    return false;

                //
                mHash.object()
                        .key("deviceId").value(deviceId)
                        .key("events").array(new _Hash_Log.Runner() {
                            public void run(_Hash_Log.Builder array) {

                                // for each event from this device
                                for (Btle.Event event : item) {
                                    array.object()
                                            .key("time").value(event.elapsed)
                                            .key("rssi").value(event.rssi)
                                            .end();
                                }
                            }
                        })
                        .end();

                return false;
            }
        });

        // close the array
        mHash.end();
    }

}




//        btle.each(new Eacher<Integer, ArrayList<Btle.Event>>() {
//            @Override
//            public boolean each(Integer deviceId, ArrayList<Btle.Event> item) {
//                Btle.Device deviceInfo = btle.own.devices.get(deviceId);
//
//                // skip anything not iBeacon
//                if(Btle.BLUETOOTH_TYPE_IBEACON != deviceInfo.type) return false;
//
//                // for all events
//                for(Btle.Event event: item) {
//
//                    // put each event in a moving window
//                    {
//                        Pair<Integer, Integer> entry = new Pair<Integer, Integer>(event.elapsed, (int) event.rssi);
//                        movingWindow.add(entry);
//                    }
//
//                    // remove anything older than max allowed threshold
//                    int cutoff = event.elapsed - T_WINDOW_THRESHOLD_NS;
//                    while(!movingWindow.isEmpty() && movingWindow.peek().getKey() < cutoff) {
//                        movingWindow.poll();
//                    }
//
//                    // compute the median of the moving window
//                    int[] arr = new int[movingWindow.size()]; int i = 0;
//                    for(Pair<Integer, Integer> entry: movingWindow) {
//                        arr[i++] = entry.getValue();
//                    }
//                    Arrays.sort(arr);
//
//                    int med;
//                    if(arr.length % 2 == 1) {
//                        med = arr[(arr.length-1)/2];
//                    }
//                    else {
//                        int half = arr.length / 2;
//                        med = Math.round((arr[half-1]+arr[half]) / 2.0f);
//                    }
//
//                    med = event.rssi;
//
//                    log.out(deviceId, event.elapsed, med);
//                }
//                return false;
//            }
//        });
