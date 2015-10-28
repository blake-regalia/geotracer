package net.blurcast.android.util;

/**
 * Created by blake on 1/12/15.
 */
public class Bytes {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String toHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for(int j=0; j<bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j*2] = HEX_ARRAY[v >>> 4];
            hexChars[j*2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String toHexString(byte[] bytes, char delimiter) {
        char[] hexChars = new char[bytes.length*3-1];
        for(int j=0, j_max=bytes.length-1; j<=j_max; j++) {
            int v = bytes[j] & 0xFF;
            int i = j*3;
            hexChars[i] = HEX_ARRAY[v >>> 4];
            hexChars[i+1] = HEX_ARRAY[v & 0x0F];
            if(j < j_max) hexChars[i+2] = delimiter;
        }
        return new String(hexChars);
    }
}
