package hello;

import uk.co.ordnancesurvey.elevation.ElevationService;
import uk.co.ordnancesurvey.elevation.appengine.ElevationServiceImpl;

public class HelloTerrain50 {
    public static void main(String[] args) {
        System.out.println("Terrain 50");

//        ElevationService elevationService = new ElevationServiceAndroidImpl();
        ElevationService elevationService = new ElevationServiceImpl();

        // Test SP04.asc
        String easting = "405214", northing = "240041";
        String expected = "46";
        String actual = elevationService.getElevation(easting, northing);
        assertEquals(expected, actual);

        easting = "406944";
        northing = "249990";
        expected = "27.9";
        actual = elevationService.getElevation(easting, northing);
        assertEquals(expected, actual);

        // Test HP61.asc
        easting = "464185";
        northing = "1212627";
        expected = "6.8";
        actual = elevationService.getElevation(easting, northing);
        assertEquals(expected, actual);

        // Test NZ02.asc
        easting = "409148";
        northing = "562258";
        expected = "162.2";
        actual = elevationService.getElevation(easting, northing);
        assertEquals(expected, actual);

        easting = "406316";
        northing = "569559";
        expected = "125.2";
        actual = elevationService.getElevation(easting, northing);
        assertEquals(expected, actual);

        // Test TM02.asc
        easting = "603245";
        northing = "221741";
        expected = "-1.6";
        actual = elevationService.getElevation(easting, northing);
        assertEquals(expected, actual);

        easting = "606686";
        northing = "225676";
        expected = "34.2";
        actual = elevationService.getElevation(easting, northing);
        assertEquals(expected, actual);

        // double values
        double eastingD = 606686;
        double northingD = 225676;
        expected = "34.2";
        actual = elevationService.getElevation(eastingD, northingD);
        assertEquals(expected, actual);

        // TQ 9123 9678
        String gridRef = "TQ 9123 9678";
        actual = elevationService.getElevation(gridRef);
        expected = "-1.7";
        assertEquals(expected, actual);
    }

    private static void assertEquals(String expected, String actual) {
        if (expected.equals(actual)) {
            System.out.println("Success.  Elevation: " + expected);
        } else {
            System.out.println("Failure: " + expected + " != " + actual);
        }
    }
}
