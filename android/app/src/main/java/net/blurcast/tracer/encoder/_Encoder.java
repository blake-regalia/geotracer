package net.blurcast.tracer.encoder;

import android.os.Parcelable;

import net.blurcast.tracer.callback.Attempt;
import net.blurcast.tracer.callback.EventDetails;
import net.blurcast.tracer.callback.IpcSubscriber;
import net.blurcast.tracer.callback.Subscriber;
import net.blurcast.tracer.logger._Logger;

import java.nio.charset.Charset;

/**
 * Created by blake on 10/20/14.
 */
public abstract class _Encoder<EventType extends Parcelable> {

    public static final Charset CHARSET_ISO_8859_1 = Charset.forName("ISO-8859-1");

    protected _Logger mLogger;

    public _Encoder(_Logger logger) {
        mLogger = logger;
    }

    public _Logger getLogger() {
        return mLogger;
    }

    public void start() {
        this.start(null);
    }

    public void replaceCuriousSubscriber(IpcSubscriber<EventType> subscriber){}

    public abstract void start(IpcSubscriber<EventType> subscriber);
    public abstract void stop(Attempt attempt);

    protected final byte[] encode_hardware_address(String hardwareAddress) {

        // convert string into character array
        char[] address = hardwareAddress.toCharArray();

        // construct byte array "01:34:67:90:23:56"
        return new byte[] {
                (byte) ((Character.digit(address[0], 16) << 4) + Character.digit(address[1], 16)),
                (byte) ((Character.digit(address[3], 16) << 4) + Character.digit(address[4], 16)),
                (byte) ((Character.digit(address[6], 16) << 4) + Character.digit(address[7], 16)),
                (byte) ((Character.digit(address[9], 16) << 4) + Character.digit(address[10], 16)),
                (byte) ((Character.digit(address[12], 16) << 4) + Character.digit(address[13], 16)),
                (byte) ((Character.digit(address[15], 16) << 4) + Character.digit(address[16], 16)),
        };
    }
}
