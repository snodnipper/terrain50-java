package uk.co.ordnancesurvey.elevation.transformation;

public interface Transformer {

    /**
     * @return true if the input coordinates are within the WGS84 Bounds for this spatial reference
     */
    boolean validFor(double latitude, double longitude);


    String getSpatialReference();

    /**
     * @return the transformed coordinates where index zero is x and index one is y
     */
    double[] transform(double latitude, double longitude);
}
