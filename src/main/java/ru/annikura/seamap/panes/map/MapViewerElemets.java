package ru.annikura.seamap.panes.map;

import com.sothawo.mapjfx.*;
import com.sothawo.mapjfx.event.MapViewEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.annikura.seamap.data.CoordinateData;
import ru.annikura.seamap.data.MapData;
import ru.annikura.seamap.data.MarkerData;
import ru.annikura.seamap.data.ShipData;
import ru.annikura.seamap.utils.Holder;
import ru.annikura.seamap.utils.Utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class MapViewerElemets {
    private MapView mapView = new MapView();

    private ArrayList<MapViewerElemets.ShipMapElements> shipMapElements = new ArrayList<>();
    private HashMap<String, MarkerData> markerMapping = new HashMap<>();
    private MapData displayedData = new MapData();

    private VBox mapSettingsPaneBox = new VBox();
    private RadioButton showAll = new RadioButton("Show everything");
    private RadioButton showOnlyMarkers = new RadioButton("Show markers only");
    private RadioButton showOnlyPaths = new RadioButton("Show paths only");
    private RadioButton customMode = new RadioButton("Custom mode");
    private CheckBox showSquaresCheckBox = new CheckBox("Show relevant marinequadrates");
    private CheckBox showLabelsCheckBox = new CheckBox("Show relevant time marks");
    private CheckBox showWindDirectionsCheckBox = new CheckBox("Show relevant wind directions");

    private Holder<MapLabel> tipLabelHolder = new Holder<>();

    private TreeView<String> visibilityControlsTree = new TreeView<>();
    private Map<String, CheckBoxTreeItem<String>> shipVisibilityControls = new HashMap<>();

    public MapViewerElemets(final @Nullable String cacheDirectory) {
        // Setup mapView

        mapView.setCustomMapviewCssURL(getClass().getResource("/custom_mapview.css"));

        mapView.initializedProperty().addListener((observableValue, aBoolean, t1) -> {
            mapView.setCenter(new Coordinate(0.0, 0.0));
            mapView.setMapType(MapType.OSM);

            mapView.setZoom(5);
            mapView.addEventHandler(MouseEvent.MOUSE_EXITED, mouseEvent -> {
                if (tipLabelHolder.getValue() != null) {
                    tipLabelHolder.getValue().setVisible(false);
                }
            });

            mapView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                if (event.getButton().equals(MouseButton.SECONDARY)) {
                    event.consume();
                }
            });

            if (cacheDirectory != null) {
                try {
                    Files.createDirectories(Paths.get(cacheDirectory));
                    mapView.getOfflineCache().setCacheDirectory(cacheDirectory);
                    mapView.getOfflineCache().setActive(true);
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Cashing error");
                    alert.setHeaderText("Unable to write to directory '" + cacheDirectory + "'");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            }
        });

        mapView.initialize();

        mapView.addEventHandler(MapViewEvent.MAP_POINTER_MOVED, mapViewEvent -> {
            if (tipLabelHolder.getValue() != null) {
                mapView.removeLabel(tipLabelHolder.getValue());
            }
            CoordinateData newCoordinate = Utils.coordinateToCoordinateData(mapViewEvent.getCoordinate());
            ShipData nearestShip = null;
            Double bestDistance = null;

            for (ShipData ship : displayedData.ships) {
                if (shipVisibilityControls.get(ship.shipName).isSelected() || shipVisibilityControls.get(ship.shipName).isIndeterminate()) {
                    double newDistance = ship.distanceToShip(newCoordinate);
                    if (newDistance < CoordinateData.EPS * Math.pow(2.0, 18 - mapView.getZoom())
                            && (bestDistance == null || newDistance < bestDistance)) {
                        nearestShip = ship;
                        bestDistance = newDistance;
                    }
                }
            }

            if (nearestShip == null) {
                return;
            }
            MarkerData coordinateApproximation = nearestShip.projectCoordinateOnPath(newCoordinate);
            if (coordinateApproximation == null) {
                return;
            }

            // Todo: create data parsing
            MapLabel newMapLabel = new MapLabel(coordinateApproximation.date.substring(11), 12, 12).setCssClass("map-label");
            tipLabelHolder.setValue(newMapLabel);
            newMapLabel.setPosition(Utils.coordinateDataToCoordinate(newCoordinate));
            mapView.addLabel(newMapLabel);
            newMapLabel.setVisible(true);
        });

        mapView.addEventHandler(MapViewEvent.MAP_EXTENT, event -> {
            mapView.setExtent(event.getExtent());
        });


        // Setup left panel controls

        ToggleGroup visibilityToggle = new ToggleGroup();
        showAll.setToggleGroup(visibilityToggle);
        showOnlyMarkers.setToggleGroup(visibilityToggle);
        showOnlyPaths.setToggleGroup(visibilityToggle);
        customMode.setToggleGroup(visibilityToggle);

        visibilityToggle.selectedToggleProperty().addListener((observableValue, oldToggle, newToggle) -> {
            visibilityControlsTree.setDisable(!newToggle.equals(customMode));
            if (newToggle.equals(customMode)) {
                return;
            }
            shipMapElements.forEach(shipElements -> {
                shipElements.setMarkersState(false);
                shipElements.setPathState(false);
            });
            if (newToggle.equals(showAll) || newToggle.equals(showOnlyMarkers)) {
                shipMapElements.forEach(shipElements -> shipElements.setMarkersState(true));
            }
            if (newToggle.equals(showAll) || newToggle.equals(showOnlyPaths)) {
                shipMapElements.forEach(shipElements -> shipElements.setPathState(true));
            }
        });

        CheckBoxTreeItem<String> visibilityControlsRootItem = new CheckBoxTreeItem<>("Content visibility");

        visibilityControlsRootItem.setExpanded(true);
        visibilityControlsTree.setRoot(visibilityControlsRootItem);
        visibilityControlsTree.setShowRoot(true);
        visibilityControlsTree.setCellFactory(CheckBoxTreeCell.forTreeView());

        showAll.setSelected(true);

        mapSettingsPaneBox.setSpacing(5.0);
        mapSettingsPaneBox.getChildren().addAll(
                showAll, showOnlyMarkers, showOnlyPaths, customMode,
                visibilityControlsTree,
                showSquaresCheckBox,
                showLabelsCheckBox,
                showWindDirectionsCheckBox);
    }

    public MarkerData getMarkerData(final @NotNull String id) {
        return markerMapping.get(id);
    }

    public Node getMapViewControls() {
        return mapSettingsPaneBox;
    }

    public MapView getMapView() {
        return mapView;
    }

    public void clearOldData() {
        visibilityControlsTree.getRoot().getChildren().clear();
        shipVisibilityControls.clear();
        for (MapViewerElemets.ShipMapElements shipMapElement : shipMapElements) {
            shipMapElement.deleteShipElements(mapView);
        }
        shipMapElements.clear();

        showAll.setSelected(true);
    }


    public void loadMapData(final @NotNull MapData mapData) {
        mapView.setCenter(new Coordinate(mapData.mapCenterLat, mapData.mapCenterLng));
        displayedData = mapData;
        for (ShipData ship : mapData.ships) {
            final ShipMapElements shipElements = new ShipMapElements(ship.markers, ship.color);
            final CheckBoxTreeItem<String> shipCheckBoxItem = new CheckBoxTreeItem<>(ship.shipName + " (" + ship.color + ")");
            shipVisibilityControls.put(ship.shipName, shipCheckBoxItem);
            final CheckBoxTreeItem<String> dataPointsCheckBoxItem = new CheckBoxTreeItem<>("Data points visible");
            final CheckBoxTreeItem<String> shipRoutesCheckBoxItem = new CheckBoxTreeItem<>("Path visible");
            shipCheckBoxItem.getChildren().add(dataPointsCheckBoxItem);
            shipCheckBoxItem.getChildren().add(shipRoutesCheckBoxItem);

            dataPointsCheckBoxItem.setSelected(!showOnlyPaths.isSelected());
            shipRoutesCheckBoxItem.setSelected(!showOnlyMarkers.isSelected());
            shipElements.setMarkersState(!showOnlyPaths.isSelected());
            shipElements.setPathState(!showOnlyMarkers.isSelected());
            shipElements.setup(dataPointsCheckBoxItem.selectedProperty(), shipRoutesCheckBoxItem.selectedProperty());

            visibilityControlsTree.getRoot().getChildren().add(shipCheckBoxItem);
            shipElements.showShipElements(mapView);
            shipMapElements.add(shipElements);
        }
    }

    private class ShipMapElements {
        private final ArrayList<Marker> markers = new ArrayList<>();
        private final ArrayList<CoordinateLine> windDirections = new ArrayList<>();
        private final ArrayList<MapLabel> labels = new ArrayList<>();
        private final CoordinateLine path;
        private final HashMap<String, CoordinateLine> marinequadrates = new HashMap<>();

        private List<Coordinate> getArrow(
                final @NotNull CoordinateData direction,
                final @NotNull CoordinateData point) {
            double arrowSize = 10000;
            CoordinateData arrowDirection = direction.mul(-1).toLength(arrowSize);
            List<Coordinate> arrow = new ArrayList<>();
            arrow.add(Utils.coordinateDataToCoordinate(point.shiftByPixels(arrowDirection.turn(30))));
            arrow.add(Utils.coordinateDataToCoordinate(point));
            arrow.add(Utils.coordinateDataToCoordinate(point.shiftByPixels(arrowDirection.turn(-30))));
            return arrow;
        }

        private ShipMapElements(final @NotNull List<MarkerData> markerData, @NotNull String color) {
            ArrayList<Coordinate> coordinates = new ArrayList<>();
            CoordinateData previousMarker = null;
            for (MarkerData marker : markerData) {
                Coordinate newCoordinate = Utils.coordinateDataToCoordinate(marker.coordinate);
                Marker newMarker = new Marker(getClass().getResource("/" + color + ".png"), -12, -12)
                        .setPosition(newCoordinate);
                if (previousMarker != null) {
                    CoordinateData arrowDirection = marker.coordinate.toXY().minus(previousMarker.toXY());
                    CoordinateData arrowPlace = marker.coordinate.plus(previousMarker).mul(0.5);
                    coordinates.add(Utils.coordinateDataToCoordinate(arrowPlace));
                    coordinates.addAll(getArrow(arrowDirection, arrowPlace));
                    coordinates.add(Utils.coordinateDataToCoordinate(arrowPlace));
                }
                coordinates.add(newCoordinate);

                // TODO: add single date property
                MapLabel newMarkerLabel = new MapLabel(marker.date.substring(marker.date.length() - 5), 12, 12)
                        .setPosition(newCoordinate)
                        .setCssClass("map-label");
                labels.add(newMarkerLabel);

                if (marker.weatherData != null) {
                    CoordinateData windDirection = marker.weatherData.getWindDirectionVector();
                    if (windDirection != null) {
                        List<Coordinate> windCoordinates = new ArrayList<>();
                        windCoordinates.add(Utils.coordinateDataToCoordinate(marker.coordinate));
                        CoordinateData arrowEnd = marker.coordinate.shiftByPixels(windDirection.mul(50000));
                        windCoordinates.add(Utils.coordinateDataToCoordinate(arrowEnd));
                        windCoordinates.addAll(getArrow(windDirection, arrowEnd));
                        windDirections.add(new CoordinateLine(windCoordinates).setColor(marker.weatherData.getWindColour()));
                    }
                }

                if (marker.square != null) {
                    CoordinateLine newSquare = new CoordinateLine(marker.square.stream()
                            .map(coordinateData -> new Coordinate(coordinateData.getLat(), coordinateData.getLng()))
                            .collect(Collectors.toList()));
                    marinequadrates.put(newMarker.getId(), newSquare);
                }
                markers.add(newMarker);
                markerMapping.put(newMarker.getId(), marker);
                previousMarker = marker.coordinate;
            }
            path = new CoordinateLine(coordinates).setColor(Color.valueOf(color)).setVisible(true);
        }

        private void showShipElements(final @NotNull MapView mapView) {
            markers.forEach(mapView::addMarker);
            mapView.addCoordinateLine(path);
            windDirections.forEach(mapView::addCoordinateLine);
            marinequadrates.values().forEach(mapView::addCoordinateLine);
            labels.forEach(mapView::addLabel);
        }

        private void deleteShipElements(final @NotNull MapView mapView) {
            markers.forEach(mapView::removeMarker);
            markers.forEach(marker -> markerMapping.remove(marker.getId()));
            mapView.removeCoordinateLine(path);
            windDirections.forEach(mapView::addCoordinateLine);
            marinequadrates.values().forEach(mapView::removeCoordinateLine);
            labels.forEach(mapView::removeLabel);
        }

        void setup(final @NotNull BooleanProperty markersVisibility,
                   final @NotNull BooleanProperty pathVisibility) {
            markers.forEach(marker -> markersVisibility.bindBidirectional(marker.visibleProperty()));
            pathVisibility.bindBidirectional(path.visibleProperty());

            bindCheckBoxProperty(labels, markersVisibility, pathVisibility, showLabelsCheckBox);
            bindCheckBoxProperty(marinequadrates.values(), markersVisibility, pathVisibility, showSquaresCheckBox);
            bindCheckBoxProperty(windDirections, markersVisibility, pathVisibility, showWindDirectionsCheckBox);

            setLabelsState(markersVisibility, pathVisibility);
            setSquaresState(markersVisibility, pathVisibility);
            setWindDirectionsState(markersVisibility, pathVisibility);
        }

        private void setMarkersState(boolean visible) {
            markers.forEach(marker -> marker.setVisible(visible));
        }

        private void setPathState(boolean visible) {
            path.setVisible(visible);
        }

        private <T extends MapElement> void bindCheckBoxProperty(final @NotNull Collection<T> property,
                                                                 final @NotNull BooleanProperty markersVisibility,
                                                                 final @NotNull BooleanProperty pathVisibility,
                                                                 final @NotNull CheckBox propertyCheckBox) {
            ChangeListener<Boolean> booleanChangeListener = (observableValue, wasSelected, isSelected) ->
                    setCheckBoxPropertyState(property, markersVisibility, pathVisibility, propertyCheckBox);
            markersVisibility.addListener(booleanChangeListener);
            pathVisibility.addListener(booleanChangeListener);
            propertyCheckBox.selectedProperty().addListener(booleanChangeListener);
        }

        private void setSquaresState(final @NotNull BooleanProperty markersVisibility,
                                     final @NotNull BooleanProperty pathVisibility) {
            setCheckBoxPropertyState(marinequadrates.values(), markersVisibility, pathVisibility, showSquaresCheckBox);
        }

        private void setLabelsState(final @NotNull BooleanProperty markersVisibility,
                                    final @NotNull BooleanProperty pathVisibility) {
            setCheckBoxPropertyState(labels, markersVisibility, pathVisibility, showSquaresCheckBox);
        }

        private void setWindDirectionsState(final @NotNull BooleanProperty markersVisibility,
                                            final @NotNull BooleanProperty pathVisibility) {
            setCheckBoxPropertyState(windDirections, markersVisibility, pathVisibility, showWindDirectionsCheckBox);
        }

        private <T extends MapElement> void setCheckBoxPropertyState(final @NotNull Collection<T> property,
                                                                     final @NotNull BooleanProperty markersVisibility,
                                                                     final @NotNull BooleanProperty pathVisibility,
                                                                     final @NotNull CheckBox propertyCheckBox) {
            property.forEach(line -> line.setVisible(
                    (markersVisibility.get() || pathVisibility.get()) && propertyCheckBox.isSelected()));
        }
    }
}
