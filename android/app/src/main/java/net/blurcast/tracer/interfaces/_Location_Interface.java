package net.blurcast.tracer.interfaces;

import android.content.Context;
import android.os.Parcelable;

import net.blurcast.tracer.callback.Attempt;
import net.blurcast.tracer.encoder.location._Location_Encoder;
import net.blurcast.tracer.helper.ParcelableSensor;
import net.blurcast.tracer.logger._Logger;

/**
 * Created by blake on 12/29/14.
 */
public abstract class _Location_Interface<EncoderType extends _Location_Encoder, EventType extends Parcelable> extends _Interface<EncoderType, EventType> {

    public _Location_Interface(String title, String resourceKey, Class<EncoderType> encoderClass, Class<EventType> eventType) {
        super(title, resourceKey, encoderClass, eventType);
    }

    public abstract boolean isPrepared();
    public abstract void prepare(Attempt attempt);

    // optional override-able method for warning user we're about to stop
    public void prepareToStop(){}
}
