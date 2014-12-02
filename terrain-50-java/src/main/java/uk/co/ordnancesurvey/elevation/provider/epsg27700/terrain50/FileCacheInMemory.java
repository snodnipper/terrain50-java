package uk.co.ordnancesurvey.elevation.provider.epsg27700.terrain50;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.co.ordnancesurvey.elevation.provider.DataProvider;
import uk.co.ordnancesurvey.elevation.impl.SecondaryCacheProvider;
import uk.co.ordnancesurvey.elevation.provider.epsg27700.terrain50.util.MaxSizeHashMap;
import uk.co.ordnancesurvey.elevation.provider.epsg27700.gis.BngTools;
import uk.co.ordnancesurvey.elevation.provider.epsg27700.gis.EsriAsciiGrid;

public class FileCacheInMemory implements DataProvider {

    private static final int MAX_CACHE_SIZE = 100;

    private static final Logger LOGGER = Logger.getLogger(FileCacheInMemory.class.getName());

    private final NetworkManager mNetworkManager;
    private final Map<String, byte[]> mZipFileCache;

    /**
     * @param networkManager
     * @param secondaryCacheProvider
     */
    public FileCacheInMemory(NetworkManager networkManager, SecondaryCacheProvider secondaryCacheProvider) {
        Map<String, byte[]> defaultCache = new MaxSizeHashMap<String, byte[]>(MAX_CACHE_SIZE);
        mZipFileCache = secondaryCacheProvider.getSecondaryCache(defaultCache);
        mNetworkManager = networkManager;
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
        String name = characters + x + y +  "_OST50GRID_20130611";
        return name + ".zip";
    }
}
