package uk.co.ordnancesurvey.elevation.appengine;

import uk.co.ordnancesurvey.elevation.ElevationService;
import uk.co.ordnancesurvey.gis.BngTools;
import uk.co.ordnancesurvey.gis.Point;

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

    public String getElevation(final String easting, final String northing) {
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
