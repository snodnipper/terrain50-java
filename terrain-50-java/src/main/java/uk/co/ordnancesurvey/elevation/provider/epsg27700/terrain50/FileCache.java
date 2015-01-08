package uk.co.ordnancesurvey.elevation.provider.epsg27700.terrain50;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.Striped;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.co.ordnancesurvey.elevation.ElevationService;
import uk.co.ordnancesurvey.elevation.Strategy;
import uk.co.ordnancesurvey.elevation.provider.epsg27700.gis.BngTools;
import uk.co.ordnancesurvey.elevation.provider.epsg27700.gis.EsriAsciiGrid;
import uk.co.ordnancesurvey.elevation.provider.DataProvider;

public class FileCache implements DataProvider {

    private static final Logger LOGGER = Logger.getLogger(FileCache.class.getName());
    private static final String CACHE_INTERNAL_NAME = "os-elevation-cache";

    private static int mStripCount;

    private final File mCacheDirectory;
    private final NetworkManager mNetworkManager;
    private final String mTerrain50FilenameSuffix;

    private Striped<Lock> mStripedLock;

    protected final Cache<String, String> mCache;

    public FileCache(Strategy strategy, NetworkManager networkManager,
                     String terrain50FilenameSuffix) {
        mTerrain50FilenameSuffix = terrain50FilenameSuffix;

        final long cacheDuration;
        final TimeUnit cacheTimeUnit;
        final int cacheMaxSize;

        switch (strategy) {
            case CONSERVE_RESOURCE:
                cacheDuration = 1;
                cacheTimeUnit = TimeUnit.HOURS;
                cacheMaxSize = 5;
                mStripCount = 2;
                break;
            case MAX_PERFORMANCE:
                cacheDuration = 14;
                cacheTimeUnit = TimeUnit.HOURS;
                cacheMaxSize = 50;
                mStripCount = 100;
                break;
            default:
                throw new IllegalStateException("unsupported strategy: "+ strategy);
        }
        mCacheDirectory = new File(System.getProperty("java.io.tmpdir") +
                File.separator +
                CACHE_INTERNAL_NAME);
        if (!mCacheDirectory.exists()) {
            boolean created = mCacheDirectory.mkdir();
            if (!created) {
                throw new IllegalStateException("cannot create cache directory");
            }
        }

        mNetworkManager = networkManager;
        mStripedLock = Striped.lazyWeakLock(mStripCount);

        mCache = CacheBuilder.newBuilder().concurrencyLevel(4)
                .maximumSize(cacheMaxSize)
                .expireAfterAccess(cacheDuration, cacheTimeUnit)
                .build();
    }

    @Override
    public String getElevation(String easting, String northing) {
        String filePath = getFilename(easting, northing);
        try {
            String asciiGrid = mCache.getIfPresent(filePath);
            boolean isCached = asciiGrid != null;

            if (!isCached) {
                File file = new File(filePath);
                if (!file.exists()) {
                    download(file);
                }
                asciiGrid = EsriAsciiGrid.getAsciiGrid(file);
                mCache.put(filePath, asciiGrid);
            }

            String result = EsriAsciiGrid.getValue(easting, northing, asciiGrid);
            if (result.isEmpty()) {
                return ElevationService.RESULT_UNKNOWN;
            }
            return result;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "cannot get elevation from file", e);
        }
        return ElevationService.RESULT_ERROR;
    }

    @Override
    public void setNext(DataProvider next) {
        throw new UnsupportedOperationException();
    }

    private void download(File file) throws IOException {
        Lock lock = mStripedLock.get(file.getName());
        lock.lock();
        try {
            if (!file.exists()) {
                byte[] bytes = mNetworkManager.download(file);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bytes);
                fos.close();
            }
        } catch (Exception lazy) {
            LOGGER.log(Level.SEVERE, "cannot download file", lazy);
        } finally {
            lock.unlock();
        }
    }

    private String getFilename(String easting, String northing) {
        String gridRef = BngTools.toGridReference(1, Double.parseDouble(easting),
                Double.parseDouble(northing)).replaceAll(" ", "");
        String characters = gridRef.substring(0, 2).toLowerCase();
        String x = gridRef.substring(2, 3);
        String y = gridRef.substring(3, 4);
        String name = characters + x + y +  mTerrain50FilenameSuffix;
        return mCacheDirectory + File.separator + name + ".zip";
    }
}
