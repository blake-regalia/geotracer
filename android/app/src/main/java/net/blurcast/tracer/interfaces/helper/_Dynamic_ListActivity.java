package net.blurcast.tracer.interfaces.helper;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import net.blurcast.tracer.R;

/**
 * Created by blake on 1/8/15.
 */
public abstract class _Dynamic_ListActivity extends _ListActivity {

    @Override
    public void init() {

        // use simple list layout
        setContentView(R.layout.simple_list);
    }

    protected void createAdapter(int layout, String[] keys, int[] ids) {
        mBaseAdapter = mSimpleAdapter = new SimpleAdapter(this, mDisplayItems, layout, keys, ids);
        mSimpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if(view instanceof ProgressBar) {
                    int value = Integer.parseInt(data.toString());
                    ProgressBar progressBar = ((ProgressBar) view);
                    progressBar.setProgress(value);
                    return true;
                }
                return false;
            }
        });
        setListAdapter(mBaseAdapter);
    }

}
