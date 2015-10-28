package net.blurcast.tracer.callback;

/**
 * Created by blake on 1/6/15.
 */
public abstract class Expectation {

    public abstract void ready(int result);
    public void error(int reason) {}
}
