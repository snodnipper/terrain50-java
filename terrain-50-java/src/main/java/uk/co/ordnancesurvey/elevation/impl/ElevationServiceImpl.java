package uk.co.ordnancesurvey.elevation.impl;

import uk.co.ordnancesurvey.elevation.ElevationService;
import uk.co.ordnancesurvey.gis.BngTools;
import uk.co.ordnancesurvey.gis.Point;
import uk.co.ordnancesurvey.gis.projection.Bng;

public class ElevationServiceImpl implements ElevationService {

    private final CacheManager mCacheManager = new CacheManager();

    public String getElevation(final String latitude, final String longitude) {
        double latitudeToUse = Double.parseDouble(latitude);
        double longitudeToUse = Double.parseDouble(longitude);
        return getElevation(latitudeToUse, longitudeToUse);
    }

    public String getElevation(double latitude, double longitude) {
        double[] eastingNorthing = Bng.toGridPoint(latitude, longitude);
        return getElevationFromBng(eastingNorthing[0], eastingNorthing[1]);
    }

    public String getElevationFromBng(double eastings, double northings) {
        return getElevationFromBng(String.valueOf(Math.round(eastings)),
                String.valueOf(Math.round(northings)));
    }

    public String getElevationFromBng(String gridReference) {
        Point point = BngTools.parseGridReference(gridReference);
        return getElevation(point.getX(), point.getY());
    }

    public String getElevationFromBng(String easting, String northing) {
        return mCacheManager.getElevation(easting, northing);
    }
}

