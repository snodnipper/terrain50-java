package uk.co.ordnancesurvey.elevation.provider.epsg27700.terrain50.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class MaxSizeHashMap<K, V> extends LinkedHashMap<K, V> {
    private final int mMaxSize;

    public MaxSizeHashMap(int maxSize) {
        mMaxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > mMaxSize;
    }
}
