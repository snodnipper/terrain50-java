package uk.co.ordnancesurvey.elevation.provider;

public interface ElevationProvider {

    /**
     * @param x the native X value for this SRID
     * @param y the native Y value for this SRID
     * @return
     */
    String getElevation(double x, double y);

    /**
     * @return the spatial reference
     */
    String getSpatialRefererence();
}
