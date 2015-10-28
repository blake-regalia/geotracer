package net.blurcast.geotracer_decoder.logger;

import java.io.PrintWriter;

/**
 * Created by blake on 1/14/15.
 */
public abstract class _Hash_Log extends _Log {

    protected String sHashOpen = "";
    protected String sHashClose = "";
    protected String sHashDelim = "\t";
    protected String sHashGets = " ";
    protected String sArrayOpen = "";
    protected String sArrayClose = "";
    protected String sArrayDelim = " ";




    protected StringBuilder buffer = new StringBuilder();

    public abstract String key(String key);
    public abstract String value(String value);

    public String value(boolean value) {
        return value+"";
    }
    public String value(int value) {
        return value((long) value);
    }
    public String value(long value) {
        return value+"";
    }
    public String value(float value) {
        return value((double) value);
    }
    public String value(double value) {
        return value+"";
    }


    public Builder object() {
        return new Builder(false, null);
    }

    public Builder array() {
        return new Builder(true, null);
    }

    public class Builder {
        private boolean isArray;
        private boolean empty = true;
        private _Hash_Log friend;
        private StringBuilder b;
        private Builder mParent = null;

        public Builder(boolean array, Builder parent) {
            isArray = array;
            friend = _Hash_Log.this;
            b = friend.buffer;
            b.append(isArray? sArrayOpen: sHashOpen);
            mParent = parent;
        }
        public Builder key(String key) {
            if(empty) empty = false;
            else b.append(sHashDelim);
            b.append(friend.key(key)).append(sHashGets);
            return this;
        }
        public Builder value(boolean value) {
            if(isArray && !empty) b.append(sArrayDelim);
            else empty = false;
            b.append(friend.value(value));
            return this;
        }
        public Builder value(int value) {
            if(isArray && !empty) b.append(sArrayDelim);
            else empty = false;
            b.append(friend.value(value));
            return this;
        }
        public Builder value(long value) {
            if(isArray && !empty) b.append(sArrayDelim);
            else empty = false;
            b.append(friend.value(value));
            return this;
        }
        public Builder value(float value) {
            if(isArray && !empty) b.append(sArrayDelim);
            else empty = false;
            b.append(friend.value(value));
            return this;
        }
        public Builder value(double value) {
            if(isArray && !empty) b.append(sArrayDelim);
            else empty = false;
            b.append(friend.value(value));
            return this;
        }
        public Builder value(String value) {
            if(isArray && !empty) b.append(sArrayDelim);
            else empty = false;
            b.append(friend.value(value));
            return this;
        }
        public Builder object() {
            if(isArray && !empty) b.append(sArrayDelim);
            else empty = false;
            return new Builder(false, this);
        }
        public Builder array() {
            if(isArray && !empty) b.append(sArrayDelim);
            else empty = false;
            return new Builder(true, this);
        }
        public Builder end() {
            b.append(isArray? sArrayClose: sHashClose);
            return mParent;
        }
        public Builder array(Runner runner) {
            Builder subArray = new Builder(true, null);
            runner.run(subArray);
            subArray.end();
            return this;
        }
    }

    public interface Runner {
        public void run(Builder object);
    }

    @Override
    public void close() {
        dump(buffer.toString());
        super.close();
    }

    @SuppressWarnings("unused")
    public static Class<? extends _Hash_Log> getDefaultLogClass() {
        return Json_Log.class;
    }
}
