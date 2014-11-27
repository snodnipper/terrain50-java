package uk.co.ordnancesurvey.elevation.provider.epsg27700.terrain50;

import uk.co.ordnancesurvey.elevation.provider.ElevationProvider;
import uk.co.ordnancesurvey.elevation.SpatialReference;
import uk.co.ordnancesurvey.elevation.provider.DataProvider;

public class ElevationProviderBng implements ElevationProvider {

    private final DataProvider mDataProvider;

    public ElevationProviderBng(DataProvider dataProvider) {
        mDataProvider = dataProvider;
    }

    @Override
    public String getElevation(double x, double y) {
        long easting = Math.round(x);
        long northing = Math.round(y);
        return mDataProvider.getElevation(String.valueOf(easting),
                String.valueOf(northing));
    }

    @Override
    public String getSpatialRefererence() {
        return SpatialReference.EPSG_27700;
    }
}
