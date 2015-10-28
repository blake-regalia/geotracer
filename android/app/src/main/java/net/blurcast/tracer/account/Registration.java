package net.blurcast.tracer.account;

import android.content.Context;
import android.telephony.TelephonyManager;

import net.blurcast.android.factory.DeviceUuidFactory;

/**
 * Created by blake on 1/13/15.
 */
public class Registration {

    public static String getUniqueId(Context context) {
        return new DeviceUuidFactory(context).getDeviceUuid().toString();
    }
}
