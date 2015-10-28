package net.blurcast.geotracer_decoder.logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Created by blake on 1/10/15.
 */
public abstract class _Log {

    private PrintWriter mWriter = new PrintWriter(System.out);
    protected String mFileSuffix = "";
    private String sTag = "";

    public _Log() {}

    public final void setup(String tag) {
        sTag = tag;
    }

    public final void saveOutput(File outputFile) {
        try {
            mWriter = new PrintWriter(outputFile.getPath()+mFileSuffix, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    protected final void dump(String output) {
        mWriter.println(output);
    }

    public final void warn(String warning) {
        System.err.println("Warning["+sTag+"]: "+warning);
    }

    public void close() {
        mWriter.close();
    }


    public static class Map {
        private HashMap<String, Class<? extends _Log>> mMap = new HashMap<String, java.lang.Class<? extends _Log>>();

        public Map(Pair... pairs) {
            for(Pair pair: pairs) {
                mMap.put(pair.getKey(), pair.getValue());
            }
        }

        public Class<? extends _Log> get(String key) {
            return mMap.get(key);
        }
    }

    public static class Pair {
        private String key;
        private Class<? extends _Log> value;

        public Pair(String _key, Class<? extends _Log> _value) {
            key = _key;
            value = _value;
        }

        public String getKey() {
            return key;
        }

        public Class<? extends _Log> getValue() {
            return value;
        }
    }
}
