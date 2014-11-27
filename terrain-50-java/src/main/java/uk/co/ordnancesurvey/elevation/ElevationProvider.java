package uk.co.ordnancesurvey.elevation;

public interface ElevationProvider {

    String getElevation(String easting, String northing);

    /**
     * set the next chain of responsibility provider
     */
    void setNext(ElevationProvider next);
}
