package uk.co.ordnancesurvey.api.appengine;

import com.google.appengine.api.memcache.InvalidValueException;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceException;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.co.ordnancesurvey.elevation.impl.PrimaryCacheProvider;
import uk.co.ordnancesurvey.elevation.impl.SecondaryCacheProvider;

public class AppEngineMemCacheProvider implements PrimaryCacheProvider, SecondaryCacheProvider {

    protected static final String KEY_POINT_CACHE = "cache_points";
    protected static final String KEY_ZIP_FILE_CACHE = "cache_zip_files";

    private static final Logger LOGGER = Logger.getLogger(AppEngineMemCacheProvider.class.getName());

    @Override
    public Map<String, String> getPrimaryCache(Map<String, String> defaultValue) {
        MemcacheService mMemcacheService = MemcacheServiceFactory.getMemcacheService();

        Map<String, String> cacheMap = new MapRestorer<String, String>()
                .restoreMap(mMemcacheService, KEY_POINT_CACHE, defaultValue);

        return cacheMap;
    }

    public Map<String, byte[]> getSecondaryCache(Map<String, byte[]> defaultValue) {
        MemcacheService mMemcacheService = MemcacheServiceFactory.getMemcacheService();

        Map<String, byte[]> cacheMap = new MapRestorer<String, byte[]>()
                .restoreMap(mMemcacheService, KEY_ZIP_FILE_CACHE, defaultValue);

        return cacheMap;
    }

    private static class MapRestorer<K, V> {
        private Map<K, V> restoreMap(MemcacheService memcacheService, String key,
                                     Map<K, V> defaultValue) {
            // GET
            Object object = null;
            try {
                object = memcacheService.get(key);
            } catch (InvalidValueException invalidValueException) {
                LOGGER.log(Level.WARNING, "Error restoring object from memcache",
                        invalidValueException);
            } catch (NullPointerException nullPointerException) {
                LOGGER.log(Level.WARNING, "Cannot get value from memcache service " +
                        "(probably local execution)", nullPointerException);
            }

            // GET
            Map<K, V> memCacheMap = null;
            try {
                memCacheMap = (Map<K, V>) object;
            } catch (ClassCastException cce) {
                LOGGER.log(Level.WARNING, "Error casting object from memcache", cce);
                // cleaning
                try {
                    memcacheService.delete(object);
                } catch (IllegalArgumentException iae) {
                    LOGGER.log(Level.WARNING, "Error deleting junk object from memcache", cce);
                }
            }

            // Initialise if necessary
            if (memCacheMap == null) {
                try {
                    LOGGER.log(Level.INFO, "Creating fresh cache manager cache");
                    memcacheService.put(key, defaultValue);
                    memCacheMap = defaultValue;
                } catch (MemcacheServiceException memcacheServiceException) {
                    LOGGER.log(Level.WARNING, "Error setting memcache value",
                            memcacheServiceException);
                } catch (NullPointerException nullPointerException) {
                    LOGGER.log(Level.WARNING, "Cannot put value into memcache service " +
                            "(probably local execution)", nullPointerException);
                }
            }
            return memCacheMap;
        }
    }
}
