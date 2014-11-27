package uk.co.ordnancesurvey.elevation;

import uk.co.ordnancesurvey.gis.BngTools;
import uk.co.ordnancesurvey.gis.Point;
import uk.co.ordnancesurvey.gis.projection.Bng;

public class ElevationServiceImpl implements ElevationService {

    private ElevationProvider mNext;

    public ElevationServiceImpl(ElevationProvider elevationProvider) {
        mNext = elevationProvider;
    }

    public String getElevation(final String latitude, final String longitude) {
        double latitudeToUse = Double.parseDouble(latitude);
        double longitudeToUse = Double.parseDouble(longitude);
        return getElevation(latitudeToUse, longitudeToUse);
    }

    public String getElevation(double latitude, double longitude) {
        if (Bng.within(latitude, longitude)) {
            double[] eastingNorthing = Bng.toGridPoint(latitude, longitude);
            return getElevationFromBng(eastingNorthing[0], eastingNorthing[1]);
        } else {
            return ElevationService.RESULT_UNKNOWN;
        }
    }

    public String getElevation(int srid, String x, String y) {
        switch (srid) {
            case SRID_4326:
                return getElevation(x, y);
            case SRID_27700:
                return getElevationFromBng(x, y);
            default:
                throw new IllegalArgumentException("unsupported spatial reference: " + srid);
        }
    }

    @Override
    public String getElevation(int srid, double x, double y) {
        switch (srid) {
            case SRID_4326:
                return getElevation(x, y);
            case SRID_27700:
                return getElevationFromBng(x, y);
            default:
                throw new IllegalArgumentException("unsupported spatial reference: " + srid);
        }
    }

    public String getElevationFromBng(String gridReference) {
        Point point = BngTools.parseGridReference(gridReference);
        return getElevationFromBng(point.getX(), point.getY());
    }

    public String getElevationFromBng(String easting, String northing) {
        try {
            return getElevationFromBng(Double.valueOf(easting), Double.valueOf(northing));
        } catch (NumberFormatException nfe) {
            return ElevationService.RESULT_ERROR;
        }
    }

    public String getElevationFromBng(double easting, double northing) {
        return getElevationFromBngWithValidation(easting, northing);
    }

    private String getElevationFromBngWithValidation(double easting, double northing) {
        if (Bng.validExtent(easting, northing)) {
            return mNext.getElevation(String.valueOf(easting), String.valueOf(northing));
        } else {
            return ElevationService.RESULT_UNKNOWN;
        }
    }
}

