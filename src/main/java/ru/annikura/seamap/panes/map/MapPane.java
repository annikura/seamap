package ru.annikura.seamap.panes.map;

import com.sothawo.mapjfx.MapType;
import com.sothawo.mapjfx.event.MapViewEvent;
import com.sothawo.mapjfx.event.MarkerEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.annikura.seamap.data.MapData;
import ru.annikura.seamap.data.MarkerData;
import ru.annikura.seamap.data.ShipData;
import ru.annikura.seamap.data.WeatherData;
import ru.annikura.seamap.journal.ChangebleStorage;
import ru.annikura.seamap.journal.JournalRecord;
import ru.annikura.seamap.journal.RecordsProcesser;
import ru.annikura.seamap.journal.WeatherRecord;

public class MapPane {
    private BorderPane mapPane = new BorderPane();
    private MapViewerElemets mapViewerElemets;
    private MapData displayedData = new MapData();

    private Label currentCoordinatesValueLabel  = new Label();
    private TextArea generalInfoContent;
    Button deleteMarkerButton = new Button("Delete marker");

    @NotNull
    private Node createStatusBar() {
        HBox statusBar = new HBox();
        statusBar.setSpacing(10);
        statusBar.setAlignment(Pos.CENTER);

        mapViewerElemets.getMapView().addEventHandler(
                MapViewEvent.MAP_POINTER_MOVED, event -> currentCoordinatesValueLabel.setText(
                String.format("%.10f, %.10f", event.getCoordinate().getLatitude(), event.getCoordinate().getLongitude())));

        Label currentCoordinatesLabel = new Label("Current coordinates (lat, lng):");
        statusBar.getChildren().addAll(currentCoordinatesLabel, currentCoordinatesValueLabel);

        return statusBar;
    }

    @NotNull
    private Node createMapSettings() {
        RadioButton osmMapRadioButton = new RadioButton("OpenStreetMap");
        RadioButton bingRoadMapRadioButton = new RadioButton("Bing road");
        RadioButton bingArealMapRadioButton = new RadioButton("Bing areal");
        TextField bingApiField = new TextField();

        ToggleGroup mapTypeGroup = new ToggleGroup();
        mapTypeGroup.getToggles().addAll(osmMapRadioButton, bingRoadMapRadioButton, bingArealMapRadioButton);
        Label bingApiLabel = new Label("Bing Api: ");
        HBox bingApiBox = new HBox(bingApiLabel, bingApiField);
        VBox mapSettingsBox = new VBox(
                osmMapRadioButton,
                bingArealMapRadioButton,
                bingRoadMapRadioButton,
                bingApiBox);
        mapSettingsBox.setSpacing(10);
        mapSettingsBox.setStyle("-fx-padding: 15px;");
        osmMapRadioButton.setSelected(true);
        bingRoadMapRadioButton.setDisable(true);
        bingArealMapRadioButton.setDisable(true);
        bingApiField.textProperty().addListener(actionEvent -> {
            bingArealMapRadioButton.setDisable(bingApiField.getText().isEmpty());
            bingRoadMapRadioButton.setDisable(bingApiField.getText().isEmpty());
        });

        mapTypeGroup.selectedToggleProperty().addListener((observableValue, oldToggle, newToogle) -> {
            if (newToogle.equals(osmMapRadioButton)) {
                mapViewerElemets.getMapView().setMapType(MapType.OSM);
            }
            if (newToogle.equals(bingArealMapRadioButton)) {
                mapViewerElemets.getMapView().setBingMapsApiKey(bingApiField.getText());
                mapViewerElemets.getMapView().setMapType(MapType.BINGMAPS_AERIAL);
            }
            if (newToogle.equals(bingRoadMapRadioButton)) {
                mapViewerElemets.getMapView().setBingMapsApiKey(bingApiField.getText());
                mapViewerElemets.getMapView().setMapType(MapType.BINGMAPS_ROAD);
            }
        });

        return mapSettingsBox;
    }

    @NotNull
    private Node createInfoPanel() {
        Image crossImage = new Image(getClass().getResourceAsStream("/cross.png"));
        ImageView crossImageView = new ImageView(crossImage);
        Button closeHelpButton = new Button();
        closeHelpButton.setGraphic(crossImageView);
        closeHelpButton.getStylesheets().add("cross_button.css");
        closeHelpButton.setOnMouseClicked(mouseEvent -> mapPane.setRight(null));

        HBox crossButtonBox = new HBox();
        crossButtonBox.setAlignment(Pos.CENTER_RIGHT);
        crossButtonBox.getChildren().add(closeHelpButton);

        Label generalInfoHeading = new Label("General info");
        generalInfoHeading.setFont(Font.font("Verdana", FontWeight.BOLD, 25));
        generalInfoHeading.setOpaqueInsets(new Insets(20));

        generalInfoContent = new TextArea();

        generalInfoContent.setWrapText(true);
        generalInfoContent.setEditable(false);
        generalInfoContent.setMinHeight(500);
        generalInfoContent.setStyle(
                "-fx-background-color: transparent ;" +
                        "-fx-background-insets: 0px ;");
        VBox innerHelpBox = new VBox(generalInfoHeading, generalInfoContent, deleteMarkerButton);
        innerHelpBox.setStyle("-fx-padding: 15px;");
        VBox result = new VBox(closeHelpButton, innerHelpBox);
        result.setMaxWidth(400);

        return result;
    }

    public MapPane(final @NotNull Stage stage,
                   final @NotNull ChangebleStorage<JournalRecord> journalRecordChangebleStorage,
                   final @NotNull ChangebleStorage<WeatherRecord> weatherRecordChangebleStorage,
                   final @Nullable String cacheDirectory) {
        mapViewerElemets = new MapViewerElemets(cacheDirectory);

        // Create map area control panel.

        CheckBox showImagesCheckBox = new CheckBox("Show images layer");
        VBox mapSettingsPaneBox = new VBox();
        mapSettingsPaneBox.setSpacing(5.0);
        Button loadFromTableButton = new Button("Reload data from table");
        mapSettingsPaneBox.getChildren().addAll(
                mapViewerElemets.getMapViewControls(),
                showImagesCheckBox,
                loadFromTableButton);

        loadFromTableButton.setOnAction(e -> {
            mapPane.setRight(null);

            mapViewerElemets.clearOldData();
            displayedData = RecordsProcesser.processRecords(
                    journalRecordChangebleStorage.getItems(),
                    weatherRecordChangebleStorage.getItems());
            mapViewerElemets.loadMapData(displayedData);
        });

        // Create image viewer elements

        ImageViewerElements imageViewerElements = new ImageViewerElements();
        imageViewerElements.visibilityProperty().bind(
                showImagesCheckBox.selectedProperty()
                        .or(imageViewerElements.getControlsPane().expandedProperty()));
        // Setting up left pane.

        Accordion leftPanel = new Accordion();
        leftPanel.setMinWidth(400);
        TitledPane mapSettings = new TitledPane("Content settings", mapSettingsPaneBox);
        TitledPane contentSettings = new TitledPane("Map settings", createMapSettings());
        leftPanel.getPanes().addAll(mapSettings, contentSettings, imageViewerElements.getControlsPane());
        leftPanel.setExpandedPane(mapSettings);

        // Create info panel

        Node infoBox = createInfoPanel();
        stage.getScene().addEventHandler(KeyEvent.KEY_PRESSED, t -> {
            if (t.getCode()== KeyCode.ESCAPE) {
                if (mapPane.getRight() != null) {
                    mapPane.setRight(null);
                }
            }
        });

        mapViewerElemets.getMapView().addEventHandler(MarkerEvent.MARKER_CLICKED, event -> {
            // TODO: validate that marker has such data
            MarkerData markerData = mapViewerElemets.getMarkerData(event.getMarker().getId());
            generalInfoContent.setText(generateGeneralReport(markerData));
            deleteMarkerButton.setOnAction(e -> {
                ShipData ship = displayedData.getShip(markerData.ship);
                mapViewerElemets.clearShipData(markerData.ship);
                displayedData.remove(markerData);
                journalRecordChangebleStorage.remove(markerData.parent);
                if (displayedData.getShip(markerData.ship) != null) {
                    mapViewerElemets.loadShipData(ship);
                }
            });
            mapPane.setRight(infoBox);
        });

        // Collect all elements

        StackPane mapViewStack = new StackPane();
        mapViewStack.getChildren().addAll(mapViewerElemets.getMapView(), imageViewerElements.getMainPane());
        mapPane.setCenter(mapViewStack);
        mapPane.setBottom(createStatusBar());
        mapPane.setLeft(leftPanel);
    }

    @NotNull
    public Node getMapPane() {
        return mapPane;
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


