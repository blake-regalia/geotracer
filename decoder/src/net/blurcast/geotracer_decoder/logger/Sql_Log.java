package net.blurcast.geotracer_decoder.logger;

import net.blurcast.geotracer_decoder.helper.Sql;
import net.blurcast.geotracer_decoder.helper.TraceInfo;

/**
 * Created by blake on 1/23/15.
 */
public class Sql_Log extends _Log implements _Trusty_Log {

    public Sql_Log() {
        mFileSuffix = ".sql";
    }

    public void out(String out) {
        dump(out);
    }

    public Sql.Core table(String table) {
        return Sql.core(table, this);
    }

    public Sql.Core getTrace(TraceInfo info) {

        // device
        this.table("device")
                .field("id").value(info.getDeviceId())
                .field("info").value("{}")
                .insert();

        // construct trace row core & assert existence
        return this.table("trace")
                .field("device_id").value(info.getDeviceId())
                .field("filename").value(info.getFileName())
                .insert(Sql.core()
                                .field("began").timestamp(info.getStartTime())
//                        .field("duration").value(mInfo.getDuration())
//                        .field("sensor").array(aSensors)
                );
    }
}
