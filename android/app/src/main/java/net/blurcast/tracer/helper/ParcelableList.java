package net.blurcast.tracer.helper;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by blake on 1/7/15.
 */
public class ParcelableList<ItemType extends Parcelable> implements Parcelable {

    String className;
    ItemType[] mArray;

    public ParcelableList(List<ItemType> list, Class<ItemType> itemTypeClass) {
        className = itemTypeClass.getCanonicalName();
        mArray = (ItemType[]) Array.newInstance(itemTypeClass, list.size());
        list.toArray(mArray);
    }

    public ParcelableList(Parcel in) {
        try {
            Class<ItemType> _class = (Class<ItemType>) Class.forName(in.readString());
            ClassLoader classLoader = _class.getClassLoader();
            ItemType[] blankArray = (ItemType[]) Array.newInstance(_class, 0);
            mArray = (ItemType[]) in.readArrayList(classLoader).toArray(blankArray);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int size() {
        return mArray.length;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int i) {
        out.writeString(className);
        out.writeList(new ArrayList<ItemType>(Arrays.asList(mArray)));
    }

    public static final Parcelable.Creator<ParcelableList> CREATOR = new Parcelable.Creator<ParcelableList>() {
        public ParcelableList createFromParcel(Parcel parcel) {
            return new ParcelableList(parcel);
        }

        public ParcelableList[] newArray(int size) {
            return new ParcelableList[size];
        }
    };

}
