package uk.co.ordnancesurvey.elevation.impl;

import java.util.Map;

import uk.co.ordnancesurvey.elevation.ElevationProvider;
import uk.co.ordnancesurvey.elevation.impl.util.MaxSizeHashMap;

public class CacheManager implements ElevationProvider {

    private static final int MAX_CACHE_SIZE = 100;

    protected Map<String, String> mMap = new MaxSizeHashMap<String, String>(MAX_CACHE_SIZE);
    ElevationProvider mNext;

    public CacheManager() {}

    public CacheManager(PrimaryCacheProvider primaryCacheProvider) {
        this();
        mMap = primaryCacheProvider.getPrimaryCache(mMap);
    }

    public String getElevation(String easting, String northing) {
        String key = easting + ":" + northing;

        if (!mMap.containsKey(key)) {
            mMap.put(key, mNext.getElevation(easting, northing));
        }
        return mMap.get(key);
    }

    public void setNext(ElevationProvider next) {
        mNext = next;
    }
}
