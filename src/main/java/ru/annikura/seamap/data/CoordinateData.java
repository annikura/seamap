package ru.annikura.seamap.data;

import org.jetbrains.annotations.NotNull;
import ru.annikura.seamap.utils.WebMercator;

public class CoordinateData {
    public static final double EPS = 0.00003;

    public CoordinateData(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    private double lat;
    private double lng;

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public double distanceTo(final @NotNull CoordinateData other) {
        return Math.sqrt((lat - other.lat) * (lat - other.lat) + (lng - other.lng) * (lng - other.lng));
    }

    public CoordinateData plus(final @NotNull CoordinateData other) {
        return new CoordinateData(lat + other.getLat(), lng + other.getLng());
    }

    public CoordinateData minus(final @NotNull CoordinateData other) {
        return new CoordinateData(lat - other.getLat(), lng - other.getLng());
    }

    public CoordinateData mul(double x) {
        return new CoordinateData(lat * x, lng * x);
    }

    public CoordinateData turn(double degree) {
        degree = degree / 180.0 * Math.PI;
        double ang = Math.atan2(lat, lng);
        double r = distanceTo(new CoordinateData(0, 0));
        return new CoordinateData(r * Math.sin(degree + ang), r * Math.cos(degree + ang));
    }

    public double length() {
        return Math.sqrt(lat * lat + lng * lng);
    }

    public CoordinateData toLength(double newLength) {
        double length = length();
        if (length < EPS) {
            return new CoordinateData(0, 0);
        }
        return new CoordinateData(lat / length * newLength, lng / length * newLength);
    }

    public CoordinateData normalize() {
        return toLength(1);
    }

    // Mecrator web projection

    public CoordinateData toXY() {
        return new CoordinateData(WebMercator.latitudeToY(lat), WebMercator.longitudeToX(lng));
    }

    public CoordinateData shiftByPixels(double x, double y) {
        return new CoordinateData(
                WebMercator.yToLatitude(WebMercator.latitudeToY(lat) + x),
                WebMercator.xToLongitude(WebMercator.longitudeToX(lng) + y));
    }

    public CoordinateData shiftByPixels(final @NotNull CoordinateData other) {
        return shiftByPixels(other.lat, other.lng);
    }
}
