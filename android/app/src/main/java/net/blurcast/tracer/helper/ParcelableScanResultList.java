package net.blurcast.tracer.helper;

import android.net.wifi.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by blake on 1/7/15.
 */
public class ParcelableScanResultList extends ParcelableList<ScanResult> implements Parcelable {

    public ParcelableScanResultList(List<ScanResult> list) {
        super(list, ScanResult.class);
    }

    public ParcelableScanResultList(Parcel in) {
        super(in);
    }

    public static final Creator<ParcelableScanResultList> CREATOR = new Creator<ParcelableScanResultList>() {
        public ParcelableScanResultList createFromParcel(Parcel parcel) {
            return new ParcelableScanResultList(parcel);
        }

        public ParcelableScanResultList[] newArray(int size) {
            return new ParcelableScanResultList[size];
        }
    };

}
