package net.blurcast.geotracer_decoder.surrogate;

import net.blurcast.geotracer_decoder.decoder.Btle;

import java.util.ArrayList;

/**
 * Created by blake on 1/11/15.
 */
public class Btle_Surrogate extends _Surrogate<Integer, ArrayList<Btle.Event>, Btle> {

    public Btle_Surrogate(Btle btle) {
        super(btle, btle.events);
    }

}
