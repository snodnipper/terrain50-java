package uk.co.ordnancesurvey.elevation.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.co.ordnancesurvey.elevation.ElevationService;
import uk.co.ordnancesurvey.elevation.SpatialReference;
import uk.co.ordnancesurvey.elevation.provider.ElevationProvider;
import uk.co.ordnancesurvey.elevation.transformation.Transformer;

public class ElevationServiceImpl implements ElevationService {

    private static final Logger LOGGER = Logger.getLogger(ElevationServiceImpl.class.getName());

    private static final String DIFFERENT_SIZED_ARRAYS =
            "latitude and longitude arrays differ in size!";

    private final Map<String, Transformer> mTransformers = new HashMap<String, Transformer>();
    private final Map<String, ElevationProvider> mElevationProviders
            = new HashMap<String, ElevationProvider>();

    /**
     * Note: only one elevation provider per spatial reference is supported
     */
    public ElevationServiceImpl(List<Transformer> transformers, ElevationProvider... elevationProviders) {
        for (ElevationProvider elevationProvider : elevationProviders) {
            mElevationProviders.put(elevationProvider.getSpatialRefererence(), elevationProvider);
        }
        for (Transformer transformer : transformers) {
            mTransformers.put(transformer.getSpatialReference(), transformer);
        }
    }

    @Override
    public String getElevation(final String latitude, final String longitude) {
        return getElevation(SpatialReference.EPSG_4326, longitude, latitude);
    }

    @Override
    public String getElevation(double latitude, double longitude) {
        return getElevation(SpatialReference.EPSG_4326, longitude, latitude);
    }

    @Override
    public String getElevation(String srid, String x, String y) {
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
    public String getElevation(String srid, double x, double y) {
        ElevationProvider elevationProvider = mElevationProviders.get(srid);

        boolean nativeProvider = elevationProvider != null;
        if (nativeProvider) {
            return elevationProvider.getElevation(x, y);
        }

        boolean globalProjection = srid.equals(SpatialReference.EPSG_4326);
        if (globalProjection) {
            double latitude = y;
            double longitude = x;

            for (ElevationProvider provider : mElevationProviders.values()) {
                Transformer transformer = mTransformers.get(provider.getSpatialRefererence());

                boolean hasTransformer = transformer != null;
                if (hasTransformer && transformer.validFor(latitude, longitude)) {
                    double[] xy = transformer.transform(latitude, longitude);
                    return provider.getElevation(xy[0], xy[1]);
                }
            }

        }
        return RESULT_UNKNOWN;
    }

    @Override
    public String[] getElevationValues(String[] latitude, String[] longitude) {
        if (latitude.length != longitude.length) {
            throw new IllegalArgumentException(DIFFERENT_SIZED_ARRAYS);
        }

        String[] result = new String[latitude.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = getElevation(latitude[i], longitude[i]);
        }
        return result;
    }


    @Override
    public String[] getElevationValues(double[] latitude, double[] longitude) {
        if (latitude.length != longitude.length) {
            throw new IllegalArgumentException(DIFFERENT_SIZED_ARRAYS);
        }

        String[] result = new String[latitude.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = getElevation(latitude[i], longitude[i]);
        }
        return result;
    }
}
