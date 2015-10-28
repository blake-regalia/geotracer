package net.blurcast.tracer.callback;

import android.util.Log;

/**
 * Created by blake on 1/6/15.
 */
public class Attempt_GroupManager extends GroupManager<AttemptQueue> {

    private static final String TAG = Attempt_GroupManager.class.getSimpleName();

    public Attempt_GroupManager(int minorVariation) {
        super(minorVariation);
    }

    public void add(int major, int minor, Attempt attempt) {
        int key = (major * nMinorVariations) + minor;
        AttemptQueue attemptQueue;
        if((attemptQueue = mItems.get(key)) != null) {
            attemptQueue.add(attempt);
        }
        else {
            attemptQueue = new AttemptQueue();
            attemptQueue.add(attempt);
            mItems.put(key, attemptQueue);
        }
    }

}
