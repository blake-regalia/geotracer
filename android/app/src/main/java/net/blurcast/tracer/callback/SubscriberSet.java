package net.blurcast.tracer.callback;

import android.os.Bundle;
import android.os.Message;

import java.util.ArrayList;

/**
 * Created by blake on 10/9/14.
 */
public class SubscriberSet<EventType> {

    private ArrayList<Subscriber<EventType>> mSubscriberList = new ArrayList<Subscriber<EventType>>();

    public void add(Subscriber<EventType> subscriber) {
        mSubscriberList.add(subscriber);
    }

    public boolean remove(Subscriber<EventType> subscriber) {
        mSubscriberList.remove(subscriber);
        return mSubscriberList.isEmpty();
    }

    public void empty() {
        mSubscriberList.clear();
    }

    public void event(EventType eventData, EventDetails eventDetails) {
        for(Subscriber<EventType> subscriber: mSubscriberList) {
            subscriber.event(eventData, eventDetails);
        }
    }

    public void notice(int noticeType, int noticeValue, Object data) {
        for(Subscriber subscriber: mSubscriberList) {
            subscriber.notice(noticeType, noticeValue, data);
        }
    }

    public void error(String error) {
        for(Subscriber subscriber: mSubscriberList) {
            subscriber.error(error);
        }
    }

}
