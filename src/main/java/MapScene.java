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
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.control.cell.PropertyValueFactory;
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

    private TableView<JournalRecord> table = new TableView<>();
    private TableColumn dateColumn = new TableColumn("Date");
    private TableColumn shipColumn = new TableColumn("Ship");
    private TableColumn latLngCoodrColumn = new TableColumn("Lat/Lng coordinate");
    private TableColumn latColumn = new TableColumn("Latitude");
    private TableColumn lngColumn = new TableColumn("Longtitude");
    private TableColumn mqkColumn = new TableColumn("MQK");
    private TableColumn commentColumn = new TableColumn("Comment");
    private Accordion tableLeftPanel = new Accordion();
    private TitledPane tableContentTitle = new TitledPane();
    private BorderPane tableBorderPane = new BorderPane();

    private Button addButton = new Button("Add");
    private TextField dateField = new TextField();
    private TextField shipField = new TextField();
    private Label latLabel = new Label("Lat: ");
    private Label lngLabel = new Label(", Lng: ");
    private TextField latDegField = new TextField();
    private Label degLatLabel = new Label("°");
    private TextField latMinField = new TextField();
    private Label minLatLabel = new Label("′");
    private TextField lngDegField = new TextField();
    private Label degLngLabel = new Label("°");
    private TextField lngMinField = new TextField();
    private Label minLngLabel = new Label("′");
    private TextField mqkTextField = new TextField();
    private TextField commentField = new TextField();
    private HBox coordBox = new HBox(
            latLabel,
            latDegField, degLatLabel, latMinField, minLatLabel,
            lngLabel,
            lngDegField, degLngLabel, lngMinField, minLngLabel);
    private HBox addToTableBox = new HBox(
            dateField, shipField,
            coordBox,
            mqkTextField,
            commentField,
            addButton);

    private VBox tableBox = new VBox(table, addToTableBox);

    private BorderPane mapPane = new BorderPane();
    private StackPane mapViewStack = new StackPane();
    private MapView mapView = new MapView();
    private Accordion leftPanel = new Accordion();

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
    private RadioButton osmMapRadioButton = new RadioButton("OpenStreetMap");
    private RadioButton bingRoadMapRadioButton = new RadioButton("Bing road");
    private RadioButton bingArealMapRadioButton = new RadioButton("Bing areal");
    private ToggleGroup mapTypeGroup = new ToggleGroup();
    private Label bingApiLabel = new Label("Bing Api: ");
    private TextField bingApiField = new TextField();
    private HBox bingApiBox = new HBox(bingApiLabel, bingApiField);

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

    private ImageModeScene imageModeScene;

    public MapScene(final @NotNull Scene scene, final @NotNull Stage stage) {
        imageModeScene = new ImageModeScene(x -> {
            imageModeButton.setDisable(false);
            return null;
        });

        final OfflineCache offlineCache = mapView.getOfflineCache();
        final String cacheDir = Path.of(System.getProperty("java.io.tmpdir"), "seamap-cache").toString();

        try {
            Files.createDirectories(Paths.get(cacheDir));
            offlineCache.setCacheDirectory(cacheDir);
            offlineCache.setActive(true);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        tableBox.setSpacing(600);

        latLngCoodrColumn.getColumns().addAll(latColumn, lngColumn);
        table.getColumns().addAll(dateColumn, shipColumn, latLngCoodrColumn, mqkColumn, commentColumn);
        dateColumn.setMinWidth(200);
        shipColumn.setMinWidth(150);
        latColumn.setMinWidth(200);
        lngColumn.setMinWidth(200);
        mqkColumn.setMinWidth(150);
        commentColumn.setMinWidth(400);

        tableBorderPane.setCenter(tableBox);
        tableBorderPane.setLeft(tableLeftPanel);
        tableTap.setContent(tableBorderPane);

        dateField.setMinWidth(150);
        latDegField.setMaxWidth(50);
        latMinField.setMaxWidth(50);
        lngDegField.setMaxWidth(50);
        lngMinField.setMaxWidth(50);
        commentField.setMinWidth(400);

        addToTableBox.setSpacing(10);
        dateField.setPromptText("DD.MM.YYYY г. HH.MM");
        shipField.setPromptText("Ship name");
        commentField.setPromptText("Comment");

        dateColumn.setCellFactory(new PropertyValueFactory<JournalRecord, String>("date"));
        shipColumn.setCellFactory(new PropertyValueFactory<JournalRecord, String>("ship"));
        latColumn.setCellFactory(new PropertyValueFactory<JournalRecord, Double>("lat"));
        lngColumn.setCellFactory(new PropertyValueFactory<JournalRecord, Double>("lng"));
        mqkColumn.setCellFactory(new PropertyValueFactory<JournalRecord, String>("mqk"));
        commentColumn.setCellFactory(new PropertyValueFactory<JournalRecord, String>("comment"));

        tableContentTitle.setText("Table content");
        tableLeftPanel.setMinWidth(400);
        tableLeftPanel.getPanes().add(tableContentTitle);

        addButton.setOnMouseClicked(mouseEvent -> {
            JournalRecord record = new JournalRecord();
            record.date = dateField.getText();
            record.ship = shipField.getText();
            record.lat = Double.valueOf(latDegField.getText()) + Double.valueOf(latMinField.getText()) * 1 / 60;
            record.lng = Double.valueOf(lngMinField.getText()) + Double.valueOf(lngMinField.getText()) * 1 / 60;
            record.comment = commentField.getText();

            table.getItems().add(record);

            dateField.clear();
            shipField.clear();
            latDegField.clear();
            latMinField.clear();
            lngDegField.clear();
            lngMinField.clear();
            mqkTextField.clear();
            commentField.clear();
        });

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

            newWindow.setX(stage.getX() + 200);
            newWindow.setY(stage.getY() + 100);
            newWindow.setResizable(false);

            StackPane newStack = new StackPane();
            imageModeScene.setBackgroundImage(mapSnapshot);
            newStack.getChildren().add(imageModeScene.getMainPane());
            Scene secondScene = new Scene(newStack, scene.getWidth(), scene.getHeight());

            imageModeButton.setDisable(true);
            newWindow.setScene(secondScene);
            newWindow.show();
        });

        mapSettingsPaneBox.setSpacing(5.0);
        mapSettingsPaneBox.getChildren().addAll(
                showAll, showOnlyMarkers, showOnlyPaths, customMode,
                visibilityControlsTree,
                showSquaresCheckBox,
                imageModeButton);

        // Initialize MapSettings

        mapTypeGroup.getToggles().addAll(osmMapRadioButton, bingRoadMapRadioButton, bingArealMapRadioButton);
        contentSettingsPaneBox.getChildren().addAll(osmMapRadioButton, bingArealMapRadioButton, bingRoadMapRadioButton, bingApiBox);
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
                Marker newMarker = new Marker(getClass().getResource(color + ".png"), -12, -12)
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