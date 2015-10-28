package net.blurcast.tracer.interfaces.helper;

import android.widget.SimpleAdapter;

import net.blurcast.tracer.R;

import java.util.HashMap;

/**
 * Created by blake on 1/5/15.
 */
public abstract class _Simple_ListActivity extends _ListActivity implements Offspring.Proxy {

    // sets content view and binds default adapter
    @Override
    public final void init() {
        setContentView(R.layout.simple_list);

        mBaseAdapter = new SimpleAdapter(this, mDisplayItems, R.layout.list_item,
                new String[]{
                        "name", "value", "subtitle",
                },
                new int[]{
                        R.id.app_data_list_item_name, R.id.app_data_list_item_value, R.id.app_data_list_item_subtitle
                });

        setListAdapter(mBaseAdapter);
    }

    // helper functions

    //
    public final void addBasicListItem(String key, String name, String subtitle) {
        addBasicListItem(key, name, subtitle, "");
    }

    //
    public final void addBasicListItem(String key, String name, String subtitle, String value) {

        // create a new row
        HashMap<String, String> row = new HashMap<String, String>();
        row.put("name", name);
        row.put("value", value);
        row.put("subtitle", subtitle);

        // add this row to a lookup hash
        mItemLookup.put(key, row);

        // push it to the list of rows
        mDisplayItems.add(row);
    }

    //
    protected void setValue(final String key, final String value) {
        HashMap<String, String> row;
        if((row = mItemLookup.get(key)) != null) {
            row.put("value", value);
        }
        mBaseAdapter.notifyDataSetChanged();
    }


}
