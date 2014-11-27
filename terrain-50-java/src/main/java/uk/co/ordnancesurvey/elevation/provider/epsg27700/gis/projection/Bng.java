package uk.co.ordnancesurvey.elevation.provider.epsg27700.gis.projection;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Bng {

    private static final double DEG = Math.PI / 180;
    // From http://www.ordnancesurvey.co.uk/oswebsite/gps/information/coordinatesystemsinfo/guidecontents/guidea.html
    private static final double AIRY1830_A = 6377563.396, AIRY1830_B = 6356256.910;
    private static final double WGS84_A = 6378137.000, WGS84_B = 6356752.3141;
    private static final double NATGRID_F0 = 0.9996012717, NATGRID_LAT0 = 49 * DEG, NATGRID_LNG0 = -2 * DEG, NATGRID_E0 = 400000, NATGRID_N0 = -100000;

    private static final double MAX_LON = 3.63202114501;
    private static final double MAX_LAT = 61.4645902176;
    private static final double MIN_LON = -7.55716018087;
    private static final double MIN_LAT = 49.766807227;

    private static final double MIN_EASTING = 0;
    private static final double MIN_NORTHING = 0;
    private static final double MAX_EASTING = 700000;
    private static final double MAX_NORTHING = 1300000;

    /**
     * Converts a WGS84 latitude/longitude to the corresponding GridPoint.
     * Accuracy depends on the projection used.
     *
     * @param latitude
     * @param longitude //@return newly created GridPoint
     * @return easting and northing
     */
    public static double[] toGridPoint(double latitude, double longitude) {
        double[] temp = new double[3];
        // Approximate average GPS altitude in the UK at a National Grid altitutde of 0.
        // It's somewhere between around 49 and 55, anyway.
        double AVERAGE_GPS_ALTITUDE = 53;
        getOSCoords(latitude, longitude, AVERAGE_GPS_ALTITUDE, temp);
        return new double[]{temp[0], temp[1]};
    }

    public static boolean validExtent(double easting, double northing) {
        return easting >= MIN_EASTING && easting <= MAX_EASTING
                && northing >= MIN_NORTHING && northing <=MAX_NORTHING;
    }

    public static boolean within(double lat, double lon) {
        return lat > MIN_LAT && lat < MAX_LAT && lon > MIN_LON && lon < MAX_LON;
    }

    /**
     * Converts latitude/longitude (WGS84) to easting and northing (National Grid) and height above geoid (m).
     * Results are highly approximate (±5m).
     *
     * @param lat             latitude (deg)
     * @param lng             longitude (deg)
     * @param alt             height above ellipsoid (m)
     * @param eastNorthHeight Must be at least 3 elements long. On return, first 3 elements are { easting, northing, height }. Other elements are untouched.
     */
    private static void getOSCoords(double lat, double lng, double alt, double[] eastNorthHeight) {
        // Save another malloc
        double[] temp = eastNorthHeight;//new double[3];
        latLngTo3D(WGS84_A, WGS84_B, lat, lng, alt, temp);

        double x = temp[0], y = temp[1], z = temp[2];
        // from http://www.ordnancesurvey.co.uk/oswebsite/gps/information/coordinatesystemsinfo/guidecontents/guide6.html
        // tx/m=-446.448 ty/m=+125.157 tz/m=-542.060 s/ppm=+20.4894 rx/sec=-0.1502 ry/sec=-0.2470 rz/sec=-0.8421
        double tx = -446.448;
        double ty = +125.157;
        double tz = -542.060;
        double s = +20.4894e-6;
        final double sec = Math.PI / 180 / 60 / 60;
        double rx = -0.1502 * sec;
        double ry = -0.2470 * sec;
        double rz = -0.8421 * sec;

        // [x']   [tx]   [1+s -rz  ry] [x]
        // [y'] = [ty] + [ rz 1+s -rx] [y]
        // [z']   [tz]   [-ry  rx 1+s] [z]

        double xp = tx + (1 + s) * x - rz * y + ry * z;
        double yp = ty + (1 + s) * y - rx * z + rz * x;
        double zp = tz + (1 + s) * z - ry * x + rx * y;

        // Reuse the same array!
        latLngFrom3D(AIRY1830_A, AIRY1830_B, xp, yp, zp, temp);

        // Reuse the same array again
        latLngToEastNorth(AIRY1830_A, AIRY1830_B, NATGRID_N0, NATGRID_E0, NATGRID_F0, NATGRID_LAT0, NATGRID_LNG0, temp[0], temp[1], temp);
    }

    /**
     * Converts latitude and longitude on an ellipsoid to 3D Cartesian coordinates. Angles are in degrees.
     * Results are highly approximate (±5m).
     *
     * @param a
     * @param b
     * @param lat
     * @param lng
     * @param h
     * @param xyzOut Must be at least 3 elements long. On return, first 3 elements are { x, y, z }. Other elements are untouched.
     */
    private static void latLngTo3D(double a, double b, double lat, double lng, double h, double[] xyzOut) {
        // From http://www.ordnancesurvey.co.uk/oswebsite/gps/docs/convertingcoordinates3D.pdf
        double asq = a * a;
        double bsq = b * b;
        double esq = (asq - bsq) / asq;

        lat = Math.toRadians(lat);
        lng = Math.toRadians(lng);
        double sinlat = Math.sin(lat);
        double coslat = Math.cos(lat);
        double sinlng = Math.sin(lng);
        double coslng = Math.cos(lng);
        double v = a / Math.sqrt(1 - esq * sinlat * sinlat);
        xyzOut[0] = (v + h) * coslat * coslng;
        xyzOut[1] = (v + h) * coslat * sinlng;
        xyzOut[2] = ((1 - esq) * v + h) * sinlat; // TODO numerical wotsits.
    }

    /**
     * Converts 3D Cartesian coordinates to latitude and longitude on an ellipsoid. Angles are in degrees.
     *
     * @param a               Ellipsoid major axis.
     * @param b               Ellipsoid minor axis.
     * @param x
     * @param y
     * @param z
     * @param latLngHeightOut Must be at least 3 elements long. On return, first 3 elements are { latitude, longitude, altitude }. Other elements are untouched.
     */
    private static void latLngFrom3D(double a, double b, double x, double y, double z, double[] latLngHeightOut) {
        // From http://www.ordnancesurvey.co.uk/oswebsite/gps/docs/convertingcoordinates3D.pdf
        double asq = a * a;
        double bsq = b * b;
        double esq = (asq - bsq) / asq;

        double lng = Math.atan2(y, x);
        double p = Math.sqrt(x * x + y * y);
        double lat = Math.atan(z / p * (1 - esq)); // TODO numerical wotsits.

        for (double oldDiff = Double.POSITIVE_INFINITY; /**/ ; /**/) {
            double sinlat = Math.sin(lat);
            double v = a / Math.sqrt(1 - esq * sinlat * sinlat);
            double newlat = Math.atan2(z + esq * v * sinlat, p);
            double diff = Math.abs(newlat - lat);
            lat = newlat;
            // Assume it converges, and that successive differences always get smaller.
            // When they stop getting smaller, we're precise enough.
            if (diff >= oldDiff) {
                break;
            }
            oldDiff = diff;
        }

        double sinlat = Math.sin(lat);
        double v = a / Math.sqrt(1 - esq * sinlat * sinlat);
        double h = p / Math.cos(lat) - v;

        latLngHeightOut[0] = Math.toDegrees(lat);
        latLngHeightOut[1] = Math.toDegrees(lng);
        latLngHeightOut[2] = h;
    }

    /**
     * TODO: BUGS: The northing is slightly inaccurate apparently due to rounding errors.
     *
     * @param a            Ellipsoid major axis.
     * @param b            Ellipsoid minor axis.
     * @param n0           Northing of true origin (m).
     * @param e0           Easting of true origin (m).
     * @param f0           Scale factor at central meridian.
     * @param lat0         Latitude of true origin (radians).
     * @param lng0         Longitude of true origin (radians).
     * @param lat          Latitude (deg).
     * @param lng          Longitude (deg).
     * @param eastNorthOut Must be at least two elements long. On return, first two elements are { easting, northing }. Other elements are untouched.
     */
    static void latLngToEastNorth(double a, double b, double n0, double e0, double f0, double lat0, double lng0, double lat, double lng, double[] eastNorthOut) {
        // From http://www.ordnancesurvey.co.uk/oswebsite/gps/docs/convertingcoordinatesEN.pdf
        double asq = a * a;
        double bsq = b * b;
        double esq = (asq - bsq) / asq;

        lat = Math.toRadians(lat);
        lng = Math.toRadians(lng);

        double sinlat = Math.sin(lat);
        double coslat = Math.cos(lat);
        double tanlat = sinlat / coslat;

        double n = (a - b) / (a + b);
        double v = a * f0 / Math.sqrt(1 - esq * sinlat * sinlat);
        double p = a * f0 * (1 - esq) * Math.pow(1 - esq * sinlat * sinlat, -1.5);
        double etasq = v / p - 1;

        double m = calculateM(b, f0, n, lat0, lat);
        double _I = m + n0;
        double _II = v / 2 * sinlat * coslat;
        double _III = v / 24 * sinlat * coslat * coslat * coslat * (5 - tanlat * tanlat + 9 * etasq);
        double _IIIA = v / 720 * sinlat * coslat * coslat * coslat * coslat * coslat * (61 - 58 * tanlat * tanlat + tanlat * tanlat * tanlat * tanlat);
        double _IV = v * coslat;
        double _V = v / 6 * coslat * coslat * coslat * (v / p - tanlat * tanlat);
        double _VI = v / 120 * coslat * coslat * coslat * coslat * coslat * (5 - 18 * tanlat * tanlat + tanlat * tanlat * tanlat * tanlat + 14 * etasq - 58 * tanlat * tanlat * etasq);
        eastNorthOut[1] = _I + _II * (lng - lng0) * (lng - lng0) + _III * (lng - lng0) * (lng - lng0) * (lng - lng0) * (lng - lng0) + _IIIA * (lng - lng0) * (lng - lng0) * (lng - lng0) * (lng - lng0) * (lng - lng0) * (lng - lng0);
        eastNorthOut[0] = e0 + _IV * (lng - lng0) + _V * (lng - lng0) * (lng - lng0) * (lng - lng0) + _VI * (lng - lng0) * (lng - lng0) * (lng - lng0) * (lng - lng0) * (lng - lng0);
    }

    // A largeish equation common to both calculations.
    private static double calculateM(double b, double f0, double n, double lat0, double lat) {
        double m = b * f0 * (
                (1 + n + 5.0 / 4 * n * n + 5.0 / 4 * n * n * n) * (lat - lat0)
                        - (3 * n + 3 * n * n + 21.0 / 8 * n * n * n) * sin(lat - lat0) * cos(lat + lat0)
                        + (15.0 / 8) * (n * n + n * n * n) * sin(2 * (lat - lat0)) * cos(2 * (lat + lat0))
                        - 35.0 / 24 * n * n * n * sin(3 * (lat - lat0)) * cos(3 * (lat + lat0))
        );
        return m;
    }
}
