import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sothawo.mapjfx.*;
import com.sothawo.mapjfx.event.MapViewEvent;
import com.sothawo.mapjfx.event.MarkerEvent;
import com.sothawo.mapjfx.offline.OfflineCache;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.DocumentEvent;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MapScene {
    private TabPane mainPane = new TabPane();
    private Tab mapTab = new Tab("Map");
    private Tab tableTap = new Tab("Table");

    private BorderPane mapPane = new BorderPane();
    private StackPane mapViewStack = new StackPane();
    private MapView mapView = new MapView();
    private Accordion leftPanel = new Accordion();
    private Canvas mapCanvas = new Canvas();


    private VBox mapSettingsPaneBox = new VBox();
    private TitledPane mapSettings = new TitledPane("Map settings", mapSettingsPaneBox);
    private ToggleGroup visibilityToggle = new ToggleGroup();
    private RadioButton showAll = new RadioButton("Show everything");
    private RadioButton showOnlyMarkers = new RadioButton("Show markers only");
    private RadioButton showOnlyPaths = new RadioButton("Show paths only");
    private RadioButton customMode = new RadioButton("Custom mode");
    private CheckBox showSquaresCheckBox = new CheckBox("Show relevant marinequadrates");
    private Button imageModeButton = new Button("Open image mode");


    private VBox contentSettingsPaneBox = new VBox();
    private TitledPane contentSettings = new TitledPane("Content settings", contentSettingsPaneBox);

    private HBox statusBar = new HBox();
    private Label currentCoordinatesLabel = new Label("Current coordinates (lat, lng):");
    private Label currentCoordinatesValueLabel  = new Label();

    private Image crossImage = new Image(getClass().getResourceAsStream("cross.png"));
    private ImageView crossImageView = new ImageView(crossImage);
    private Button closeHelpButton = new Button();
    private HBox crossButtonBox = new HBox();
    private VBox helpBox = new VBox();
    private TextArea generalInfoContent = new TextArea();

    private TreeView<String> visibilityControlsTree = new TreeView<>();

    private ArrayList<ShipMapElements> shipMapElements = new ArrayList<>();
    private HashMap<String, MarkerData> markerMapping = new HashMap<>();


    public MapScene(final @NotNull Scene scene) {
        final OfflineCache offlineCache = mapView.getOfflineCache();
        final String cacheDir = Path.of(System.getProperty("java.io.tmpdir"), "seamap-cache").toString();

        try {
            Files.createDirectories(Paths.get(cacheDir));
            offlineCache.setCacheDirectory(cacheDir);
            offlineCache.setActive(true);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        mapView.initializedProperty().addListener((observableValue, aBoolean, t1) -> {
            mapView.setCenter(new Coordinate(10.0, 10.0));
            mapView.setMapType(MapType.OSM);
            loadMapData(getClass().getResource("data.json").getPath());
            mapView.setZoom(5);
        });
        mapView.initialize();

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

        CheckBoxTreeItem<String> visibilityControlsRootItem = new CheckBoxTreeItem<>("Content visibility");
        visibilityControlsRootItem.setExpanded(true);
        visibilityControlsTree.setRoot(visibilityControlsRootItem);
        visibilityControlsTree.setShowRoot(true);
        visibilityControlsTree.setCellFactory(CheckBoxTreeCell.forTreeView());
        imageModeButton.setOnMouseClicked(mouseEvent -> {
            WritableImage mapSnapshot = mapView.snapshot(new SnapshotParameters(), null);

            Stage newWindow = new Stage();
            newWindow.setTitle("Image mode");

            StackPane newStack = new StackPane();
            newStack.getChildren().add(new ImageModeScene(mapSnapshot, newWindow).getMainPane());
            Scene secondScene = new Scene(newStack, scene.getWidth(), scene.getHeight());

            newWindow.setScene(secondScene);

            // Set position of second window, related to primary window.

            newWindow.setX(scene.getWindow().getX() + 100);
            newWindow.setY(scene.getWindow().getY() + 100);
            newWindow.setResizable(false);

            newWindow.show();
        });

        mapSettingsPaneBox.setSpacing(5.0);
        mapSettingsPaneBox.getChildren().addAll(
                showAll, showOnlyMarkers, showOnlyPaths, customMode,
                visibilityControlsTree,
                showSquaresCheckBox,
                imageModeButton);

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
        statusBar.getChildren().addAll(currentCoordinatesLabel, currentCoordinatesValueLabel);

        mapCanvas.widthProperty().bind(mapView.widthProperty());
        mapCanvas.heightProperty().bind(mapView.heightProperty());

        mapViewStack.getChildren().addAll(mapView);
        mapPane.setCenter(mapViewStack);
        mapPane.setBottom(statusBar);
        mapPane.setLeft(leftPanel);

        mainPane.getTabs().addAll(mapTab, tableTap);
        mapTab.setContent(mapPane);

        scene.addEventHandler(KeyEvent.KEY_PRESSED, t -> {
            if (t.getCode()== KeyCode.ESCAPE) {
                if (mapPane.getRight() != null) {
                    mapPane.setRight(null);
                } else {
                    Stage sb = (Stage) scene.getWindow();
                    sb.close();
                }
            }
        });
    }

    public Node getMainPane() {
        return mainPane;
    }

    void loadMapData(String filename) {
        Gson gson = new Gson();
        JsonReader reader;
        try {
            reader = new JsonReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            // TODO: add log
            return;
        }

        MapData mapData = gson.fromJson(reader, MapData.class);
        mapView.setCenter(new Coordinate(mapData.mapCenterLat, mapData.mapCenterLng));
        mapView.addEventHandler(MarkerEvent.MARKER_CLICKED, event -> {
                MarkerData markerData = markerMapping.get(event.getMarker().getId());
                generalInfoContent.setText(
                        "Ship: " + markerData.ship + "\n" +
                        "Date: " + markerData.date + "\n" +
                        "Coordinates: \n\tlat: " + markerData.coordinate.lat + "\n\tlng: " + markerData.coordinate.lng + "\n" +
                        "Original coordinates: " + markerData.originalCoordinates + "\n");
                if (markerData.comment != null && !markerData.comment.isEmpty())
                generalInfoContent.setText(generalInfoContent.getText() + "Comment: " + markerData.comment + "\n");
                mapPane.setRight(helpBox);
        });

        for (ShipData ship : mapData.ships) {
            final ShipMapElements shipElements = new ShipMapElements(ship.markers, ship.color);
            final CheckBoxTreeItem<String> shipCheckBoxItem = new CheckBoxTreeItem<>(ship.shipName + " (" + ship.color + ")");
            final CheckBoxTreeItem<String> dataPointsCheckBoxItem = new CheckBoxTreeItem<>("Data points visible");
            final CheckBoxTreeItem<String> shipRoutesCheckBoxItem = new CheckBoxTreeItem<>("Path visible");
            shipCheckBoxItem.getChildren().add(dataPointsCheckBoxItem);
            shipCheckBoxItem.getChildren().add(shipRoutesCheckBoxItem);

            dataPointsCheckBoxItem.setSelected(!showOnlyPaths.isSelected());
            shipRoutesCheckBoxItem.setSelected(!showOnlyMarkers.isSelected());
            shipElements.setMarkersState(!showOnlyPaths.isSelected());
            shipElements.setPathState(!showOnlyMarkers.isSelected());

            shipElements.bindMarkersWith(dataPointsCheckBoxItem.selectedProperty());
            shipElements.bindPathWith(shipRoutesCheckBoxItem.selectedProperty());
            shipElements.bindSquaresWith(dataPointsCheckBoxItem.selectedProperty(), shipRoutesCheckBoxItem.selectedProperty());

            visibilityControlsTree.getRoot().getChildren().add(shipCheckBoxItem);
            shipElements.showShipElements(mapView);
            shipMapElements.add(shipElements);
        }
    }

    private class ShipMapElements {
        private final ArrayList<Marker> markers = new ArrayList<>();
        private final CoordinateLine path;
        private final HashMap<String, CoordinateLine> marinequadrates = new HashMap<>();

        private ShipMapElements(final @NotNull List<MarkerData> markerData, @NotNull String color) {
            ArrayList<Coordinate> coordinates = new ArrayList<>();
            markerData.forEach(marker -> {
                Coordinate newCoordinate = new Coordinate(marker.coordinate.lat, marker.coordinate.lng);
                Marker newMarker = Marker
                        .createProvided(Marker.Provided.valueOf(color.toUpperCase()))
                        .setPosition(newCoordinate);
                coordinates.add(newCoordinate);

                if (marker.square != null) {
                    CoordinateLine newSquare = new CoordinateLine(marker.square.stream()
                            .map(coordinateData -> new Coordinate(coordinateData.lat, coordinateData.lng))
                            .collect(Collectors.toList()));
                    marinequadrates.put(newMarker.getId(), newSquare);
                }
                markers.add(newMarker);
                markerMapping.put(newMarker.getId(), marker);
            });
            path = new CoordinateLine(coordinates).setColor(Color.valueOf(color)).setVisible(true);
        }

        private void showShipElements(final @NotNull MapView mapView) {
            markers.forEach(mapView::addMarker);
            mapView.addCoordinateLine(path);
            marinequadrates.values().forEach(mapView::addCoordinateLine);
        }

        private void deleteShipElements(final @NotNull MapView mapView) {
            markers.forEach(mapView::removeMarker);
            mapView.removeCoordinateLine(path);
        }

        private void setMarkersState(boolean visible) {
            markers.forEach(marker -> marker.setVisible(visible));
        }

        private void setPathState(boolean visible) {
            path.setVisible(visible);
        }

        private void bindMarkersWith(final @NotNull BooleanProperty bindingProperty) {
            markers.forEach(marker -> bindingProperty.bindBidirectional(marker.visibleProperty()));
        }

        private void bindPathWith(final @NotNull BooleanProperty bindingProperty) {
            bindingProperty.bindBidirectional(path.visibleProperty());
        }

        private void bindSquaresWith(final @NotNull BooleanProperty markersVisibility,
                                     final @NotNull BooleanProperty pathVisibility) {
            markersVisibility.addListener((observableValue, wasSelected, isSelected) ->
                    marinequadrates
                            .values()
                            .forEach(line -> line.setVisible(
                                    (markersVisibility.get() || pathVisibility.get()) && showSquaresCheckBox.isSelected())));
            pathVisibility.addListener((observableValue, wasSelected, isSelected) ->
                    marinequadrates
                            .values()
                            .forEach(line -> line.setVisible(
                                    (markersVisibility.get() || pathVisibility.get()) && showSquaresCheckBox.isSelected())));
            showSquaresCheckBox.selectedProperty().addListener((observableValue, oldValue, newValue) ->
                    marinequadrates
                            .values()
                            .forEach(line -> line.setVisible(
                                    (markersVisibility.get() || pathVisibility.get()) && showSquaresCheckBox.isSelected())));
        }
    }
}