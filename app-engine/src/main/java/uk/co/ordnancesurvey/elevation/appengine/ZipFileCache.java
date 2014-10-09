package uk.co.ordnancesurvey.elevation.appengine;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.co.ordnancesurvey.elevation.ElevationProvider;
import uk.co.ordnancesurvey.elevation.MaxSizeHashMap;
import uk.co.ordnancesurvey.gis.BngTools;
import uk.co.ordnancesurvey.gis.EsriAsciiGrid;

class ZipFileCache implements ElevationProvider {

    private static final int MAX_CACHE_SIZE = 100;

    private static final Logger sLogger = Logger.getLogger(ZipFileCache.class.getName());

    private NetworkManager mNetworkManager = new NetworkManager();
    private Map<String, byte[]> mZipFileCache = new MaxSizeHashMap<String, byte[]>(MAX_CACHE_SIZE);

    public ZipFileCache() {
        restoreCache();
    }

    @Override
    public String getElevation(String easting, String northing) {
        try {
            // TODO: validate easting and northing values
            String filename = getFilename(easting, northing);

            byte[] zippedData = mZipFileCache.get(filename);

            if (zippedData == null) {
                File file = new File(filename);
                zippedData = mNetworkManager.download(file);
                mZipFileCache.put(filename, zippedData);
            }

            String asciiGrid = EsriAsciiGrid.getAsciiGrid(zippedData);
            return EsriAsciiGrid.getValue(easting, northing, asciiGrid);
        } catch (IOException e) {
            sLogger.log(Level.WARNING, "issues getting zipped elevation data", e);
            e.printStackTrace();
        }
        return String.valueOf(Float.MIN_VALUE);
    }

    private String getFilename(String easting, String northing) {
        String gridRef = BngTools.toGridReference(1, Double.parseDouble(easting),
                Double.parseDouble(northing)).replaceAll(" ", "");
        String characters = gridRef.substring(0, 2).toLowerCase();
        String x = gridRef.substring(2, 3);
        String y = gridRef.substring(3, 4);
        String name = characters + x + y +  "_OST50GRID_20130611";
        return name + ".zip";
    }

    private void restoreCache() {
        MemcacheService mMemcacheService = MemcacheServiceFactory.getMemcacheService();
        if (mMemcacheService.get(MemCache.KEY_ZIP_FILE) != null) {
            try {
                mZipFileCache = (Map<String, byte[]>) mMemcacheService.get(MemCache.KEY_ZIP_FILE);
            } catch (ClassCastException exc) {
                sLogger.log(Level.WARNING, "Error restoring object from memcache", exc);
            }
        } else {
            sLogger.log(Level.INFO, "Creating fresh zip file cache");
            mMemcacheService.put(MemCache.KEY_ZIP_FILE, mZipFileCache);
        }
    }
}
