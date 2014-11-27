package uk.co.ordnancesurvey.elevation.impl;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import uk.co.ordnancesurvey.elevation.ElevationService;
import uk.co.ordnancesurvey.elevation.ElevationServiceProvider;

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
        String actual = elevationService.getElevation(27700, easting, northing);
        assertEquals(expected, actual);
    }

    @Test
    public void testLondonElevation() {
        ElevationService elevationService = ElevationServiceProvider.getInstance();

        // London
        String easting = "530050";
        String northing = "180361";
        String expected = "7.3";
        String actual = elevationService.getElevation(27700, easting, northing);
        assertEquals(expected, actual);

        String latitude = "51.50722";
        String longitude = "-0.12750";
        String expected2 = "7.3";
        String actual2 = elevationService.getElevation(latitude, longitude);
        assertEquals(expected2, actual2);
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
                String actual = elevationService.getElevation(27700, easting, northing);
                assertEquals(expected, actual);
                atomicInteger.incrementAndGet();
            }
        }).start();
    }
}

