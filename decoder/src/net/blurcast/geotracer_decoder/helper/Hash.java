package net.blurcast.geotracer_decoder.helper;

import net.blurcast.geotracer_decoder.decoder._Decoder;
import net.blurcast.geotracer_decoder.logger.ByteDecodingFileReader;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by blake on 1/17/15.
 */
public class Hash {

    private HashMap<Integer, String> mMethodNames = new HashMap<Integer, String>();

    public Hash(int eventType, String methodName) {
        mMethodNames.put(eventType, methodName);
    }

    public Hash(int eventType1, String methodName1, int eventType2, String methodName2) {
        mMethodNames.put(eventType1, methodName1);
        mMethodNames.put(eventType2, methodName2);
    }

    public Hash(int eventType1, String methodName1, int eventType2, String methodName2, int eventType3, String methodName3) {
        mMethodNames.put(eventType1, methodName1);
        mMethodNames.put(eventType2, methodName2);
        mMethodNames.put(eventType3, methodName3);
    }

    public Hash(int eventType1, String methodName1, int eventType2, String methodName2, int eventType3, String methodName3, int eventType4, String methodName4) {
        mMethodNames.put(eventType1, methodName1);
        mMethodNames.put(eventType2, methodName2);
        mMethodNames.put(eventType3, methodName3);
        mMethodNames.put(eventType4, methodName4);
    }

    public Hash(int eventType1, String methodName1, int eventType2, String methodName2, int eventType3, String methodName3, int eventType4, String methodName4, int eventType5, String methodName5) {
        mMethodNames.put(eventType1, methodName1);
        mMethodNames.put(eventType2, methodName2);
        mMethodNames.put(eventType3, methodName3);
        mMethodNames.put(eventType4, methodName4);
        mMethodNames.put(eventType5, methodName5);
    }

    public Hash(int eventType1, String methodName1, int eventType2, String methodName2, int eventType3, String methodName3, int eventType4, String methodName4, int eventType5, String methodName5, int eventType6, String methodName6) {
        mMethodNames.put(eventType1, methodName1);
        mMethodNames.put(eventType2, methodName2);
        mMethodNames.put(eventType3, methodName3);
        mMethodNames.put(eventType4, methodName4);
        mMethodNames.put(eventType5, methodName5);
        mMethodNames.put(eventType6, methodName6);
    }


    public void setClassAndPutMethods(_Decoder decoder, HashMap<Integer, Cover> methods) {
        for(Map.Entry<Integer, String> entry: mMethodNames.entrySet()) {
            Method method = null;
            try {
                method = decoder.getClass().getMethod("decode_"+entry.getValue(), ByteDecodingFileReader.class);
            } catch (NoSuchMethodException e) {
                System.err.println("No such method in ["+decoder.getClass().getSimpleName()+"] to handle decoding "+entry.getValue());
                System.exit(1);
            }
            methods.put(entry.getKey(), new Cover(decoder, method));
        }
    }
}
