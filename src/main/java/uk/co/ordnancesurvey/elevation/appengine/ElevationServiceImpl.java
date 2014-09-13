package uk.co.ordnancesurvey.elevation.appengine;

import uk.co.ordnancesurvey.elevation.ElevationService;
import uk.co.ordnancesurvey.elevation.appengine.gis.BngTools;
import uk.co.ordnancesurvey.elevation.appengine.gis.Point;

public class ElevationServiceImpl implements ElevationService {

    private final CacheManager mCacheManager = new CacheManager();

    public ElevationServiceImpl() {
    }

    public String getElevation(double eastings, double northings) {
        return getElevation(String.valueOf(Math.round(eastings)),
                String.valueOf(Math.round(northings)));
    }

    /**
     * @param gridReference e.g. TQ 9123 9678
     * @return a String value of the altitude
     */
    public String getElevation(String gridReference) {
        Point point = BngTools.parseGridReference(gridReference);
        return getElevation(point.getX(), point.getY());
    }

    public String getElevation(String easting, String northing) {
        return mCacheManager.getElevation(easting, northing);
    }
}
