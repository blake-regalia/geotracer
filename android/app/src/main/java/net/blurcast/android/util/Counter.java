package net.blurcast.android.util;

/**
 * Created by blake on 12/28/14.
 */
public class Counter {

    private int nLength;
    private int nCount = 0;

    public Counter(int length) {
        nLength = length;
    }

    public boolean plus() {
        nCount += 1;
        return (nCount >= nLength);
    }
}
