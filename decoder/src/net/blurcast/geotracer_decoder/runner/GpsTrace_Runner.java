//package net.blurcast.geotracer_decoder.runner;
//
//import net.blurcast.geotracer_decoder.callback.Eacher;
//import net.blurcast.geotracer_decoder.decoder.Gps;
//import net.blurcast.geotracer_decoder.decoder._Decoder;
//import net.blurcast.geotracer_decoder.helper.TraceInfo;
//import net.blurcast.geotracer_decoder.logger._Gis_Tabular_Log;
//import net.blurcast.geotracer_decoder.surrogate.Gps_Surrogate;
//import net.blurcast.geotracer_decoder.surrogate._Surrogate;
//
//import java.util.HashMap;
//import java.util.concurrent.atomic.AtomicBoolean;
//
///**
// * Created by blake on 1/21/15.
// */
//public class GpsTrace_Runner extends _Runner<_Gis_Tabular_Log> {
//
//    public GpsTrace_Runner(HashMap<Class<? extends _Decoder>, _Surrogate> surrogates, TraceInfo info) {
//        super(surrogates, info, _Gis_Tabular_Log.class);
//    }
//
//    @Override
//    public void run(final _Gis_Tabular_Log log) {
//        Gps_Surrogate gps = (Gps_Surrogate) mSurrogates.get(Gps.class);
//
//
//
////        log.table("gps-trace")
////                .field("device").cell(mInfo.getDeviceId())
////                .field("source").cell(mInfo.getFileName())
////                .field("trace").cell(gps.own.locations);
////
////                .field("device").allRows(mInfo.getDeviceId())
////                .field("source").allRows(mInfo.getFileName());
//
//        final AtomicBoolean bFirst = new AtomicBoolean(true);
//        final StringBuilder sPointArray2 = new StringBuilder();
//        final StringBuilder sPointArray3 = new StringBuilder();
//        final StringBuilder sMultipoint2 = new StringBuilder();
//        final StringBuilder sMultipoint3 = new StringBuilder();
//
//        final String sInstance = mInfo.getDeviceId()+"/"+mInfo.getFileName();
//
//        gps.each(new Eacher<Integer, Gps.Location>() {
//            @Override
//            public boolean each(Integer time, Gps.Location location) {
//
//                if(!bFirst.get()) {
//                    sPointArray2.append(",");
//                    sPointArray3.append(",");
//                    sMultipoint2.append(",");
//                    sMultipoint3.append(",");
//                }
//                else {
//                    bFirst.set(false);
//                }
//
//                sPointArray2.append("ST_GeomFromText('POINT("+location.latitude+" "+location.longitude+")',4326)");
//
//                sPointArray3.append("ST_GeomFromText('POINT("+location.latitude+" "+location.longitude+" "+time+")',4326)");
//
//                sMultipoint2.append(location.latitude+" "+location.longitude);
//
//                sMultipoint3.append(location.latitude+" "+location.longitude+" "+time);
//
//                return false;
//            }
//            @Override
//            public void after() {
//                log.table("t1")
//                        .field("name").value(sInstance)
//                        .field("traj").cell("ARRAY["+sPointArray2+"]")
//                        .save();
//
//                log.table("t2")
//                        .field("name").value(sInstance)
//                        .field("traj").geom("MULTIPOINT("+sMultipoint2+")")
//                        .save();
//
//                log.table("t3")
//                        .field("name").value(sInstance)
//                        .field("traj").geom("LINESTRING("+sMultipoint2+")")
//                        .save();
//
//
//                log.table("t1z")
//                        .field("name").value(sInstance)
//                        .field("traj").cell("ARRAY["+sPointArray3+"]")
//                        .save();
//
//                log.table("t2z")
//                        .field("name").value(sInstance)
//                        .field("traj").geom("MULTIPOINT("+sMultipoint3+")")
//                        .save();
//
//                log.table("t3z")
//                        .field("name").value(sInstance)
//                        .field("traj").geom("LINESTRING("+sMultipoint3+")")
//                        .save();
//
//
//                log.table("t1m")
//                        .field("name").value(sInstance)
//                        .field("traj").cell("ARRAY["+sPointArray3+"]")
//                        .save();
//
//                log.table("t2m")
//                        .field("name").value(sInstance)
//                        .field("traj").geom("MULTIPOINT("+sMultipoint3+")")
//                        .save();
//
//                log.table("t3m")
//                        .field("name").value(sInstance)
//                        .field("traj").geom("LINESTRING("+sMultipoint3+")")
//                        .save();
//
//                log.close();
//            }
//        });
//    }
//
//
//}
