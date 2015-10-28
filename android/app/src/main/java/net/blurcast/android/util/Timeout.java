package net.blurcast.android.util;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.util.Log;

public class Timeout {
	
	private static final int DEFAULT_SIZE = 128;
	private static int mSize = DEFAULT_SIZE;
	
	private static Timeout[] timeouts = new Timeout[mSize];
    private static final Timer mTimer = new Timer();

	private static int index = 0;

    private TimerTask mTimerTask;
	
	private Timeout(final int id, final Runnable task, long delay) {
		mTimerTask = new TimerTask() {
			@Override
			public void run() {
                timeouts[id] = null;
                task.run();
            }
		};
        mTimer.schedule(mTimerTask, delay);
	}

    private void cancel() {
        mTimerTask.cancel();
        mTimerTask = null;
    }

    public static int setTimeout(Runnable task, long delay) {
        int began = index;
        while(timeouts[index] != null) {
            index += 1;
            index %= mSize;
            if(index == began) {
                System.err.println("ERROR: RAN OUT OF TIMEOUTS");
            }
        }
        timeouts[index] = new Timeout(index, task, delay);
        return index;
    }

    // run on ui thread
    public static int setTimeout(final Runnable task, long delay, final Activity activity) {
        return setTimeout(new Runnable() {
            public void run() {
                activity.runOnUiThread(task);
            }
        }, delay);
    }

	
	public static int clearTimeout(int id) {
		if(id == -1) return -1;
		
		Timeout timeout = timeouts[id];
		if(timeout == null) return -1;
		
		timeout.cancel();
		timeouts[id] = null;

		return -1;
	}
}
