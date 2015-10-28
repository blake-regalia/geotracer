package net.blurcast.geotracer_decoder.callback;

/**
 * Created by blake on 1/11/15.
 */
public abstract class Eacher<KeyType, ItemType> {

    public abstract boolean each(KeyType id, ItemType item);

    public void after(){}
}
