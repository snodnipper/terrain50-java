package uk.co.ordnancesurvey.elevation;

/**
 * Elevation is the height above sea level, whereas altitude is the height above the surface of the
 * Earth at a particular location.
 */
public interface ElevationService {

    /**
     * String input values specified as decimal degrees
     */
    String getElevation(String latitude, String longitude);

    /**
     * input values specified as decimal degrees
     */
    String getElevation(double latitude, double longitude);

    /**
     * e.g. 591235 196785
     */
    String getElevationFromBng(double eastings, double northings);

    /**
     * @param gridReference e.g. TQ 9123 9678
     * @return a String value of the altitude
     */
    String getElevationFromBng(String gridReference);

    String getElevationFromBng(String easting, String northing);
}
