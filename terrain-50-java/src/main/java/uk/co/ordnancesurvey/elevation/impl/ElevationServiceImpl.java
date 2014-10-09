package uk.co.ordnancesurvey.elevation.impl;

import uk.co.ordnancesurvey.elevation.ElevationService;
import uk.co.ordnancesurvey.gis.BngTools;
import uk.co.ordnancesurvey.gis.Point;

public class ElevationServiceImpl implements ElevationService {

    private final CacheManager mCacheManager = new CacheManager();

    public String getElevation(double eastings, double northings) {
        return getElevation(String.valueOf(Math.round(eastings)),
                String.valueOf(Math.round(northings)));
    }

    public String getElevation(String gridReference) {
        Point point = BngTools.parseGridReference(gridReference);
        return getElevation(point.getX(), point.getY());
    }

    public String getElevation(String easting, String northing) {
        return mCacheManager.getElevation(easting, northing);
    }
}

