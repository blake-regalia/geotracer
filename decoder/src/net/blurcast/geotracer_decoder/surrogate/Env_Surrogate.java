package net.blurcast.geotracer_decoder.surrogate;

import net.blurcast.geotracer_decoder.decoder.Env;

import java.util.ArrayList;

/**
 * Created by blake on 1/17/15.
 */
public class Env_Surrogate extends _Surrogate<Integer, ArrayList<Env.SensorEvent>, Env> {

    public Env_Surrogate(Env env) {
        super(env, env.events);
    }
}
