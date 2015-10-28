package net.blurcast.geotracer_decoder.helper;

import net.blurcast.geotracer_decoder.logger._Trusty_Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by blake on 1/23/15.
 */
public class Sql {

    public static final String TABLE_DEVICE = "device";
    public static final String TABLE_TRACE = "trace";
    public static final String TABLE_TEMPERATURE = "temp";
    public static final String TABLE_WAP = "wap";
    public static final String TABLE_WAP_SAMPLE = "wap_sample";

    private static String _F(String field) {
        return "\""+field+"\"";
    }

    private static String _A(Collection<String> things) {
        return _J(things, " and ");
    }

    private static String _J(Collection<String> things) {
        return _J(things, ",");
    }

    private static String _J(Collection<String> things, String join) {
        StringBuilder b = new StringBuilder();
        boolean first = true;
        for(String thing: things) {
            if(!first) b.append(join);
            else first = false;
            b.append(thing);
        }
        return b.toString();
    }

    private static String _E(String value) {
        return "'"+value.replaceAll("'","\\'")+"'";
    }

    public static Core core() {
        return new Core(null, null);
    }
    public static Core core(String table) {
        return new Core(null, table);
    }
    public static Core core(String table, _Trusty_Log log) {
        return new Core(log, table);
    }

    public static class Core {
        private _Trusty_Log mLog;
        private String sTable;
        private ArrayList<String> mFields = new ArrayList<String>();
        private ArrayList<String> mValues = new ArrayList<String>();

        private boolean bSelectMode = false;
        private HashSet<String> mSelectTablesUsed = new HashSet<String>();
        private ArrayList<String> mSelectWheres = new ArrayList<String>();

        public Core(_Trusty_Log log, String table) {
            mLog = log;
            sTable = table;
        }

        public Expect field(String field) {
            mFields.add(_F(field));
            return new Expect();
        }

        public ArrayList<String> getWheres() {
            ArrayList<String> wheres = new ArrayList<String>();
            for(int i=0; i<mValues.size(); i++) {
                wheres.add(_F(sTable)+"."+mFields.get(i)+"="+mValues.get(i));
            }
            return wheres;
        }

        public Core insert() {
            insert(true);
            return Core.this;
        }

        private String insert(boolean writeToLog) {
            StringBuilder b = new StringBuilder();
            b.append("insert into ")
                    .append(_F(sTable))
                    .append("(").append(_J(mFields)).append(") ");
            if(!bSelectMode) {
                b.append("values(").append(_J(mValues)).append(");");
            }
            else {
                b.append("select ").append(_J(mValues))
                        .append(" from ").append(_J(mSelectTablesUsed))
                        .append(" where ").append(_A(mSelectWheres))
                        .append(";");
            }
            String statement = b.toString();
            if(writeToLog && mLog != null) mLog.out(statement);
            return statement;
        }

        public Core insert(Core expand) {
            for(String field: mFields) {
                expand.mFields.add(field);
            }
            for(String value: mValues) {
                expand.mValues.add(value);
            }
            expand.sTable = sTable;
            String statement = expand.insert(false);
            if(mLog != null) mLog.out(statement);
            return Core.this;
        }

        public class Expect {

            public Core value(long value) {
                mValues.add(value+"");
                return Core.this;
            }

            public Core value(double value) {
                mValues.add(value+"");
                return Core.this;
            }

            public Core value(String value) {
                mValues.add(_E(value));
                return Core.this;
            }

            public Core timestamp(long ts) {
                mValues.add("to_timestamp("+ts+")");
                return Core.this;
            }

            public Core timestamp(double ts) {
                mValues.add("to_timestamp("+String.format("%.2f", ts)+")");
                return Core.this;
            }

            public Core interval(long ts) {
                mValues.add("'"+ts+"' milliseconds::interval");
                return Core.this;
            }

            public Core interval(double ts) {
                mValues.add("'"+ts+"' milliseconds::interval");
                return Core.this;
            }

            public Core geom(String wkt, int srid) {
                mValues.add("ST_GeomFromText('"+wkt+"',"+srid+")");
                return Core.this;
            }

            public Core geom(Wkt.Geometry geometry) {
                mValues.add("ST_GeomFromText('"+geometry.getWkt()+"',4326)");
                return Core.this;
            }

            public Core array(String[] values) {
                StringBuilder b = new StringBuilder();
                for(String value: values) {
                    b.append(_E(value));
                }
                mValues.add(b.toString());
                return Core.this;
            }

            public Core using(Core basis, String field) {
                bSelectMode = true;
                mValues.add(_F(basis.sTable)+"."+_F(field));
                mSelectTablesUsed.add(_F(basis.sTable));
                mSelectWheres.addAll(basis.getWheres());
                return Core.this;
            }
        }
    }
}
