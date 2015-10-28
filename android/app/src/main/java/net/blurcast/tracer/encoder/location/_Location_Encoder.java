package net.blurcast.tracer.encoder.location;

import android.content.Context;
import android.location.Location;
import android.os.Parcelable;

import net.blurcast.tracer.callback.Attempt;
import net.blurcast.tracer.callback.Subscriber;
import net.blurcast.tracer.encoder._Encoder;
import net.blurcast.tracer.helper.ParcelableSensorEvent;
import net.blurcast.tracer.logger._Logger;

/**
 * Created by blake on 10/20/14.
 */
public abstract class _Location_Encoder<EventType extends Parcelable> extends _Encoder<EventType> {

    protected Context mContext;

    public _Location_Encoder(Context context, _Logger logger) {
        super(logger);
        mContext = context;
    }

    public abstract void prepare(Attempt attempt);
    public abstract boolean isPrepared();

}
