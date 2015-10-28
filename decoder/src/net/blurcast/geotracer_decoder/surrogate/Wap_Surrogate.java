package net.blurcast.geotracer_decoder.surrogate;

import net.blurcast.geotracer_decoder.decoder.Wap;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by blake on 1/11/15.
 */
public class Wap_Surrogate extends _Surrogate<Integer, ArrayList<Wap.Event>, Wap> {

    public Wap_Surrogate(Wap wap) {
        super(wap, wap.events);
    }



}
