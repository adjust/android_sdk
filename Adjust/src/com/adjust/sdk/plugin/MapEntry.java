package com.adjust.sdk.plugin;

import java.util.Map;

/**
 * Created by pfms on 18/09/14.
 */
public class MapEntry<K, V> implements Map.Entry<K, V>{

    private K key;
    private V value;

    public MapEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V v) {
        this.value = v;
        return this.value;
    }
}
