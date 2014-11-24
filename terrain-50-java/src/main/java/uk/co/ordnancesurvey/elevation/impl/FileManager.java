package uk.co.ordnancesurvey.elevation.impl;


import com.google.common.util.concurrent.Striped;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.co.ordnancesurvey.elevation.ElevationProvider;
import uk.co.ordnancesurvey.gis.BngTools;
import uk.co.ordnancesurvey.gis.EsriAsciiGrid;

class FileManager implements ElevationProvider {

    private static final int STRIPE_COUNT = 10;
    private static final Logger LOGGER = Logger.getLogger(FileManager.class.getName());

    private final File mCacheDirectory;
    private final NetworkManager mNetworkManager;
    private Striped<Lock> mStripedLock = Striped.lazyWeakLock(STRIPE_COUNT);

    public FileManager() {
        mCacheDirectory = new File(System.getProperty("java.io.tmpdir"));
        mNetworkManager = new NetworkManager(mCacheDirectory);
    }

    @Override
    public String getElevation(String easting, String northing) {
        String filePath = getFilename(easting, northing);
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                download(file);
            }
            return EsriAsciiGrid.getValue(easting, northing, file);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "cannot get elevation from file", e);
        }
        return String.valueOf(Float.MIN_VALUE);
    }

    private void download(File file) throws IOException {
        Lock lock = mStripedLock.get(file.getName());
        lock.lock();
        try {
            if (!file.exists()) {
                mNetworkManager.download(file);
            }
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
