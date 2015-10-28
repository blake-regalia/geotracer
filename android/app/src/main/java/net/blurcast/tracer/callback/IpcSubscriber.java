package net.blurcast.tracer.callback;

import android.os.Parcelable;
import android.provider.ContactsContract;

/**
 * Created by blake on 1/6/15.
 */
public abstract class IpcSubscriber<EventType extends Parcelable> extends Subscriber<EventType> {

    public abstract void event(EventType eventData, EventDetails eventDetails);

    public void notice(int noticeType, int noticeValue, Parcelable noticeData) {}

}
