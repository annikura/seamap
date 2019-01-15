import com.sothawo.mapjfx.Coordinate;
import com.sothawo.mapjfx.CoordinateLine;
import com.sothawo.mapjfx.MapView;
import com.sothawo.mapjfx.Marker;
import javafx.beans.property.BooleanProperty;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ShipMapElements {
    private final ArrayList<Marker> markers = new ArrayList<>();
    private final CoordinateLine path;


    public ShipMapElements(final @NotNull List<Coordinate> coordinates, String color) {
        coordinates.forEach(coordinate -> markers.add(
                Marker.createProvided(Marker.Provided.valueOf(color.toUpperCase())).setPosition(coordinate)));
        path = new CoordinateLine(coordinates).setColor(Color.valueOf(color)).setVisible(true);
    }

    public void showShipElements(final @NotNull MapView mapView) {
        markers.forEach(mapView::addMarker);
        mapView.addCoordinateLine(path);
    }

    public void deleteShipElements(final @NotNull MapView mapView) {
        markers.forEach(mapView::removeMarker);
        mapView.removeCoordinateLine(path);
    }

    public void setMarkersState(boolean visible) {
        markers.forEach(marker -> marker.setVisible(visible));
    }

    public void setPathState(boolean visible) {
        path.setVisible(visible);
    }

    public void bindMarkersWith(final @NotNull BooleanProperty bindingProperty) {
        markers.forEach(marker -> bindingProperty.bindBidirectional(marker.visibleProperty()));
    }

    public void bindPathWith(final @NotNull BooleanProperty bindingProperty) {
        bindingProperty.bindBidirectional(path.visibleProperty());
    }
}
