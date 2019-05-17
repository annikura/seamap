import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WeatherPane {
    private TableView<WeatherRecord> table = new TableView<>();
    private TableColumn dateColumn = new TableColumn("Date");
    private TableColumn sourceColumn = new TableColumn("Source");
    private TableColumn windDirectionColumn = new TableColumn("Wind direction");
    private TableColumn windStrengthColumn = new TableColumn("Wind strength");
    private TableColumn visibilityRangeColumn = new TableColumn("Visibility range");
    private Accordion tableLeftPanel = new Accordion();
    private TitledPane tableContentTitle = new TitledPane();
    private BorderPane tableBorderPane = new BorderPane();

    private Button addButton = new Button("Add row");
    private Button deleteButton = new Button("Delete selected row");
    private Button clearAllButton = new Button("Clear all");

    private Button loadCSVButton = new Button("Add from CSV");
    private Button saveCSVButton = new Button("Save as CSV");

    private TextArea errorMessage = new TextArea();
    private HBox tableEditButtons = new HBox(addButton, deleteButton, clearAllButton);
    private HBox tableLoaderButtons = new HBox(loadCSVButton, saveCSVButton);
    private VBox tableContentTitleBox;


    public WeatherPane() {

        table.getColumns().addAll(dateColumn, sourceColumn, windDirectionColumn, windStrengthColumn, visibilityRangeColumn);
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        dateColumn.setMinWidth(200);
        sourceColumn.setMinWidth(300);
        windDirectionColumn.setMinWidth(150);
        windStrengthColumn.setMinWidth(150);
        visibilityRangeColumn.setMinWidth(150);

        tableBorderPane.setCenter(table);
        tableBorderPane.setLeft(tableLeftPanel);

        dateColumn.setCellValueFactory(new PropertyValueFactory<WeatherRecord, String>("date"));
        sourceColumn.setCellValueFactory(new PropertyValueFactory<WeatherRecord, String>("source"));
        windDirectionColumn.setCellValueFactory(new PropertyValueFactory<WeatherRecord, String>("windDirection"));
        windStrengthColumn.setCellValueFactory(new PropertyValueFactory<WeatherRecord, Double>("windStrength"));
        visibilityRangeColumn.setCellValueFactory(new PropertyValueFactory<WeatherRecord, Double>("visibilityRange"));

        tableContentTitleBox = new VBox(generateNewRowForm(t -> {
            addButton.setOnAction(a -> {
                ErrorOr<WeatherRecord> record = t.get();
                if (record.isError()) {
                    errorMessage.setText(record.getError());
                } else {
                    errorMessage.clear();
                    table.getItems().add(record.get());
                }
            });
        }), tableEditButtons, tableLoaderButtons, errorMessage);

        tableContentTitle.setText("Weather table content");
        tableContentTitle.setContent(tableContentTitleBox);

        tableLeftPanel.setMinWidth(400);
        tableLeftPanel.getPanes().add(tableContentTitle);
        tableLeftPanel.setExpandedPane(tableContentTitle);

        tableEditButtons.setSpacing(20);
        tableLoaderButtons.setSpacing(20);
        tableContentTitleBox.setSpacing(10);

        errorMessage.setMaxWidth(380);
        errorMessage.setWrapText(true);
        errorMessage.setEditable(false);
        errorMessage.setStyle(
                "-fx-background-color: transparent ;" +
                        "-fx-background-insets: 0px ;");


        deleteButton.setOnAction(e -> {
            WeatherRecord selectedItem = table.getSelectionModel().getSelectedItem();
            table.getItems().remove(selectedItem);
        });

        clearAllButton.setOnAction(e -> table.getItems().clear());
        loadCSVButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose file to load from");
            Stage loaderStage = new Stage();
            File file = fileChooser.showOpenDialog(loaderStage);
            if (file != null) {
                ErrorOr<List<WeatherRecord>> possibleRecords = DataLoader.downloadWeatherRecords(file.getName());
                if (possibleRecords.isError()) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Loading problem");
                    alert.setHeaderText("An error happened while trying to upload " + file.getPath() + " csv file.");
                    alert.setContentText(possibleRecords.getError());

                    alert.showAndWait();
                    return;
                }
                for (WeatherRecord record : possibleRecords.get()) {
                    table.getItems().add(record);
                }
            }
        });
        saveCSVButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save to");
            Stage saverStage = new Stage();
            File file = fileChooser.showSaveDialog(saverStage);
            if (file != null) {
                ErrorOr<Void> result = DataLoader.uploadWeatherRecords(file.getPath(), table.getItems());
                if (result.isError()) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Saving problem");
                    alert.setHeaderText("Could not save file to " + file.getPath());
                    alert.setContentText(result.getError());

                    alert.showAndWait();
                }
            }
        });
    }

    public Node getTablePane() {
        return tableBorderPane;
    }

    private Node generateNewRowForm(@NotNull Consumer<Supplier<ErrorOr<WeatherRecord>>> newRowSupplierConsumer) {
        Label dateLabel = new Label("Date/time");
        Label sourceLabel = new Label("Source");
        Label windLabel = new Label("Wind strength");
        Label visibilityRangeLabel = new Label("Visibility range");
        VBox labels = new VBox(dateLabel, sourceLabel, windLabel, visibilityRangeLabel);
        labels.setSpacing(15.0);

        TextField dateField = new TextField();
        dateField.setPromptText("20.01.2001 20:01");
        TextField sourceField = new TextField();
        TextField windStrength = new TextField();

        ComboBox windDirectionsBox = new ComboBox<String>();
        windDirectionsBox.setMinWidth(70);
        windDirectionsBox.getItems().add("");
        windDirectionsBox.getItems().addAll(WeatherRecord.possibleDirections);
        windDirectionsBox.getSelectionModel().select(0);

        HBox windFields = new HBox(windStrength, windDirectionsBox);
        windFields.setSpacing(10);

        TextField visibilityRangeField = new TextField();

        VBox fields = new VBox(dateField, sourceField, windFields, visibilityRangeField);
        fields.setSpacing(4.0);

        newRowSupplierConsumer.accept(() -> {
            ErrorOr<WeatherRecord> possibleRecord = WeatherRecord.tryCreating(
                    dateField.getText(), sourceField.getText(),
                    windStrength.getText(), windDirectionsBox.getSelectionModel().getSelectedItem().toString(),
                    visibilityRangeField.getText());
            if (!possibleRecord.isError()) {
                windStrength.clear();
                visibilityRangeField.clear();
                windDirectionsBox.getSelectionModel().select(0);
            }
            return possibleRecord;
        });

        HBox result = new HBox(labels, fields);
        result.setSpacing(20.0);
        return result;
    }

    public List<WeatherRecord> getRecords() {
        return table.getItems();
    }
}
