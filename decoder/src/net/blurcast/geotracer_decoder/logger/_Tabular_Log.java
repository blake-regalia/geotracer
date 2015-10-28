package net.blurcast.geotracer_decoder.logger;

/**
 * Created by blake on 1/14/15.
 */
public abstract class _Tabular_Log<BuilderType extends _Tabular_Log.Builder> extends _Log {

//    public abstract void setFields(String... fields);
//
//    public abstract void out(String... output);
//
//    public void out(int... output) {
//        String[] mirror = new String[output.length];
//        for (int i = 0; i < output.length; i++) {
//            mirror[i] = output[i] + "";
//        }
//        out(mirror);
//    }

    public abstract BuilderType table(String tableName);

    public abstract class Builder {
        protected String sTableName;
        protected StringBuilder f = new StringBuilder();
        protected StringBuilder v = new StringBuilder();

        public Builder(String tableName) {
            sTableName = tableName;
        }

        public abstract BuilderType field(String name);
        public abstract BuilderType cell(String cell);
        public abstract BuilderType value(String value);

        public abstract void save();

        public BuilderType row() {
            this.save();
            return _Tabular_Log.this.table(sTableName);
        }
    }

}
