package net.blurcast.geotracer_decoder.decoder;

import net.blurcast.geotracer_decoder.surrogate._Surrogate;

/**
 * Created by blake on 1/9/15.
 */
public abstract class _Decoder {

    public _Decoder() {

    }


    private static final char[] STR_HEX_LOWER = "0123456789abcdef".toCharArray();

    public String hwToString(byte[] bssid) {
        if(bssid.length == 6) {
            return STR_HEX_LOWER[(bssid[0] & 0xf0) >> 8] + ""
                    + STR_HEX_LOWER[(int) bssid[0] & 0x0f] + ":"
                    + STR_HEX_LOWER[(bssid[1] & 0xf0) >> 8] + ""
                    + STR_HEX_LOWER[(int) bssid[1] & 0x0f] + ":"
                    + STR_HEX_LOWER[(bssid[2] & 0xf0) >> 8] + ""
                    + STR_HEX_LOWER[(int) bssid[2] & 0x0f] + ":"
                    + STR_HEX_LOWER[(bssid[3] & 0xf0) >> 8] + ""
                    + STR_HEX_LOWER[(int) bssid[3] & 0x0f] + ":"
                    + STR_HEX_LOWER[(bssid[4] & 0xf0) >> 8] + ""
                    + STR_HEX_LOWER[(int) bssid[4] & 0x0f] + ":"
                    + STR_HEX_LOWER[(bssid[5] & 0xf0) >> 8] + ""
                    + STR_HEX_LOWER[(int) bssid[5] & 0x0f];
        }
        else if(bssid.length == 9) {
            return STR_HEX_LOWER[(bssid[0] & 0xf0) >> 8]+""
                    +STR_HEX_LOWER[(int) bssid[0] & 0x0f]+":"
                    +STR_HEX_LOWER[(bssid[1] & 0xf0) >> 8]+""
                    +STR_HEX_LOWER[(int) bssid[1] & 0x0f]+":"
                    +STR_HEX_LOWER[(bssid[2] & 0xf0) >> 8]+""
                    +STR_HEX_LOWER[(int) bssid[2] & 0x0f]+":"
                    +STR_HEX_LOWER[(bssid[3] & 0xf0) >> 8]+""
                    +STR_HEX_LOWER[(int) bssid[3] & 0x0f]+":"
                    +STR_HEX_LOWER[(bssid[4] & 0xf0) >> 8]+""
                    +STR_HEX_LOWER[(int) bssid[4] & 0x0f]+":"
                    +STR_HEX_LOWER[(bssid[5] & 0xf0) >> 8]+""
                    +STR_HEX_LOWER[(int) bssid[5] & 0x0f]+":"
                    +STR_HEX_LOWER[(bssid[6] & 0xf0) >> 8]+""
                    +STR_HEX_LOWER[(int) bssid[6] & 0x0f]+":"
                    +STR_HEX_LOWER[(bssid[7] & 0xf0) >> 8]+""
                    +STR_HEX_LOWER[(int) bssid[7] & 0x0f]+":"
                    +STR_HEX_LOWER[(bssid[8] & 0xf0) >> 8]+""
                    +STR_HEX_LOWER[(int) bssid[8] & 0x0f];
        }

        return "???? {"+bssid.length+" bytes}";
    }

    public abstract Class<? extends _Decoder> getRealClass();
    public abstract _Surrogate getSurrogate();
}
