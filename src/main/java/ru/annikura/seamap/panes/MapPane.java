package ru.annikura.seamap.panes;

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
import ru.annikura.seamap.ImageModeScene;
import ru.annikura.seamap.data.MapData;
import ru.annikura.seamap.data.MarkerData;
import ru.annikura.seamap.data.WeatherData;
import ru.annikura.seamap.journal.ChangebleStorage;
import ru.annikura.seamap.journal.JournalRecord;
import ru.annikura.seamap.journal.RecordsProcesser;
import ru.annikura.seamap.journal.WeatherRecord;

public class MapPane {
    private MapViewerElemets mapViewerElemets = new MapViewerElemets();

    private VBox mapSettingsPaneBox = new VBox();
    private TitledPane mapSettings = new TitledPane("Content settings", mapSettingsPaneBox);

    private BorderPane mapPane = new BorderPane();
    private StackPane mapViewStack = new StackPane();
    private Accordion leftPanel = new Accordion();

    private CheckBox showImagesCheckBox = new CheckBox("Show images layer");

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

    private ImageModeScene imageModeScene;


    public MapPane(final @NotNull Stage stage,
                   final @NotNull ChangebleStorage<JournalRecord> journalRecordChangebleStorage,
                   final @NotNull ChangebleStorage<WeatherRecord> weatherRecordChangebleStorage) {
        imageModeScene = new ImageModeScene();
        imageModeScene.visibilityProperty().bind(
                showImagesCheckBox.selectedProperty()
                        .or(imageModeScene.getControlsPane().expandedProperty()));

        mapViewerElemets.getMapView().addEventHandler(MarkerEvent.MARKER_CLICKED, event -> {
            // TODO: validate that marker has such data
            MarkerData markerData = mapViewerElemets.getMarkerData(event.getMarker().getId());
            generalInfoContent.setText(generateGeneralReport(markerData));
            mapPane.setRight(helpBox);
        });

        // Setting up left pane.

        leftPanel.setMinWidth(400);
        leftPanel.getPanes().addAll(mapSettings, contentSettings, imageModeScene.getControlsPane());
        leftPanel.setExpandedPane(mapSettings);

        mapSettingsPaneBox.setSpacing(5.0);
        mapSettingsPaneBox.getChildren().addAll(
                mapViewerElemets.getMapViewControls(),
                showImagesCheckBox,
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

        mapViewerElemets.getMapView().addEventHandler(MapViewEvent.MAP_POINTER_MOVED, event -> currentCoordinatesValueLabel.setText(
                String.format("%.10f, %.10f", event.getCoordinate().getLatitude(), event.getCoordinate().getLongitude())));

        statusBar.getChildren().addAll(currentCoordinatesLabel, currentCoordinatesValueLabel);

        mapViewStack.getChildren().addAll(mapViewerElemets.getMapView(), imageModeScene.getMainPane());
        mapPane.setCenter(mapViewStack);
        mapPane.setBottom(statusBar);
        mapPane.setLeft(leftPanel);

        loadFromTableButton.setOnAction(e -> {
            mapPane.setRight(null);

            mapViewerElemets.clearOldData();
            MapData displayedData = RecordsProcesser.processRecords(
                    journalRecordChangebleStorage.getItems(),
                    weatherRecordChangebleStorage.getItems());
            mapViewerElemets.loadMapData(displayedData);
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


