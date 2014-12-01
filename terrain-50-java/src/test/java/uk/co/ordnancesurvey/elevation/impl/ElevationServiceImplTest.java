package uk.co.ordnancesurvey.elevation.impl;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import uk.co.ordnancesurvey.elevation.Configuration;
import uk.co.ordnancesurvey.elevation.ElevationService;
import uk.co.ordnancesurvey.elevation.ElevationServiceProvider;
import uk.co.ordnancesurvey.elevation.SpatialReference;
import uk.co.ordnancesurvey.elevation.transformation.epsg27700.TransformerProj4js27700;

import static org.junit.Assert.assertEquals;

public class ElevationServiceImplTest {

    @BeforeClass
    public static void cleanup() {
        Util.clearDownloadDirectory();
    }

    @Test
    public void testElevationService() {
        ElevationService elevationService = ElevationServiceProvider.getInstance();
        String easting = "402945";
        String northing = "249990";
        String expected = "101.8";
        String actual = elevationService.getElevation(SpatialReference.EPSG_27700, easting, northing);
        assertEquals(expected, actual);
    }

    @Test
    public void testElevationServiceWithProj4jsConfig() {
        Configuration configuration = new Configuration.Builder().addTransformer(new TransformerProj4js27700()).build();
        ElevationService elevationService = ElevationServiceProvider.getInstance(configuration);

        String latitude = "51.50722";
        String longitude = "-0.12750";
        String expected = "7.3";
        String actual = elevationService.getElevation(latitude, longitude);
        assertEquals(expected, actual);
    }

    @Test
    public void testLoad() {
        AtomicInteger counter = new AtomicInteger(0);
        ElevationService elevationService = ElevationServiceProvider.getInstance();
        run(counter, elevationService);
        run(counter, elevationService);
        run(counter, elevationService);

        long timeout = 10000;
        long start = System.currentTimeMillis();

        while (counter.get() != 3 && isValid(start, timeout)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        int expected = 3;
        int actual = counter.get();
        assertEquals(expected, actual);
    }

    @Test
    public void testLondonElevation() {
        ElevationService elevationService = ElevationServiceProvider.getInstance();

        // London
        String easting = "530050";
        String northing = "180361";
        String expected = "7.3";
        String actual = elevationService.getElevation(SpatialReference.EPSG_27700, easting, northing);
        assertEquals(expected, actual);

        String latitude = "51.50722";
        String longitude = "-0.12750";
        String expected2 = "7.3";
        String actual2 = elevationService.getElevation(latitude, longitude);
        assertEquals(expected2, actual2);
    }

    @Test
    public void testSpotHeights() {
        checkLocation("57.4405369733", "-1.81790913252", "41.8");
        checkLocation("60.6921160183", "-1.1066942536", "72.6");
        checkLocation("50.7640788772", "0.122657620389", "43.5");
        checkLocation("51.4271375368", "1.51903787131", "-1.5");
        checkLocation("50.8526802104", "-0.482403140607", "56.6");
        checkLocation("51.6256844165", "-5.05800933452", "37.8");
        checkLocation("51.7338801877", "-3.52531535356", "261.3");
        checkLocation("52.7236291443", "-3.39806719052", "261.5");
        checkLocation("54.4219408227", "-0.404460175857", "-1.6");
        checkLocation("56.1570700993", "-3.53028364237", "127");
    }

    @Test
    public void testSpotHeightsInBng() {
        checkLocationInBng("411030", "838927", "41.8");
        checkLocationInBng("448883", "1201323", "72.6");
        checkLocationInBng("549794", "98202", "43.5");
        checkLocationInBng("644725", "175668", "-1.5");
        checkLocationInBng("506923", "107004", "56.6");
        checkLocationInBng("188433", "196305", "37.8");
        checkLocationInBng("294763", "205009", "261.3");
        checkLocationInBng("305674", "314915", "261.5");
        checkLocationInBng("503623", "504117", "-1.6");
        checkLocationInBng("305047", "697098", "127");
    }

    private void checkLocation(String latitude, String longitude, String elevation) {
        ElevationService elevationService = ElevationServiceProvider.getInstance();
        String expected = elevation;
        String actual = elevationService.getElevation(latitude, longitude);
        assertEquals(expected, actual);
    }

    private void checkLocationInBng(String easting, String northing, String elevation) {
        ElevationService elevationService = ElevationServiceProvider.getInstance();
        String expected = elevation;
        String actual = elevationService.getElevation(SpatialReference.EPSG_27700, easting, northing);
        assertEquals(expected, actual);
    }

    private boolean isValid(long start, long timeout) {
        return System.currentTimeMillis() - start < timeout;
    }

    private void run(final AtomicInteger atomicInteger, final ElevationService elevationService) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                String easting = "402945";
                String northing = "249990";
                String expected = "101.8";
                String actual = elevationService.getElevation(SpatialReference.EPSG_27700, easting, northing);
                assertEquals(expected, actual);
                atomicInteger.incrementAndGet();
            }
        }).start();
    }
}

