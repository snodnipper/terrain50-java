package uk.co.ordnancesurvey.elevation.impl;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import uk.co.ordnancesurvey.elevation.Configuration;
import uk.co.ordnancesurvey.elevation.ElevationService;
import uk.co.ordnancesurvey.elevation.SpatialReference;
import uk.co.ordnancesurvey.elevation.Strategy;
import uk.co.ordnancesurvey.elevation.transformation.Transformer;
import uk.co.ordnancesurvey.elevation.transformation.epsg27700.TransformerProj4js27700;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ElevationServiceImplTest {

    @BeforeClass
    public static void cleanup() {
        Util.clearDownloadDirectory();
    }

    @Test
    public void testElevationService() {
        ElevationService elevationService = getElevationService();
        String easting = "402945";
        String northing = "249990";
        String expected = "101.8";
        String actual = elevationService.getElevation(SpatialReference.EPSG_27700, easting, northing);
        assertEquals(expected, actual);
    }

    @Test
    public void testElevationServiceWithProj4jsConfig() {
        ElevationService elevationService = getElevationService(new TransformerProj4js27700());

        String latitude = "51.50722";
        String longitude = "-0.12750";
        String expected = "7.3";
        String actual = elevationService.getElevation(latitude, longitude);
        assertEquals(expected, actual);
    }

    @Test
    public void testLoad() {
        AtomicInteger counter = new AtomicInteger(0);
        ElevationService elevationService = getElevationService();
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
        ElevationService elevationService = getElevationService();

        // London
        String easting = "530050";
        String northing = "180361";
        String expected = "7.3";
        String actual = elevationService.getElevation(SpatialReference.EPSG_27700, easting,
                northing);
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

    @Test
    public void testSpotHeightsArray() {
        String[] latitudes = new String[]{"57.4405369733", "60.6921160183", "50.7640788772"};
        String[] longitudes = new String[]{"-1.81790913252", "-1.1066942536", "0.122657620389"};


        ElevationService elevationService = getElevationService();
        String[] expected = new String[]{"41.8", "72.6", "43.5"};
        String[] actual = elevationService.getElevationValues(latitudes, longitudes);
        assertArrayEquals(expected, actual);
    }

// # Crude Performance Test #
//    @Test
//    public void testCaches() {
//        double easting = 402945;
//        double northing = 249990;
//        ElevationService elevationService = getElevationService();
//
//        long then = System.currentTimeMillis();
//        for (int i = 0; i < 50000; i++) {
//            easting++;
//            northing++;
//            String result = elevationService.getElevation(SpatialReference.EPSG_27700, easting, northing);
//        }
//        long now = System.currentTimeMillis();
//        long totalTime = (now - then) / 1000;
//        System.out.println("Total time: " + totalTime + " seconds");
//    }

    private void checkLocation(String latitude, String longitude, String elevation) {
        ElevationService elevationService = getElevationService();
        String expected = elevation;
        String actual = elevationService.getElevation(latitude, longitude);
        assertEquals(expected, actual);
    }

    private void checkLocationInBng(String easting, String northing, String elevation) {
        ElevationService elevationService = getElevationService();
        String expected = elevation;
        String actual = elevationService.getElevation(SpatialReference.EPSG_27700, easting, northing);
        assertEquals(expected, actual);
    }

    private ElevationService getElevationService() {
        return getElevationService(null);
    }

    // Note: cannot use singleton provider to test!
    private ElevationService getElevationService(Transformer transformer) {
        Configuration.Builder builder = new Configuration.Builder()
                .setStrategy(Strategy.CONSERVE_RESOURCE)
                .terrain50DataUrl("https://github.com/snodnipper/terrain50-java/raw/master/data/");
        if (transformer != null) {
            builder.addTransformer(transformer);
        }

        Configuration configuration = builder.build();
        return new ElevationServiceImpl(configuration.getTransformers(),
                configuration.getElevationProvider());
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

