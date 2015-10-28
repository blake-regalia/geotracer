package net.blurcast.geotracer_decoder.runner;

import net.blurcast.geotracer_decoder.callback.Eacher;
import net.blurcast.geotracer_decoder.decoder.Env;
import net.blurcast.geotracer_decoder.decoder.Gps;
import net.blurcast.geotracer_decoder.helper.Sql;
import net.blurcast.geotracer_decoder.helper.TraceInfo;
import net.blurcast.geotracer_decoder.logger.Sql_Log;
import net.blurcast.geotracer_decoder.surrogate.Env_Surrogate;
import net.blurcast.geotracer_decoder.surrogate.Gps_Surrogate;

import java.util.ArrayList;

/**
 * Created by blake on 1/27/15.
 */
public class EnvMap_Runner extends _Runner<Sql_Log> {

    @Override
    protected Class<? extends Sql_Log> getLogClass() {
        return Sql_Log.class;
    }

    @Override
    public void run(final Sql_Log log) {
        final Gps_Surrogate gps = (Gps_Surrogate) mSurrogates.get(Gps.class);
        final Env_Surrogate env = (Env_Surrogate) mSurrogates.get(Env.class);

        // start with the trace
        final Sql.Core trace = log.getTrace(mInfo);

        // each event
        env.each(new Eacher<Integer, ArrayList<Env.SensorEvent>>() {
            @Override
            public boolean each(Integer sensorId, ArrayList<Env.SensorEvent> events) {

                // fetch sensor info
                Env.SensorInfo sensorInfo = env.own.sensors.get(sensorId);

                // determine sensor type
                switch(sensorInfo.localType) {

                    // temperature sensor!
                    case Env.SENSOR_TYPE_TEMPERATURE:

                        // iterate events
                        for(Env.SensorEvent event: events) {

                            // settle location
                            Gps_Surrogate.EstimatedLocation location = gps.interpolate(event.elapsed);

                            // no location available, skip event
                            if(location == null) continue;

                            // insert temperature row
                            log.table("temperature")
                                .field("trace_id").using(trace, "local_id")
                                .field("time").timestamp(mInfo.timeOf(event.elapsed))
                                .field("location").geom(location)
                                .field("celsius").value(event.value)
                                .insert();
                        }
                        break;
                }

                return false;
            }
        });
    }
}
