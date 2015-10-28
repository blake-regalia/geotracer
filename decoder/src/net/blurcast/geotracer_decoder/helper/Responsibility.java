package net.blurcast.geotracer_decoder.helper;

import net.blurcast.geotracer_decoder.decoder._Decoder;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * Created by blake on 1/17/15.
 */
public class Responsibility {

    private Hash mResolutions;
    private _Decoder mDecoder;

    public Responsibility(Class<? extends _Decoder> decoderClass, Hash resolutions) {
        mResolutions = resolutions;
        try {
            mDecoder = (_Decoder) decoderClass.getConstructors()[0].newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void setMethodMap(HashMap<Integer, Cover> methods) {
        mResolutions.setClassAndPutMethods(mDecoder, methods);
    }

}
