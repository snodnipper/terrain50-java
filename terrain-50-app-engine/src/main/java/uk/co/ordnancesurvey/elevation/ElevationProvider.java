package uk.co.ordnancesurvey.elevation;

public interface ElevationProvider {

    String getElevation(String easting, String northing);
}
