package hello;

import uk.co.ordnancesurvey.elevation.ElevationServiceImpl;

public class HelloTerrain50 {
    public static void main(String[] args) {
        System.out.println("Terrain 50");

        ElevationServiceImpl elevationService = new ElevationServiceImpl();
        String easting = "599595", northing = "114325";
        String elevation = elevationService.getAltitude(easting, northing);

        System.out.println("The elevation is: " + elevation);
    }
}
