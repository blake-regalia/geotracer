package net.blurcast.geotracer_decoder.helper;

/**
 * Created by blake on 1/17/15.
 */
public class Pair<KeyType, ValueType> {

    private KeyType key;
    private ValueType value;

    public Pair(KeyType _key, ValueType _value) {
        key = _key;
        value = _value;
    }

    public KeyType getKey() {
        return key;
    }

    public ValueType getValue() {
        return value;
    }
}
