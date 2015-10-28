package net.blurcast.tracer.interfaces.helper;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.widget.BaseAdapter;
import android.widget.SimpleAdapter;

import net.blurcast.tracer.callback.IpcSubscriber;
import net.blurcast.tracer.callback.Subscriber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by blake on 1/8/15.
 */
public abstract class _ListActivity extends ListActivity implements Offspring.Proxy {

    private Offspring mOffspring;

    protected BaseAdapter mBaseAdapter;
    protected SimpleAdapter mSimpleAdapter;
    protected List<HashMap<String, String>> mDisplayItems = new ArrayList<HashMap<String, String>>();
    protected HashMap<String, HashMap<String, String>> mItemLookup = new HashMap<String, HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOffspring = new Offspring(this);
        mOffspring.ready();
    }

    // implementing methods

    public Activity getActivity() {
        return this;
    }

    // handle subscribe requests
    public final void subscribe(IpcSubscriber subscriber) {
        mOffspring.subscribe(subscriber);
    }

    // needs to be overriden by subclass
    public abstract void init();
    public abstract void setup();
}
