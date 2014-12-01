package uk.co.ordnancesurvey.elevation.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import uk.co.ordnancesurvey.elevation.ElevationService;
import uk.co.ordnancesurvey.elevation.provider.DataProvider;

public class CacheManager implements DataProvider {

    private static final Logger LOGGER = Logger.getLogger(CacheManager.class.getName());

    private static final int ERROR_EXPIRY = 1;
    private static final int MAP_EXPIRY = 5;
    private static final int MAX_CACHE_SIZE = 1000;

    protected final Cache<String, String> mMap;
    private final Cache<String, Long> mCircuitBreaker;

    private DataProvider mNext;

    public CacheManager() {
        mMap = CacheBuilder.newBuilder().concurrencyLevel(4)
                .maximumSize(MAX_CACHE_SIZE)
                .expireAfterAccess(MAP_EXPIRY, TimeUnit.MINUTES)
                .build();

        mCircuitBreaker = CacheBuilder.newBuilder()
                .concurrencyLevel(4)
                .maximumSize(MAX_CACHE_SIZE)
                .expireAfterWrite(ERROR_EXPIRY, TimeUnit.MINUTES)
                .build();
    }

    public CacheManager(PrimaryCacheProvider primaryCacheProvider) {
        this();
        mMap.putAll(primaryCacheProvider.getPrimaryCache(mMap.asMap()));
    }

    public String getElevation(String x, String y) {
        String key = x + ":" + y;

        boolean circuitBreakerActive = mCircuitBreaker.getIfPresent(key) != null;
        if (circuitBreakerActive) {
            return ElevationService.RESULT_ERROR;
        }

        String result = mMap.getIfPresent(key);
        final boolean hadResult = result != null;
        if (!hadResult) {
            result = mNext.getElevation(x, y);
            mMap.put(key, result);
        }

        if (result.equals(ElevationService.RESULT_ERROR)) {
            mCircuitBreaker.put(key, System.currentTimeMillis());
        }

        return result;
    }

    public void setNext(DataProvider next) {
        mNext = next;
    }
}
