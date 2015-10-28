package net.blurcast.geotracer_decoder.helper;

import net.blurcast.geotracer_decoder.decoder._Decoder;
import net.blurcast.geotracer_decoder.logger.ByteDecodingFileReader;
import net.blurcast.geotracer_decoder.surrogate._Surrogate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by blake on 1/17/15.
 */
public class Cover {

    private _Decoder mDecoder;
    private Method mMethod;

    public Cover(_Decoder decoder, Method method) {
        mDecoder = decoder;
        mMethod = method;
    }

    public Pair<Class<? extends _Decoder>, _Surrogate> invoke(ByteDecodingFileReader source) {
        try {
            mMethod.invoke(mDecoder, source);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return new Pair<Class<? extends _Decoder>, _Surrogate>(mDecoder.getRealClass(), mDecoder.getSurrogate());
    }

}
