package uk.co.ordnancesurvey.elevation.appengine;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.co.ordnancesurvey.elevation.ElevationProvider;
import uk.co.ordnancesurvey.elevation.MaxSizeHashMap;

class CacheManager implements ElevationProvider {

    private static final int MAX_CACHE_SIZE = 100;

    private static final Logger sLogger = Logger.getLogger(ZipFileCache.class.getName());

    ElevationProvider mZipFileCache = new ZipFileCache();
    Map<String, String> mMap = new MaxSizeHashMap<String, String>(MAX_CACHE_SIZE);

    public CacheManager() {
        restoreCache();
    }

    public String getElevation(String easting, String northing) {
        String key = easting + ":" + northing;

        String elevation = mMap.get(key);
        if (elevation == null) {
            elevation = mZipFileCache.getElevation(easting, northing);
            mMap.put(key, elevation);
        }
        return elevation;
    }

    private void restoreCache() {
        MemcacheService mMemcacheService = MemcacheServiceFactory.getMemcacheService();
        if (mMemcacheService.get(MemCache.KEY_CACHE_MANAGER) != null) {
            try {
                mMap = (Map<String, String>) mMemcacheService.get(MemCache.KEY_CACHE_MANAGER);
            } catch (ClassCastException exc) {
                sLogger.log(Level.WARNING, "Error restoring object from memcache", exc);
            }
        } else {
            sLogger.log(Level.INFO, "Creating fresh cache manager cache");
            mMemcacheService.put(MemCache.KEY_CACHE_MANAGER, mMap);
        }
    }
}
