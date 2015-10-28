package net.blurcast.tracer.callback;

import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;

/**
 * Created by blake on 10/9/14.
 */
public abstract class Subscriber<EventType> {

    public abstract void event(EventType eventData, EventDetails eventDetails);

    public void notice(int noticeType, int noticeValue, Object noticeData) {}

    public void error(String error) {}

}
