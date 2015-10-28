package net.blurcast.geotracer_decoder.helper;

import net.blurcast.geotracer_decoder.decoder._Decoder;
import net.blurcast.geotracer_decoder.logger.ByteDecodingFileReader;
import net.blurcast.geotracer_decoder.surrogate._Surrogate;

import java.util.HashMap;

/**
 * Created by blake on 1/17/15.
 */
public class DecoderResponsibilities {

        private HashMap<Integer, Cover> mMethods = new HashMap<Integer, Cover>();

        public DecoderResponsibilities(Responsibility... allResponsibilities) {
            for(Responsibility responsibility: allResponsibilities) {
                responsibility.setMethodMap(mMethods);
            }
        }

        public Pair<Class<? extends _Decoder>, _Surrogate> handle(int eventType, ByteDecodingFileReader source) {
            Cover cover = mMethods.get(eventType);
            if(cover == null) {
                return null;
            }
            else {
                return cover.invoke(source);
            }
        }

}
