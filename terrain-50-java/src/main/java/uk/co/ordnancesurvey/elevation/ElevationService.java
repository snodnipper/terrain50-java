package uk.co.ordnancesurvey.elevation;

/**
 * Elevation is the height above sea level, whereas altitude is the height above the surface of the
 * Earth at a particular location.
 */
public interface ElevationService {

    public static final int SRID_4326 = 4326;
    public static final int SRID_27700 = 27700;

    public static final String RESULT_UNKNOWN = "";
    public static final String RESULT_ERROR = "ERROR";

    /**
     * @param latitude specified in decimal degrees
     * @param longitude specified in decimal degrees
     * @return the value of elevation in meters or "" if the request was successful _but_ the data
     * is simply unknown or "ERROR" caused by an underlying problem
     */
    String getElevation(String latitude, String longitude);


    /**
     * input values specified as decimal degrees
     */
    String getElevation(double latitude, double longitude);

    /**
     * @param srid the spatial reference of the input coordinates
     * @param x the x coordinate of the requested elevation point
     * @param y the y coordinate of the requested elevation point
     * @return
     */
    String getElevation(int srid, String x, String y);

    String getElevation(int srid, double x, double y);
}
