package uk.co.ordnancesurvey.elevation.provider.epsg27700.terrain50;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.Striped;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.co.ordnancesurvey.elevation.impl.Strategy;
import uk.co.ordnancesurvey.elevation.provider.DataProvider;
import uk.co.ordnancesurvey.elevation.impl.SecondaryCacheProvider;
import uk.co.ordnancesurvey.elevation.provider.epsg27700.terrain50.util.MaxSizeHashMap;
import uk.co.ordnancesurvey.elevation.provider.epsg27700.gis.BngTools;
import uk.co.ordnancesurvey.elevation.provider.epsg27700.gis.EsriAsciiGrid;

/**
 * Terrain 50 Zip Data Files are ~50KiB.  Thus we can store ~20 zipped files per MiB.  Assuming
 * the container is allocating 128MiB for this operation can allocate ~200 files (~100MiB).
 */
public class FileCacheInMemory implements DataProvider {

    private static final int MAX_CACHE_SIZE = 100;

    private static final Logger LOGGER = Logger.getLogger(FileCacheInMemory.class.getName());

    private final NetworkManager mNetworkManager;
    private final Cache<String, byte[]> mZipFileCache;
    private final String mTerrain50FilenameSuffix;
    private Striped<Lock> mStripedLock;

    /**
     * @param networkManager
     * @param secondaryCacheProvider
     */
    public FileCacheInMemory(Strategy strategy, NetworkManager networkManager,
                             SecondaryCacheProvider secondaryCacheProvider,
                             String terrain50FilenameSuffix) {
        mNetworkManager = networkManager;
        mTerrain50FilenameSuffix = terrain50FilenameSuffix;

        int maxCacheSize;
        int expiryHours;
        // loosely define the no. of concurrent downloads
        int stripCount;

        switch (strategy) {
            case CONSERVE_RESOURCE:
                expiryHours = 1;
                maxCacheSize = 20;
                stripCount = 2;
                break;
            case MAX_PERFORMANCE:
                expiryHours = 14;
                maxCacheSize = 200;
                stripCount = 100;
                break;
            default:
                throw new IllegalArgumentException("");
        }

        Map<String, byte[]> defaultCache = new MaxSizeHashMap<String, byte[]>(MAX_CACHE_SIZE);

        mZipFileCache = CacheBuilder.newBuilder().concurrencyLevel(4)
                .expireAfterAccess(expiryHours, TimeUnit.HOURS)
                .maximumSize(maxCacheSize)
                .build();
        mZipFileCache.asMap().putAll(secondaryCacheProvider
                .getSecondaryCache(defaultCache));

        mStripedLock = Striped.lazyWeakLock(stripCount);
    }

    @Override
    public String getElevation(String easting, String northing) {
        try {
            // TODO: validate easting and northing values
            String filename = getFilename(easting, northing);

            byte[] zippedData = mZipFileCache.getIfPresent(filename);

            if (zippedData == null) {
                File file = new File(filename);

                Lock lock = mStripedLock.get(file.getName());
                lock.lock();
                try {
                    zippedData = mNetworkManager.download(file);
                    mZipFileCache.put(filename, zippedData);
                } finally {
                    lock.unlock();
                }
            }

            String asciiGrid = EsriAsciiGrid.getAsciiGrid(zippedData);
            return EsriAsciiGrid.getValue(easting, northing, asciiGrid);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "issues getting zipped elevation data", e);
        }
        return String.valueOf(Float.MIN_VALUE);
    }

    @Override
    public void setNext(DataProvider next) {
        throw new UnsupportedOperationException("Lowest level - the buck stops here.");
    }

    private String getFilename(String easting, String northing) {
        String gridRef = BngTools.toGridReference(1, Double.parseDouble(easting),
                Double.parseDouble(northing)).replaceAll(" ", "");
        String characters = gridRef.substring(0, 2).toLowerCase();
        String x = gridRef.substring(2, 3);
        String y = gridRef.substring(3, 4);
        String name = characters + x + y + mTerrain50FilenameSuffix;
        return name + ".zip";
    }
}
