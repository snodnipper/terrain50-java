package uk.co.ordnancesurvey.gis;

public class Point {

    private final double mX;
    private final double mY;

    public Point(double x, double y) {
        mX = x;
        mY = y;
    }

    public double getX() {
        return mX;
    }

    public double getY() {
        return mY;
    }
}
