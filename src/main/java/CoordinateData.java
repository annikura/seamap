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
}
