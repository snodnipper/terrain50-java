package hello;

import uk.co.ordnancesurvey.elevation.ElevationService;
import uk.co.ordnancesurvey.elevation.ElevationServiceProvider;
import uk.co.ordnancesurvey.gis.projection.Bng;

public class HelloTerrain50 {
    public static void main(String[] args) {
        System.out.println("Terrain 50");

        double[] max = Bng.toGridPoint(61.4645902176, 3.63202114501);
        double[] min = Bng.toGridPoint(49.766807227, -7.55716018087);

        String latitude = "55.5";
        String longitude = "-1.54";

        // ElevationService elevationService2 = new ElevationServiceImpl(configuration.getElevationProvider());
        ElevationService elevationService2 = ElevationServiceProvider.getInstance();

        String elevation = elevationService2.getElevation(latitude, longitude);
        System.out.println("Elevation: " + elevation);

        System.out.println("END");
        if (true) {
            return;
        }

        ElevationService elevationService = ElevationServiceProvider.getInstance();

        int bng = 27700;

        // Test SP04.asc
        String easting = "405214", northing = "240041";
        String expected = "46";
        String actual = elevationService.getElevation(bng, easting, northing);
        assertEquals(expected, actual);

        easting = "406944";
        northing = "249990";
        expected = "27.9";
        actual = elevationService.getElevation(bng, easting, northing);
        assertEquals(expected, actual);

        // Test HP61.asc
        easting = "464185";
        northing = "1212627";
        expected = "6.8";
        actual = elevationService.getElevation(bng, easting, northing);
        assertEquals(expected, actual);

        // Test NZ02.asc
        easting = "409148";
        northing = "562258";
        expected = "162.2";
        actual = elevationService.getElevation(bng, easting, northing);
        assertEquals(expected, actual);

        easting = "406316";
        northing = "569559";
        expected = "125.2";
        actual = elevationService.getElevation(bng, easting, northing);
        assertEquals(expected, actual);

        // Test TM02.asc
        easting = "603245";
        northing = "221741";
        expected = "-1.6";
        actual = elevationService.getElevation(bng, easting, northing);
        assertEquals(expected, actual);

        easting = "606686";
        northing = "225676";
        expected = "34.2";
        actual = elevationService.getElevation(bng, easting, northing);
        assertEquals(expected, actual);

        // double values
        double eastingD = 606686;
        double northingD = 225676;
        expected = "34.2";
        actual = elevationService.getElevation(bng, eastingD, northingD);
        assertEquals(expected, actual);

//        // TQ 9123 9678
//        String gridRef = "TQ 9123 9678";
//        actual = elevationService.getElevation(gridRef);
//        expected = "-1.7";
//        assertEquals(expected, actual);
    }

    private static void assertEquals(String expected, String actual) {
        if (expected.equals(actual)) {
            System.out.println("Success.  Elevation: " + expected);
        } else {
            System.out.println("Failure: " + expected + " != " + actual);
        }
    }
}
