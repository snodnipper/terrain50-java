package uk.co.ordnancesurvey.elevation;

import uk.co.ordnancesurvey.elevation.impl.CacheManager;
import uk.co.ordnancesurvey.elevation.impl.ElevationProviderBng;
import uk.co.ordnancesurvey.elevation.impl.FileCache;
import uk.co.ordnancesurvey.elevation.impl.FileCacheInMemory;
import uk.co.ordnancesurvey.elevation.impl.NativeElevationProvider;
import uk.co.ordnancesurvey.elevation.impl.NetworkManager;
import uk.co.ordnancesurvey.elevation.impl.PrimaryCacheProvider;
import uk.co.ordnancesurvey.elevation.impl.SecondaryCacheProvider;

public class Configuration {

    private final int mConcurrentFileRequests;
    private final ElevationProvider mElevationProvider;


    private Configuration(Builder builder){
        mConcurrentFileRequests = builder.concurrentFileRequests;
        mElevationProvider = builder.elevationProvider;
    }

    public static class Builder {
        private String terrain50DataUrl = "https://github.com/snodnipper/terrain50-java/raw/master/data/";
        private int concurrentFileRequests = 10;
        private PrimaryCacheProvider primaryCacheProvider;
        private SecondaryCacheProvider secondaryCacheProvider;
        private ElevationProvider elevationProvider = null;

        public Builder concurrentFileRequests(int val) {
            concurrentFileRequests = val;
            return this;
        }

        /**
         * @param val the key used to reference the level 1 lat/lon -> elevation map in app engine
         */
        public Builder setPrimaryCache(PrimaryCacheProvider val) {
            primaryCacheProvider = val;
            return this;
        }

        /**
         * @param val the key used to reference the level 2 filename -> file map in app engine
         *            (used instead of the default filesystem location)
         */
        public Builder setSecondaryCache(SecondaryCacheProvider val) {
            secondaryCacheProvider = val;
            return this;
        }

        public Builder terrain50DataUrl(String val) {
            terrain50DataUrl = val;
            return this;
        }

        public Configuration build() {
            NativeElevationProvider primaryElevationProvider;
            NativeElevationProvider secondaryElevationProvider;

            boolean hasPrimaryCache = primaryCacheProvider != null;
            if (hasPrimaryCache) {
                primaryElevationProvider = new CacheManager(primaryCacheProvider);
            } else {
                primaryElevationProvider = new CacheManager();
            }

            boolean hasSecondaryCache = secondaryCacheProvider != null;
            NetworkManager networkManager = new NetworkManager(terrain50DataUrl);
            if (hasSecondaryCache) {
                secondaryElevationProvider = new FileCacheInMemory(networkManager,
                        secondaryCacheProvider);
            } else {
                secondaryElevationProvider = new FileCache(networkManager);
            }

            primaryElevationProvider.setNext(secondaryElevationProvider);

            elevationProvider = new ElevationProviderBng(primaryElevationProvider);
            return new Configuration(this);
        }
    }

    public ElevationProvider getElevationProvider() {
        return mElevationProvider;
    }

    public int getConcurrentFileRequests() {
        return mConcurrentFileRequests;
    }
}
