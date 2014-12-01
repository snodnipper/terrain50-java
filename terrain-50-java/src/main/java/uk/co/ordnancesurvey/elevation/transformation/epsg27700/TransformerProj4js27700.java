package uk.co.ordnancesurvey.elevation.transformation.epsg27700;

import uk.co.ordnancesurvey.elevation.transformation.TransformerProj4js;

public class TransformerProj4js27700 extends TransformerProj4js {

    // TODO: check why these are different to http://spatialreference.org/ref/epsg/27700/
    private static final double MAX_LON = 3.63202114501;
    private static final double MAX_LAT = 61.4645902176;
    private static final double MIN_LON = -7.55716018087;
    private static final double MIN_LAT = 49.766807227;

    private static final String SRID = "EPSG:27700";
    private static final String SRID_DEFINITION = "+proj=tmerc +lat_0=49 +lon_0=-2 +k=0.9996012717 +x_0=400000 +y_0=-100000 +ellps=airy +datum=OSGB36 +units=m +no_defs";

    public TransformerProj4js27700() {
        super(SRID, SRID_DEFINITION);
    }

    @Override
    public boolean validFor(double lat, double lon) {
        return lat > MIN_LAT && lat < MAX_LAT && lon > MIN_LON && lon < MAX_LON;
    }
}
