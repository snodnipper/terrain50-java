package uk.co.ordnancesurvey.elevation;

/**
 * Elevation is the height above sea level, whereas altitude is the height above the surface of the
 * Earth at a particular location.
 */
public interface ElevationService {

    /**
     * e.g. 591235 196785
     */
    String getElevation(double eastings, double northings);

    /**
     * @param gridReference e.g. TQ 9123 9678
     * @return a String value of the altitude
     */
    String getElevation(String gridReference);

    String getElevation(String easting, String northing);
}
