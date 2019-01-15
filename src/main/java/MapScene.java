import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sothawo.mapjfx.*;
import com.sothawo.mapjfx.event.MapViewEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class MapScene {
    private BorderPane mainPane = new BorderPane();
    private MapView mapView = new MapView();
    private Accordion leftPanel = new Accordion();

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

    private TreeView<String> visibilityControlsTree = new TreeView<>();


    private ArrayList<ShipMapElements> shipMapElements = new ArrayList<>();

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
        closeHelpButton.setOnMouseClicked(mouseEvent -> {
            mainPane.setRight(null);
        });

        crossButtonBox.setAlignment(Pos.BASELINE_RIGHT);
        crossButtonBox.fillWidthProperty();
        crossButtonBox.getChildren().add(closeHelpButton);
        helpBox.setMinWidth(400);
        helpBox.getChildren().add(closeHelpButton);

        statusBar.setSpacing(10);
        statusBar.setAlignment(Pos.CENTER);
        mapView.addEventHandler(MapViewEvent.MAP_POINTER_MOVED, event -> {
            currentCoordinatesValueLabel.setText(
                    String.format("%.10f, %.10f", event.getCoordinate().getLatitude(), event.getCoordinate().getLongitude()));
        });
        statusBar.getChildren().addAll(currentCoordinatesLabel, currentCoordinatesValueLabel);

        mainPane.setCenter(mapView);
        mainPane.setBottom(statusBar);
        // mainPane.setRight(helpBox);
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

        for (ShipData ship : mapData.ships) {
            final ArrayList<Coordinate> coordinates = new ArrayList<>();

            for (MarkerData marker : ship.markers) {
                coordinates.add(new Coordinate(marker.lat, marker.lng));
            }
            final ShipMapElements shipElements = new ShipMapElements(coordinates, ship.color);

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
}
