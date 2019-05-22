import org.jetbrains.annotations.NotNull;

public class CoordinateData {
    public static final double EPS = 0.03;

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
        return new CoordinateData(
                Math.cos(degree * lat) - Math.sin(degree * lng),
                Math.sin(degree * lat) + Math.cos(degree * lng));
    }
}
