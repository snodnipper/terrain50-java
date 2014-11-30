package uk.co.ordnancesurvey.elevation.provider;

/**
 * A simple interface for internal data providers to use, which may be referenced by a
 * {@link uk.co.ordnancesurvey.elevation.provider.ElevationProvider}
 */
public interface DataProvider {

    String getElevation(String x, String y);

    /**
     * set the next chain of responsibility provider
     */
    void setNext(DataProvider next);
}
