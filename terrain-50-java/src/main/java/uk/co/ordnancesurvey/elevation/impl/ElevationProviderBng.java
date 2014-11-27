package uk.co.ordnancesurvey.elevation.impl;

import uk.co.ordnancesurvey.elevation.ElevationProvider;
import uk.co.ordnancesurvey.gis.projection.Bng;

public class ElevationProviderBng implements ElevationProvider {

    private final NativeElevationProvider mNativeElevationProvider;

    public ElevationProviderBng(NativeElevationProvider nativeElevationProvider) {
        mNativeElevationProvider = nativeElevationProvider;
    }

    @Override
    public boolean containsLatLon(double latitude, double longitude) {
        boolean isWithinBritishNationalGrid = Bng.within(latitude, longitude);
        return isWithinBritishNationalGrid;
    }

    @Override
    public String getElevation(double x, double y) {
        long easting = Math.round(x);
        long northing = Math.round(y);
        return mNativeElevationProvider.getElevation(String.valueOf(easting),
                String.valueOf(northing));
    }

    @Override
    public String getElevationFromLatLon(double latitude, double longitude) {
        double[] eastingNorthing = Bng.toGridPoint(latitude, longitude);
        long easting = Math.round(eastingNorthing[0]);
        long northing = Math.round(eastingNorthing[1]);
        return mNativeElevationProvider.getElevation(String.valueOf(easting),
                String.valueOf(northing));
    }

    @Override
    public int getSpatialRefererence() {
        return 27700;
    }
}
