package net.blurcast.geotracer_decoder.logger;



import com.oracle.javafx.jmx.json.JSONWriter;

import java.io.PrintWriter;

/**
 * Created by blake on 1/13/15.
 */
public class Json_Log extends _Hash_Log {

    JSONWriter mJson;

    public Json_Log() {
        mFileSuffix = ".json";
        sHashOpen = "{";
        sHashClose = "}";
        sHashDelim = ",";
        sHashGets = ":";
        sArrayOpen = "[";
        sArrayClose = "]";
        sArrayDelim = ",";
    }

    @Override
    public String key(String key) {
        return "\""+key.replace("\"", "").replaceAll("[^\\w\\-]", "")+"\"";
    }

    @Override
    public String value(String value) {
        return "\""+value.replaceAll("[\"\\t\\n]", "\\$1")+"\"";
    }

}
