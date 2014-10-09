package uk.co.ordnancesurvey.elevation.android;


import java.io.File;
import java.io.IOException;

import uk.co.ordnancesurvey.elevation.ElevationProvider;
import uk.co.ordnancesurvey.gis.BngTools;
import uk.co.ordnancesurvey.gis.EsriAsciiGrid;

class FileManager implements ElevationProvider {

    private final File mCacheDirectory;
    private final NetworkManager mNetworkManager;

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
                mNetworkManager.download(file);
            }
            return EsriAsciiGrid.getValue(easting, northing, file);
        } catch (IOException e) {
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
        return mCacheDirectory + File.separator + name + ".zip";
    }
}
