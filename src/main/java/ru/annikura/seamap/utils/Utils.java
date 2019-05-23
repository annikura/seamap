package ru.annikura.seamap.utils;

import com.sothawo.mapjfx.Coordinate;
import org.jetbrains.annotations.NotNull;
import ru.annikura.seamap.data.CoordinateData;

public class Utils {
    public static Coordinate coordinateDataToCoordinate(final @NotNull CoordinateData cordinate) {
        return new Coordinate(cordinate.getLat(), cordinate.getLng());
    }

    public static CoordinateData coordinateToCoordinateData(final @NotNull Coordinate cordinate) {
        return new CoordinateData(cordinate.getLatitude(), cordinate.getLongitude());
    }


}
