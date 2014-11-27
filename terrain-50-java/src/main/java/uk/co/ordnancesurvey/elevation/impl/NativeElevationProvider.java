package uk.co.ordnancesurvey.elevation.impl;

public interface NativeElevationProvider {

    String getElevation(String x, String y);

    /**
     * set the next chain of responsibility provider
     */
    void setNext(NativeElevationProvider next);
}
