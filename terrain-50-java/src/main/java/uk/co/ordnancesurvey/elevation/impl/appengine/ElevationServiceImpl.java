package uk.co.ordnancesurvey.elevation.impl.appengine;

import uk.co.ordnancesurvey.elevation.ElevationService;
import uk.co.ordnancesurvey.gis.BngTools;
import uk.co.ordnancesurvey.gis.Point;
import uk.co.ordnancesurvey.gis.projection.Bng;

public class ElevationServiceImpl implements ElevationService {

    private final CacheManager mCacheManager = new CacheManager();

    public ElevationServiceImpl() {}

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

    /**
     * @param gridReference e.g. TQ 9123 9678
     * @return a String value of the altitude
     */
    public String getElevationFromBng(String gridReference) {
        Point point = BngTools.parseGridReference(gridReference);
        return getElevation(point.getX(), point.getY());
    }

    public String getElevationFromBng(final String easting, final String northing) {
        String eastingToUse = getCorrectedValue(easting);
        String northingToUse = getCorrectedValue(northing);
        if (eastingToUse == null || northingToUse == null) {
            return "";
        }
        return mCacheManager.getElevation(eastingToUse, northingToUse);
    }

    /**
     * no interpolation so we are rounding down to an integer value
     */
    private String getCorrectedValue(String number) {
        Long result = null;
        if (isInteger(number)) {
            result = Long.parseLong(number);
        } else if (isDouble(number)) {
            result = Math.round(Double.parseDouble(number));
        }
        return result == null ? null : String.valueOf(result);
    }

    private static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
        } catch(NumberFormatException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }
}
