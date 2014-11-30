package uk.co.ordnancesurvey.elevation.provider.epsg27700.terrain50;


import com.google.common.util.concurrent.Striped;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.co.ordnancesurvey.elevation.ElevationService;
import uk.co.ordnancesurvey.elevation.provider.epsg27700.gis.BngTools;
import uk.co.ordnancesurvey.elevation.provider.epsg27700.gis.EsriAsciiGrid;
import uk.co.ordnancesurvey.elevation.provider.DataProvider;

public class FileCache implements DataProvider {

    private static final Logger LOGGER = Logger.getLogger(FileCache.class.getName());
    private static final String CACHE_INTERNAL_NAME = "os-elevation-cache";

    private static int sStripCount = 10;

    private final File mCacheDirectory;
    private final NetworkManager mNetworkManager;
    private Striped<Lock> mStripedLock;

    public FileCache(NetworkManager networkManager) {
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
        mStripedLock = Striped.lazyWeakLock(sStripCount);
    }

    @Override
    public String getElevation(String easting, String northing) {
        String filePath = getFilename(easting, northing);
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                download(file);
            }
            String value = EsriAsciiGrid.getValue(easting, northing, file);
            if (value.isEmpty()) {
                return ElevationService.RESULT_UNKNOWN;
            }
            return value;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "cannot get elevation from file", e);
        }
        return ElevationService.RESULT_ERROR;
    }

    @Override
    public void setNext(DataProvider next) {
        throw new UnsupportedOperationException();
    }

    public static void setConcurrentDownloads(int concurrentDownloads) {
        sStripCount = concurrentDownloads;
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
        String name = characters + x + y +  "_OST50GRID_20130611";
        return mCacheDirectory + File.separator + name + ".zip";
    }
}
