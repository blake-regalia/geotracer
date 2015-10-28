package net.blurcast.geotracer_decoder.logger;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by blake on 1/13/15.
 */
public class Csv_Log  {

//    private static final Pattern R_NEEDS_ESCAPE = Pattern.compile("\"|\t|\n");
//
//    private boolean opened = false;
//
//    public Csv_Log() {
//        mFileSuffix = ".json";
//    }
//
//    @Override
//    public void setFields(String... keys) {
//        if (!opened) {
//            out(keys);
//            opened = true;
//        }
//    }
//
//    @Override
//    public void out(int... output) {
//        StringBuilder b = new StringBuilder();
//        for(int i=0, max_i=output.length-1; i<=max_i; i++) {
//            b.append(output[i]);
//            if(i < max_i) b.append(",");
//        }
//        dump(b.toString());
//    }
//
//    @Override
//    public void out(String... output) {
//        StringBuilder b = new StringBuilder();
//        for(int i=0, max_i=output.length-1; i<=max_i; i++) {
//            String chunk = output[i];
//            Matcher m = R_NEEDS_ESCAPE.matcher(chunk);
//            if(m.find()) {
//                chunk = chunk.replace("\"","\\\"").replace("\t"," ").replace("\n"," ");
//                b.append("\"").append(chunk).append("\"");
//            }
//            else {
//                b.append(chunk);
//            }
//            if(i < max_i) b.append(",");
//        }
//        dump(b.toString());
//    }

}
