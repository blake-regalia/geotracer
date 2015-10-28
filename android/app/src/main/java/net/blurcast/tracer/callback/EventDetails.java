package net.blurcast.tracer.callback;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by blake on 10/10/14.
 */
public class EventDetails implements Parcelable {

    private long mTimeSpanStart;
    private long mTimeSpanEnd;
    private Set<String> aStringSet = new HashSet<String>(0);
    private String[] aStringArray;

    public EventDetails() {
        mTimeSpanStart = SystemClock.elapsedRealtime();
    }

    public EventDetails(Parcel in) {
        mTimeSpanStart = in.readLong();
        mTimeSpanEnd = in.readLong();
        int arraySize = in.readInt();
        aStringArray = new String[arraySize];
        in.readStringArray(aStringArray);
//        aStringSet = new HashSet<String>(Arrays.asList(aStrings));
    }

    public void reset() { mTimeSpanStart = SystemClock.elapsedRealtime(); }

    public long getStartTime() {
        return mTimeSpanStart;
    }

    public void endTimeSpan() {
        mTimeSpanEnd = SystemClock.elapsedRealtime();
    }

    public long[] getTimeSpan() {
        return new long[]{
                mTimeSpanStart,
                mTimeSpanEnd
        };
    }

    public void setStringSet(Set<String> set) {
        aStringSet = set;
    }

    public String[] getStringArray() {
        return aStringArray;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int i) {
        out.writeLong(mTimeSpanStart);
        out.writeLong(mTimeSpanEnd);
        out.writeInt(aStringSet.size());
        out.writeStringArray(aStringSet.toArray(new String[0]));
    }

    public static final Creator<EventDetails> CREATOR = new Creator<EventDetails>() {
        public EventDetails createFromParcel(Parcel parcel) {
            return new EventDetails(parcel);
        }

        public EventDetails[] newArray(int size) {
            return new EventDetails[size];
        }
    };
}
