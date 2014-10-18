package uk.co.ordnancesurvey.elevation.impl;

import java.util.Map;

import uk.co.ordnancesurvey.elevation.ElevationProvider;

class CacheManager {
    private static final int MAX_CACHE_SIZE = 100;
    Map<String, String> mMap = new MaxSizeHashMap<String, String>(MAX_CACHE_SIZE);
    ElevationProvider mFileManager = new FileManager();

    public String getElevation(String easting, String northing) {
        String key = easting + ":" + northing;
        if (!mMap.containsKey(key)) {
            mMap.put(key, mFileManager.getElevation(easting, northing));
        }
        return mMap.get(key);
    }
}
