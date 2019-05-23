package ru.annikura.seamap.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ShipData {
    public String color;
    public String shipName;
    public List<MarkerData> markers;

    public @Nullable MarkerData projectCoordinateOnPath(final @NotNull CoordinateData coordinate) {
        int closestSegment = findClosestSegment(coordinate);
        if (closestSegment + 1 >= markers.size() ||
                !pointIsInner(
                        markers.get(closestSegment).coordinate,
                        markers.get(closestSegment + 1).coordinate,
                        coordinate)) {
            return null;
        }
        CoordinateData point1 = markers.get(closestSegment).coordinate;
        CoordinateData point2 = markers.get(closestSegment + 1).coordinate;

        double distanceToPoint = coordinate.distanceTo(point1);
        double segmentLength = point1.distanceTo(point2);
        double minDistance = distanceToSegment(closestSegment, coordinate);

        double part = Math.sqrt(distanceToPoint * distanceToPoint - minDistance * minDistance) / segmentLength;
        CoordinateData projection = point2.minus(point1).mul(part).plus(point1);
        return approximateCoordinateToPath(projection);
    }


    @Nullable
    public MarkerData approximateCoordinateToPath(final @NotNull CoordinateData coordinate) {
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

            if (pointIsInner(point1, point2, coordinate)) {
                return distanceToLine(point1, point2, coordinate);
            } else {
                return Math.min(coordinate.distanceTo(point1), coordinate.distanceTo(point2));
            }
        } else {
            return markers.get(segment).coordinate.distanceTo(coordinate);
        }
    }

    private static boolean pointIsInner(final @NotNull CoordinateData point1,
                                 final @NotNull CoordinateData point2,
                                 final @NotNull CoordinateData coordinate) {
        double segmentLength = point1.distanceTo(point2);
        double distanceToLine = distanceToLine(point1, point2, coordinate);
        double distanceTo1 = coordinate.distanceTo(point1);
        double distanceTo2 = coordinate.distanceTo(point2);
        return (Math.sqrt(distanceTo1 * distanceTo1 - distanceToLine * distanceToLine) < segmentLength &&
                Math.sqrt(distanceTo2 * distanceTo2 - distanceToLine * distanceToLine) < segmentLength);
    }

    private static double distanceToLine(final @NotNull CoordinateData point1,
                                         final @NotNull CoordinateData point2,
                                         final @NotNull CoordinateData coordinate) {
        return Math.abs(
                (point2.getLat()- point1.getLat()) * coordinate.getLng()
                        - (point2.getLng() - point1.getLng()) * coordinate.getLat()
                        + point2.getLng() * point1.getLat() - point1.getLng() * point2.getLat()) / point1.distanceTo(point2);

    }
}
