package uk.co.ordnancesurvey.elevation.impl;

import java.util.Map;

import uk.co.ordnancesurvey.elevation.impl.util.MaxSizeHashMap;

public class CacheManager implements NativeElevationProvider {

    private static final int MAX_CACHE_SIZE = 100;

    protected Map<String, String> mMap = new MaxSizeHashMap<String, String>(MAX_CACHE_SIZE);
    private NativeElevationProvider mNext;

    public CacheManager() {}

    public CacheManager(PrimaryCacheProvider primaryCacheProvider) {
        this();
        mMap = primaryCacheProvider.getPrimaryCache(mMap);
    }

    public String getElevation(String x, String y) {
        String key = x + ":" + y;

        if (!mMap.containsKey(key)) {
            mMap.put(key, mNext.getElevation(x, y));
        }
        return mMap.get(key);
    }

    public void setNext(NativeElevationProvider next) {
        mNext = next;
    }
}
