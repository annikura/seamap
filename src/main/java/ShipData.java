import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ShipData {
    String color;
    String shipName;
    List<MarkerData> markers;

    @Nullable
    public MarkerData approximateInnerPathCoordinate(final @NotNull CoordinateData coordinate) {
        int closestSegment = findClosestSegment(coordinate);
        if (closestSegment + 1 >= markers.size()) {
            return null;
        }

        MarkerData newMarker = new MarkerData();
        newMarker.ship = shipName;
        newMarker.coordinate = coordinate;

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        long date1;
        long date2;
        try {
            date1 = dateFormat.parse(markers.get(closestSegment).date).getTime();
            date2 = dateFormat.parse(markers.get(closestSegment + 1).date).getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e.getCause());
        }

        double distance1 = coordinate.distanceTo(markers.get(closestSegment).coordinate);
        double distance2 = coordinate.distanceTo(markers.get(closestSegment + 1).coordinate);

        newMarker.date = dateFormat.format(new Date((long) ((date2 - date1) * distance1 / (distance1 + distance2) + date1)));
        return newMarker;
    }

    @NotNull
    public Double distanceToShip(final @NotNull CoordinateData coordinate) {
        return distanceToSegment(findClosestSegment(coordinate), coordinate);
    }

    private int findClosestSegment(final @NotNull CoordinateData coordinate) {
        Double distance = null;
        int bestSegment = -1;
        for (int i = 0; i < markers.size(); i++) {
            double newDistance = distanceToSegment(i, coordinate);
            if (distance == null || newDistance < distance) {
                distance = newDistance;
                bestSegment = i;
            }
        }
        return bestSegment;
    }

    @NotNull
    private Double distanceToSegment(int segment, final @NotNull CoordinateData coordinate) {
        if (segment + 1 < markers.size()
                && markers.get(segment).coordinate.distanceTo(markers.get(segment + 1).coordinate) > CoordinateData.EPS) {
            CoordinateData point1 = markers.get(segment).coordinate;
            CoordinateData point2 = markers.get(segment + 1).coordinate;

            double segmentLength = point1.distanceTo(point2);
            double distanceToLine = Math.abs(
                    (point2.getLat()- point1.getLat()) * coordinate.getLng()
                            - (point2.getLng() - point1.getLng()) * coordinate.getLat()
                            + point2.getLng() * point1.getLat() - point1.getLng() * point2.getLat()) / segmentLength;

            double distanceTo1 = coordinate.distanceTo(point1);
            double distanceTo2 = coordinate.distanceTo(point2);

            if (Math.sqrt(distanceTo1 * distanceTo1 - distanceToLine * distanceToLine) < segmentLength &&
                    Math.sqrt(distanceTo2 * distanceTo2 - distanceToLine * distanceToLine) < segmentLength) {
                return distanceToLine;
            } else {
                return Math.min(distanceTo1, distanceTo2);
            }
        } else {
            return markers.get(segment).coordinate.distanceTo(coordinate);
        }
    }
}
