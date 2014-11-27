package uk.co.ordnancesurvey.elevation;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ElevationServiceImpl implements ElevationService {

    private static final Logger LOGGER = Logger.getLogger(ElevationServiceImpl.class.getName());

    private final Map<Integer, ElevationProvider> mElevationProviders
            = new HashMap<Integer, ElevationProvider>();

    /**
     * Note: only one elevation provider per spatial reference is supported
     */
    public ElevationServiceImpl(ElevationProvider... elevationProviders) {
        for (ElevationProvider elevationProvider : elevationProviders) {
            mElevationProviders.put(elevationProvider.getSpatialRefererence(), elevationProvider);
        }
    }

    @Override
    public String getElevation(final String latitude, final String longitude) {
        return getElevation(4326, longitude, latitude);
    }

    @Override
    public String getElevation(double latitude, double longitude) {
        return getElevation(4326, longitude, latitude);
    }

    @Override
    public String getElevation(int srid, String x, String y) {
        try {
            double x_ = Double.parseDouble(x);
            double y_ = Double.parseDouble(y);
            return getElevation(srid, x_, y_);
        } catch (NumberFormatException numberFormatException) {
            LOGGER.log(Level.SEVERE, "invalid parameters", numberFormatException);
            return ElevationService.RESULT_ERROR;
        } catch (NullPointerException nullPointerException) {
            LOGGER.log(Level.SEVERE, "invalid parameters", nullPointerException);
            return ElevationService.RESULT_ERROR;
        }
    }

    @Override
    public String getElevation(int srid, double x, double y) {
        ElevationProvider elevationProvider = mElevationProviders.get(srid);

        boolean nativeProvider = elevationProvider != null;
        if (nativeProvider) {
            return elevationProvider.getElevation(x, y);
        }

        boolean globalProjection = srid == SRID_4326;
        if (globalProjection) {
            for (ElevationProvider provider : mElevationProviders.values()) {
                if (provider.containsLatLon(y, x)) {
                    return provider.getElevationFromLatLon(y, x);
                }
            }

        }
        return RESULT_UNKNOWN;
    }
}
