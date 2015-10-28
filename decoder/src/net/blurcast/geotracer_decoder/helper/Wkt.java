package net.blurcast.geotracer_decoder.helper;

/**
 * Created by blake on 1/26/15.
 */
public abstract class Wkt {

    public static String point(double x, double y) {
        return "POINT("+x+" "+y+")";
    }

    public static String lineString(double[][] xys) {
        StringBuilder b = new StringBuilder("LINESTRING(");
        for(int i=0; i<xys.length; i++) {
            if(i != 0) b.append(",");
            b.append(xys[i][0]).append(" ").append(xys[i][1]);
        }
        return b.append(")").toString();
    }

    public interface Geometry {
        public String getWkt();
    }
}
