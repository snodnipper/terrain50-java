package uk.co.ordnancesurvey.elevation;

import java.util.ArrayList;
import java.util.List;

import uk.co.ordnancesurvey.elevation.impl.CacheManager;
import uk.co.ordnancesurvey.elevation.provider.ElevationProvider;
import uk.co.ordnancesurvey.elevation.provider.epsg27700.terrain50.ElevationProviderBng;
import uk.co.ordnancesurvey.elevation.provider.epsg27700.terrain50.FileCache;
import uk.co.ordnancesurvey.elevation.provider.epsg27700.terrain50.FileCacheInMemory;
import uk.co.ordnancesurvey.elevation.provider.DataProvider;
import uk.co.ordnancesurvey.elevation.provider.epsg27700.terrain50.NetworkManager;
import uk.co.ordnancesurvey.elevation.impl.PrimaryCacheProvider;
import uk.co.ordnancesurvey.elevation.impl.SecondaryCacheProvider;
import uk.co.ordnancesurvey.elevation.transformation.Transformer;
import uk.co.ordnancesurvey.elevation.transformation.epsg27700.Transformer27700;

public class Configuration {

    private final ElevationProvider mElevationProvider;
    private List<Transformer> mTransformers;


    private Configuration(Builder builder) {
        mElevationProvider = builder.elevationProvider;
        mTransformers = builder.transformers;
    }

    public static class Builder {
        private String terrain50DataUrl = "";
        private PrimaryCacheProvider primaryCacheProvider;
        private SecondaryCacheProvider secondaryCacheProvider;
        private ElevationProvider elevationProvider = null;
        private List<Transformer> transformers = new ArrayList<Transformer>();
        private Strategy strategy = Strategy.CONSERVE_RESOURCE;
        private String terrain50FilenameSuffix = "";

        /**
         * @param val a coordinate transformer from global (WGS84 SRID 4326) lat/lon coordinates to
         *            native values for a particular provider.
         */
        public Builder addTransformer(Transformer val) {
            transformers.add(val);
            return this;
        }

        /**
         * @param val the key used to reference the level 1 lat/lon -> elevation map in app engine
         */
        public Builder setPrimaryCache(PrimaryCacheProvider val) {
            primaryCacheProvider = val;
            return this;
        }

        public Builder setStrategy(Strategy val) {
            strategy = val;
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

        /**
         * @param suffix e.g. _OST50GRID_20130611 for a file data/hu/hu55_OST50GRID_20130611.zip
         */
        public Builder terrain50FileSuffix(String suffix) {
            terrain50FilenameSuffix = suffix;
            return this;
        }

        public Configuration build() {
            DataProvider primaryDataProvider;
            DataProvider secondaryDataProvider;

            boolean hasPrimaryCache = primaryCacheProvider != null;
            if (hasPrimaryCache) {
                primaryDataProvider = new CacheManager(strategy, primaryCacheProvider);
            } else {
                primaryDataProvider = new CacheManager(strategy);
            }

            boolean hasSecondaryCache = secondaryCacheProvider != null;
            NetworkManager networkManager = new NetworkManager(terrain50DataUrl);
            if (hasSecondaryCache) {
                secondaryDataProvider = new FileCacheInMemory(strategy, networkManager,
                        secondaryCacheProvider, terrain50FilenameSuffix);
            } else {
                secondaryDataProvider = new FileCache(strategy, networkManager,
                        terrain50FilenameSuffix);
            }

            primaryDataProvider.setNext(secondaryDataProvider);

            elevationProvider = new ElevationProviderBng(primaryDataProvider);

            transformers.add(new Transformer27700());

            return new Configuration(this);
        }
    }

    public List<Transformer> getTransformers() {
        return mTransformers;
    }

    public ElevationProvider getElevationProvider() {
        return mElevationProvider;
    }
}
