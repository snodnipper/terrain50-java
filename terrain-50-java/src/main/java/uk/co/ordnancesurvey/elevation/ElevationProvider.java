package uk.co.ordnancesurvey.elevation;

public interface ElevationProvider {

    /**
     * @return true if the provider contains elevation data for the given input
     */
    boolean containsLatLon(double latitude, double longitude);

    String getElevation(double x, double y);

    String getElevationFromLatLon(double latitude, double longitude);

    /**
     * @return the spatial reference ID
     */
    int getSpatialRefererence();
}
