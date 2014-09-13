package uk.co.ordnancesurvey.elevation.android;

import java.util.Map;

import uk.co.ordnancesurvey.elevation.ElevationProvider;
import uk.co.ordnancesurvey.elevation.MaxSizeHashMap;

class CacheManager {
    private static final int MAX_CACHE_SIZE = 100;
    Map<String, String> mMap = new MaxSizeHashMap<String, String>(MAX_CACHE_SIZE);
    ElevationProvider mFileManager = new FileManager();

    public String getElevation(String easting, String northing) {
        String key = easting + northing;
        if (mMap.containsKey(key)) {
            return mMap.get(key);
        }
        return mFileManager.getElevation(easting, northing);
    }
}
