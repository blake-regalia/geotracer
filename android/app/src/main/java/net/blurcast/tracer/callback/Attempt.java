package net.blurcast.tracer.callback;

/**
 * Created by blake on 10/8/14.
 */
public abstract class Attempt {

    private Attempt mWrap;
    protected Object data;

    public Attempt() {}

    public Attempt(Attempt wrap) {
        mWrap = wrap;
    }

    public final void ready(Object _data) {
        data = _data;
        ready();
    };

    public abstract void ready();

    public void progress(int value) {
        if(mWrap != null) mWrap.progress(value);
    }

    public void update(String key, String value) {
        if(mWrap != null) mWrap.update(key, value);
    }

    public final void error() { if(mWrap != null) mWrap.error(0); else error(0); }
    public void error(int reason) {
        if(mWrap != null) mWrap.error(reason);
    }
}
