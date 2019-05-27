package ru.annikura.seamap.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MapData {
    public Double mapCenterLat = 0.0;
    public Double mapCenterLng = 0.0;
    public List<ShipData> ships = new ArrayList<>();

    @Nullable
    public ShipData getShip(final @NotNull String name) {
        for (ShipData shipData: ships) {
            if (shipData.shipName.equals(name)) {
                return shipData;
            }
        }
        return null;
    }

    public boolean remove(final @NotNull MarkerData marker) {
        ShipData ship = getShip(marker.ship);

        if (ship != null) {
            boolean result = ship.remove(marker);
            if (ship.markers.size() == 0) {
                ships.remove(ship);
            }
            return result;
        }
        return false;
    }

    public boolean add(final @NotNull MarkerData marker) {
        ShipData ship = getShip(marker.ship);
        if (ship != null) {
            return ship.add(marker);
        }
        return false;
    }

    // todo: replace colour with enum
    public ShipData addNewShip(final @NotNull MarkerData markerData, final @NotNull String colour) {
        ShipData newShip = new ShipData();
        newShip.shipName = markerData.ship;
        newShip.markers = new ArrayList<>();
        newShip.markers.add(markerData);
        newShip.color = colour;
        ships.add(newShip);
        return newShip;
    }
}
