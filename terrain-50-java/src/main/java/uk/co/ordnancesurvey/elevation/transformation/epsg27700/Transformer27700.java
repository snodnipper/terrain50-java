package uk.co.ordnancesurvey.elevation.transformation.epsg27700;

import uk.co.ordnancesurvey.elevation.SpatialReference;
import uk.co.ordnancesurvey.elevation.provider.epsg27700.gis.projection.Bng;
import uk.co.ordnancesurvey.elevation.transformation.Transformer;

public class Transformer27700 implements Transformer {
    @Override
    public boolean validFor(double latitude, double longitude) {
        boolean isWithinBritishNationalGrid = Bng.within(latitude, longitude);
        return isWithinBritishNationalGrid;
    }

    @Override
    public String getSpatialReference() {
        return SpatialReference.EPSG_27700;
    }

    @Override
    public double[] transform(double latitude, double longitude) {
        double[] eastingNorthing = Bng.toGridPoint(latitude, longitude);
        return eastingNorthing;
    }
}
