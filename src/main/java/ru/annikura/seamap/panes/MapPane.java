package ru.annikura.seamap.panes;

import com.sothawo.mapjfx.*;
import com.sothawo.mapjfx.event.MapViewEvent;
import com.sothawo.mapjfx.event.MarkerEvent;
import com.sothawo.mapjfx.offline.OfflineCache;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import ru.annikura.seamap.ImageModeScene;
import ru.annikura.seamap.data.*;
import ru.annikura.seamap.journal.RecordsProcesser;
import ru.annikura.seamap.utils.Holder;
import ru.annikura.seamap.utils.Utils;

import javax.rmi.CORBA.Util;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class MapPane {
    private BorderPane mapPane = new BorderPane();
    private StackPane mapViewStack = new StackPane();
    private MapView mapView = new MapView();
    private Accordion leftPanel = new Accordion();

    private VBox mapSettingsPaneBox = new VBox();
    private TitledPane mapSettings = new TitledPane("Content settings", mapSettingsPaneBox);
    private ToggleGroup visibilityToggle = new ToggleGroup();
    private RadioButton showAll = new RadioButton("Show everything");
    private RadioButton showOnlyMarkers = new RadioButton("Show markers only");
    private RadioButton showOnlyPaths = new RadioButton("Show paths only");
    private RadioButton customMode = new RadioButton("Custom mode");
    private CheckBox showSquaresCheckBox = new CheckBox("Show relevant marinequadrates");
    private CheckBox showLabelsCheckBox = new CheckBox("Show relevant time marks");
    private CheckBox showWindDirectionsCheckBox = new CheckBox("Show relevant wind directions");
    private Button imageModeButton = new Button("Open image mode");

    private VBox contentSettingsPaneBox = new VBox();
    private TitledPane contentSettings = new TitledPane("Map settings", contentSettingsPaneBox);
    private RadioButton osmMapRadioButton = new RadioButton("OpenStreetMap");
    private RadioButton bingRoadMapRadioButton = new RadioButton("Bing road");
    private RadioButton bingArealMapRadioButton = new RadioButton("Bing areal");
    private ToggleGroup mapTypeGroup = new ToggleGroup();
    private Label bingApiLabel = new Label("Bing Api: ");
    private TextField bingApiField = new TextField();
    private HBox bingApiBox = new HBox(bingApiLabel, bingApiField);
    private Button loadFromTableButton = new Button("Reload data from table");

    private HBox statusBar = new HBox();
    private Label currentCoordinatesLabel = new Label("Current coordinates (lat, lng):");
    private Label currentCoordinatesValueLabel  = new Label();

    private Image crossImage = new Image(getClass().getResourceAsStream("/cross.png"));
    private ImageView crossImageView = new ImageView(crossImage);
    private Button closeHelpButton = new Button();
    private HBox crossButtonBox = new HBox();
    private VBox helpBox = new VBox();
    private TextArea generalInfoContent = new TextArea();

    private TreeView<String> visibilityControlsTree = new TreeView<>();
    private Map<String, CheckBoxTreeItem<String>> shipVisibilityControls = new HashMap<>();

    private ArrayList<MapPane.ShipMapElements> shipMapElements = new ArrayList<>();
    private HashMap<String, MarkerData> markerMapping = new HashMap<>();

    private ImageModeScene imageModeScene;

    private Holder<MapLabel> tipLabelHolder = new Holder<>();

    private MapData displayedData = new MapData();

    public MapPane(@NotNull Stage stage, @NotNull JournalPane journalPane, @NotNull WeatherPane weatherPane) {
        imageModeScene = new ImageModeScene(x -> {
            imageModeButton.setDisable(false);
            return null;
        });

        final OfflineCache offlineCache = mapView.getOfflineCache();
        final String cacheDir = "seamap-cache";

        mapView.setCustomMapviewCssURL(getClass().getResource("/custom_mapview.css"));

        mapView.initializedProperty().addListener((observableValue, aBoolean, t1) -> {
            mapView.setCenter(new Coordinate(10.0, 10.0));
            mapView.setMapType(MapType.OSM);
            mapView.addEventHandler(MarkerEvent.MARKER_CLICKED, event -> {
                // TODO: validate that marker has such data
                MarkerData markerData = markerMapping.get(event.getMarker().getId());
                generalInfoContent.setText(generateGeneralReport(markerData));
                mapPane.setRight(helpBox);
            });
            mapView.setZoom(5);
            mapView.addEventHandler(MouseEvent.MOUSE_EXITED, mouseEvent -> {
                if (tipLabelHolder.getValue() != null) {
                    tipLabelHolder.getValue().setVisible(false);
                }
            });

            try {
                Files.createDirectories(Paths.get(cacheDir));
                offlineCache.setCacheDirectory(cacheDir);
                offlineCache.setActive(true);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            } catch (NullPointerException ignored) {}
        });

        mapView.initialize();

        // Setting up left pane.

        leftPanel.setMinWidth(400);
        leftPanel.getPanes().addAll(mapSettings, contentSettings);
        leftPanel.setExpandedPane(mapSettings);

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
        showAll.setSelected(true);

        CheckBoxTreeItem<String> visibilityControlsRootItem = new CheckBoxTreeItem<>("Content visibilityRange");
        visibilityControlsRootItem.setExpanded(true);
        visibilityControlsTree.setRoot(visibilityControlsRootItem);
        visibilityControlsTree.setShowRoot(true);
        visibilityControlsTree.setCellFactory(CheckBoxTreeCell.forTreeView());
        imageModeButton.setOnMouseClicked(mouseEvent -> {
            WritableImage mapSnapshot = mapView.snapshot(new SnapshotParameters(), null);

            Stage newWindow = new Stage();
            newWindow.setOnCloseRequest(e -> imageModeButton.setDisable(false));

            newWindow.setX(stage.getX() + 200);
            newWindow.setY(stage.getY() + 100);
            newWindow.setResizable(false);

            StackPane newStack = new StackPane();
            imageModeScene.setBackgroundImage(mapSnapshot);
            newStack.getChildren().add(imageModeScene.getMainPane());
            Scene secondScene = new Scene(newStack, stage.getScene().getWidth(), stage.getScene().getHeight());

            imageModeButton.setDisable(true);
            newWindow.setScene(secondScene);
            newWindow.show();
        });

        mapSettingsPaneBox.setSpacing(5.0);
        mapSettingsPaneBox.getChildren().addAll(
                showAll, showOnlyMarkers, showOnlyPaths, customMode,
                visibilityControlsTree,
                showSquaresCheckBox,
                showLabelsCheckBox,
                showWindDirectionsCheckBox,
                imageModeButton,
                loadFromTableButton);

        // Initialize MapSettings

        mapTypeGroup.getToggles().addAll(osmMapRadioButton, bingRoadMapRadioButton, bingArealMapRadioButton);
        contentSettingsPaneBox.getChildren().addAll(
                osmMapRadioButton,
                bingArealMapRadioButton,
                bingRoadMapRadioButton,
                bingApiBox);
        contentSettingsPaneBox.setSpacing(10);
        contentSettingsPaneBox.setStyle("-fx-padding: 15px;");
        osmMapRadioButton.setSelected(true);
        bingRoadMapRadioButton.setDisable(true);
        bingArealMapRadioButton.setDisable(true);
        bingApiField.textProperty().addListener(actionEvent -> {
            bingArealMapRadioButton.setDisable(bingApiField.getText().isEmpty());
            bingRoadMapRadioButton.setDisable(bingApiField.getText().isEmpty());
        });
        mapTypeGroup.selectedToggleProperty().addListener((observableValue, oldToggle, newToogle) -> {
            if (newToogle.equals(osmMapRadioButton)) {
                mapView.setMapType(MapType.OSM);
            }
            if (newToogle.equals(bingArealMapRadioButton)) {
                mapView.setBingMapsApiKey(bingApiField.getText());
                mapView.setMapType(MapType.BINGMAPS_AERIAL);
            }
            if (newToogle.equals(bingRoadMapRadioButton)) {
                mapView.setBingMapsApiKey(bingApiField.getText());
                mapView.setMapType(MapType.BINGMAPS_ROAD);
            }
        });

        contentSettings.setContent(contentSettingsPaneBox);

        closeHelpButton.setGraphic(crossImageView);
        closeHelpButton.getStylesheets().add("cross_button.css");
        closeHelpButton.setOnMouseClicked(mouseEvent -> mapPane.setRight(null));

        crossButtonBox.setAlignment(Pos.CENTER_RIGHT);
        crossButtonBox.getChildren().add(closeHelpButton);
        helpBox.setMaxWidth(400);

        Label generalInfoHeading = new Label("General info");
        generalInfoHeading.setFont(Font.font("Verdana", FontWeight.BOLD, 25));
        generalInfoHeading.setOpaqueInsets(new Insets(20));
        generalInfoContent.setWrapText(true);
        generalInfoContent.setEditable(false);
        generalInfoContent.setMinHeight(500);
        generalInfoContent.setStyle(
                "-fx-background-color: transparent ;" +
                        "-fx-background-insets: 0px ;");
        VBox innerHelpBox = new VBox(generalInfoHeading, generalInfoContent);
        innerHelpBox.setStyle("-fx-padding: 15px;");
        helpBox.getChildren().addAll(closeHelpButton, innerHelpBox);

        statusBar.setSpacing(10);
        statusBar.setAlignment(Pos.CENTER);

        mapView.addEventHandler(MapViewEvent.MAP_POINTER_MOVED, event -> currentCoordinatesValueLabel.setText(
                String.format("%.10f, %.10f", event.getCoordinate().getLatitude(), event.getCoordinate().getLongitude())));

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
                    if (newDistance < CoordinateData.EPS * Math.pow(2.0, 18 - mapView.getZoom()) && (bestDistance == null || newDistance < bestDistance)) {
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
            MapLabel newMapLabel = new MapLabel(coordinateApproximation.date.substring(11), 12, 12).setCssClass("map-label");
            tipLabelHolder.setValue(newMapLabel);
            newMapLabel.setPosition(Utils.coordinateDataToCoordinate(newCoordinate));
            mapView.addLabel(newMapLabel);
            newMapLabel.setVisible(true);
        });

        mapView.addEventHandler(MapViewEvent.MAP_EXTENT, event -> {
            event.consume();
            mapView.setExtent(event.getExtent());
        });
        statusBar.getChildren().addAll(currentCoordinatesLabel, currentCoordinatesValueLabel);

        mapViewStack.getChildren().addAll(mapView);
        mapPane.setCenter(mapViewStack);
        mapPane.setBottom(statusBar);
        mapPane.setLeft(leftPanel);

        loadFromTableButton.setOnAction(e -> {
            clearOldData();
            displayedData = RecordsProcesser.processRecords(journalPane.getRecords(), weatherPane.getRecords());
            loadMapData(displayedData);
        });

        stage.getScene().addEventHandler(KeyEvent.KEY_PRESSED, t -> {
            if (t.getCode()== KeyCode.ESCAPE) {
                if (mapPane.getRight() != null) {
                    mapPane.setRight(null);
                }
            }
        });
    }

    public Node getMapPane() {
        return mapPane;
    }

    private void clearOldData() {
        visibilityControlsTree.getRoot().getChildren().clear();
        shipVisibilityControls.clear();
        for (MapPane.ShipMapElements shipMapElement : shipMapElements) {
            shipMapElement.deleteShipElements(mapView);
        }
        shipMapElements.clear();

        mapPane.setRight(null);
        showAll.setSelected(true);
    }

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

    private void loadMapData(@NotNull MapData mapData) {
        mapView.setCenter(new Coordinate(mapData.mapCenterLat, mapData.mapCenterLng));

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

    @NotNull
    private String generateGeneralReport(@NotNull MarkerData markerData) {
        StringBuilder builder = new StringBuilder();
        builder.append("Ship: ");
        builder.append(markerData.ship);
        builder.append(System.lineSeparator());
        builder.append("Date: ");
        builder.append(markerData.date);
        builder.append(System.lineSeparator());
        builder.append("Coordinate: ");
        builder.append(System.lineSeparator());
        builder.append("\t");
        builder.append("lat: ");
        builder.append(markerData.coordinate.getLat());
        builder.append(System.lineSeparator());
        builder.append("\t");
        builder.append("lng: ");
        builder.append(markerData.coordinate.getLng());
        builder.append(System.lineSeparator());

        if (!markerData.originalCoordinates.equals("")) {
            builder.append("Original coordinates: ");
            builder.append(markerData.originalCoordinates);
            builder.append(System.lineSeparator());
        }

        if (markerData.comment != null && !markerData.comment.isEmpty()) {
            builder.append("Comment: ");
            builder.append(markerData.comment);
            builder.append(System.lineSeparator());
        }

        if (markerData.weatherData != null) {
            builder.append(System.lineSeparator());
            builder.append(generateWeatherReport(markerData.weatherData));
        }

        return builder.toString();
    }

    @NotNull
    private String generateWeatherReport(@NotNull WeatherData weatherData) {
        StringBuilder builder = new StringBuilder();
        builder.append("Weather data");
        builder.append(System.lineSeparator());
        if (weatherData.source != null) {
            builder.append("Source: ");
            builder.append(weatherData.source);
            builder.append(System.lineSeparator());
        }

        if (weatherData.windDirection != null) {
            builder.append("Wind direction: ");
            builder.append(weatherData.windDirection);
            builder.append(System.lineSeparator());
        }

        if (weatherData.windStrength != null) {
            builder.append("Wind windStrength: ");
            builder.append(weatherData.windStrength);
            builder.append(System.lineSeparator());
        }


        if (weatherData.visibilityRange != null) {
            builder.append("Visibility range: ");
            builder.append(weatherData.visibilityRange);
            builder.append(System.lineSeparator());
        }
        return builder.toString();
    }
}


