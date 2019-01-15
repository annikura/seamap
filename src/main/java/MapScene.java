import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sothawo.mapjfx.*;
import com.sothawo.mapjfx.event.MapViewEvent;
import com.sothawo.mapjfx.event.MarkerEvent;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapScene {
    private BorderPane mainPane = new BorderPane();
    private StackPane mapViewStack = new StackPane();
    private MapView mapView = new MapView();
    private Accordion leftPanel = new Accordion();
    private Canvas mapCanvas = new Canvas();


    private VBox mapSettingsPaneBox = new VBox();
    private TitledPane mapSettings = new TitledPane("Map settings", mapSettingsPaneBox);
    private CheckBox disableMarkersCheckBox = new CheckBox("Disable markers");
    private Button showMarkers = new Button("Show all markers");
    private CheckBox disablePathsCheckBox = new CheckBox("Disable paths");
    private Button showPaths = new Button("Show all paths");

    private VBox contentSettingsPaneBox = new VBox();
    private TitledPane contentSettings = new TitledPane("Content settings", contentSettingsPaneBox);

    private HBox statusBar = new HBox();
    private Label currentCoordinatesLabel = new Label("Current coordinates (lat, lng):");
    private Label currentCoordinatesValueLabel  = new Label();

    private Image crossImage = new Image(getClass().getResourceAsStream("cross.png"));
    private ImageView crossImageView = new ImageView(crossImage);
    private Button closeHelpButton = new Button();
    private VBox crossButtonBox = new VBox();
    private VBox helpBox = new VBox();
    private WebView webView = new WebView();


    private TreeView<String> visibilityControlsTree = new TreeView<>();

    private ArrayList<ShipMapElements> shipMapElements = new ArrayList<>();
    private HashMap<String, MarkerData> markerMapping = new HashMap<>();


    public MapScene() {
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

        CheckBoxTreeItem<String> visibilityControlsRootItem = new CheckBoxTreeItem<>("Content visibility");
        visibilityControlsRootItem.setExpanded(true);
        visibilityControlsTree.setRoot(visibilityControlsRootItem);
        visibilityControlsTree.setShowRoot(true);
        visibilityControlsTree.setCellFactory(CheckBoxTreeCell.forTreeView());

        mapSettingsPaneBox.setSpacing(5.0);
        disableMarkersCheckBox.selectedProperty().addListener((observableValue, aBoolean, isSelected) -> {
            if (isSelected) {
                shipMapElements.forEach(elements -> elements.setMarkersState(false));
            }
        });

        disablePathsCheckBox.selectedProperty().addListener((observableValue, aBoolean, isSelected) -> {
            if (isSelected) {
                shipMapElements.forEach(elements -> elements.setPathState(false));
            }
        });

        mapSettingsPaneBox.getChildren().addAll(visibilityControlsTree, disableMarkersCheckBox, disablePathsCheckBox);

        closeHelpButton.setGraphic(crossImageView);
        closeHelpButton.getStylesheets().add("cross_button.css");
        closeHelpButton.setOnMouseClicked(mouseEvent -> mainPane.setRight(null));

        crossButtonBox.setAlignment(Pos.BASELINE_RIGHT);
        crossButtonBox.fillWidthProperty();
        crossButtonBox.getChildren().add(closeHelpButton);
        helpBox.setMaxWidth(400);
        helpBox.getChildren().addAll(closeHelpButton, webView);

        statusBar.setSpacing(10);
        statusBar.setAlignment(Pos.CENTER);
        mapView.addEventHandler(MapViewEvent.MAP_POINTER_MOVED, event -> currentCoordinatesValueLabel.setText(
                String.format("%.10f, %.10f", event.getCoordinate().getLatitude(), event.getCoordinate().getLongitude())));
        statusBar.getChildren().addAll(currentCoordinatesLabel, currentCoordinatesValueLabel);

        mapCanvas.widthProperty().bind(mapView.widthProperty());
        mapCanvas.heightProperty().bind(mapView.heightProperty());

        mapViewStack.getChildren().addAll(mapView, mapCanvas);
        mainPane.setCenter(mapViewStack);
        mainPane.setBottom(statusBar);
        mainPane.setLeft(leftPanel);
    }

    public Node getMapScene() {
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
                webView.getEngine().loadContent(markerMapping.get(event.getMarker().getId()).toolTip);
                mainPane.setRight(helpBox);
        });

        for (ShipData ship : mapData.ships) {
            final ShipMapElements shipElements = new ShipMapElements(ship.markers, ship.color);

            final CheckBoxTreeItem<String> shipCheckBoxItem = new CheckBoxTreeItem<>(ship.ship_name + " (" + ship.color + ") visible");
            final CheckBoxTreeItem<String> dataPointsCheckBoxItem = new CheckBoxTreeItem<>("Data points visible");
            final CheckBoxTreeItem<String> shipRoutesCheckBoxItem = new CheckBoxTreeItem<>("Path visible");
            shipCheckBoxItem.getChildren().addAll(dataPointsCheckBoxItem, shipRoutesCheckBoxItem);

            dataPointsCheckBoxItem.selectedProperty().addListener((observableValue, aBoolean, isSelected) -> {
                if (isSelected) disableMarkersCheckBox.setSelected(false);
            });
            shipRoutesCheckBoxItem.selectedProperty().addListener((observableValue, aBoolean, isSelected) -> {
                if (isSelected) disablePathsCheckBox.setSelected(false);
            });

            dataPointsCheckBoxItem.setSelected(!disableMarkersCheckBox.isSelected());
            shipRoutesCheckBoxItem.setSelected(!disablePathsCheckBox.isSelected());
            shipElements.setMarkersState(!disableMarkersCheckBox.isSelected());
            shipElements.setPathState(!disablePathsCheckBox.isSelected());
            shipElements.bindMarkersWith(dataPointsCheckBoxItem.selectedProperty());
            shipElements.bindPathWith(shipRoutesCheckBoxItem.selectedProperty());

            visibilityControlsTree.getRoot().getChildren().add(shipCheckBoxItem);
            shipElements.showShipElements(mapView);
            shipMapElements.add(shipElements);
        }
    }

    private class ShipMapElements {
        private final ArrayList<Marker> markers = new ArrayList<>();
        private final CoordinateLine path;


        private ShipMapElements(final @NotNull List<MarkerData> markerData, @NotNull String color) {
            ArrayList<Coordinate> coordinates = new ArrayList<>();
            markerData.forEach(marker -> {
                Coordinate newCoordinate = new Coordinate(marker.lat, marker.lng);
                Marker newMarker = Marker
                        .createProvided(Marker.Provided.valueOf(color.toUpperCase()))
                        .setPosition(newCoordinate);
                coordinates.add(newCoordinate);
                markers.add(newMarker);
                markerMapping.put(newMarker.getId(), marker);
            });
            path = new CoordinateLine(coordinates).setColor(Color.valueOf(color)).setVisible(true);
        }

        private void showShipElements(final @NotNull MapView mapView) {
            markers.forEach(mapView::addMarker);
            mapView.addCoordinateLine(path);
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
    }
}