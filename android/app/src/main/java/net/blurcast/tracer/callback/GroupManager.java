package net.blurcast.tracer.callback;

import android.util.Log;
import android.util.SparseArray;

/**
 * Created by blake on 1/6/15.
 */
public class GroupManager<ItemType> {

    private static final String TAG = GroupManager.class.getSimpleName();

    protected SparseArray<ItemType> mItems = new SparseArray<ItemType>();
    protected int nMinorVariations;

    public GroupManager(int minorVariations) {
        nMinorVariations = minorVariations;
    }

    public void add(int major, int minor, ItemType item) {
        int key = (major * nMinorVariations) + minor;
        mItems.put(key, item);
    }

    public ItemType fetch(int major, int minor) {
        ItemType item = mItems.get((major * nMinorVariations) + minor);
        if(item == null) Log.e(TAG, "No "+item.getClass().getSimpleName()+" objects found at {"+major+","+minor+"}");
        return item;
    }

}
