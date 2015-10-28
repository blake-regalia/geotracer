package net.blurcast.geotracer_decoder.surrogate;

import net.blurcast.geotracer_decoder.callback.Eacher;

import java.util.Map;

/**
 * Created by blake on 1/11/15.
 */
public abstract class _Surrogate<KeyType, ItemType, EventType> {

    public EventType own;
    private Map<KeyType, ItemType> mEvents;

    protected _Surrogate(EventType _own, Map<KeyType, ItemType> events) {
        own = _own;
        mEvents = events;
    }

    public void each(Eacher<KeyType, ItemType> eacher) {
        for(Map.Entry<KeyType, ItemType> item: mEvents.entrySet()) {
            if(eacher.each(item.getKey(), item.getValue())) break;
        }
        eacher.after();
    }
}
