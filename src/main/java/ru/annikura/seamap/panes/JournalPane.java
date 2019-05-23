package ru.annikura.seamap.panes;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import ru.annikura.seamap.journal.DataLoader;
import ru.annikura.seamap.journal.JournalRecord;
import ru.annikura.seamap.utils.ErrorOr;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class JournalPane {
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

    private Button addButton = new Button("Add row");
    private Button deleteButton = new Button("Delete selected row");
    private Button clearAllButton = new Button("Clear all");

    private Button loadCSVButton = new Button("Add from CSV");
    private Button saveCSVButton = new Button("Save as CSV");

    private TextArea errorMessage = new TextArea();
    private HBox tableEditButtons = new HBox(addButton, deleteButton, clearAllButton);
    private HBox tableLoaderButtons = new HBox(loadCSVButton, saveCSVButton);
    private VBox tableContentTitleBox;


    public JournalPane() {

        latLngCoodrColumn.getColumns().addAll(latColumn, lngColumn);

        table.getColumns().addAll(dateColumn, shipColumn, latLngCoodrColumn, mqkColumn, commentColumn);
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        dateColumn.setMinWidth(200);
        shipColumn.setMinWidth(150);
        latColumn.setMinWidth(200);
        lngColumn.setMinWidth(200);
        mqkColumn.setMinWidth(150);
        commentColumn.setMinWidth(400);

        tableBorderPane.setCenter(table);
        tableBorderPane.setLeft(tableLeftPanel);

        dateColumn.setCellValueFactory(new PropertyValueFactory<JournalRecord, String>("date"));
        shipColumn.setCellValueFactory(new PropertyValueFactory<JournalRecord, String>("ship"));
        latColumn.setCellValueFactory(new PropertyValueFactory<JournalRecord, Double>("lat"));
        lngColumn.setCellValueFactory(new PropertyValueFactory<JournalRecord, Double>("lng"));
        mqkColumn.setCellValueFactory(new PropertyValueFactory<JournalRecord, String>("mqk"));
        commentColumn.setCellValueFactory(new PropertyValueFactory<JournalRecord, String>("comment"));

        tableContentTitleBox = new VBox(generateNewRowForm(t -> {
            addButton.setOnAction(a -> {
                ErrorOr<JournalRecord> record = t.get();
                if (record.isError()) {
                    errorMessage.setText(record.getError());
                } else {
                    errorMessage.clear();
                    table.getItems().add(record.get());
                }
            });
        }), tableEditButtons, tableLoaderButtons, errorMessage);

        tableContentTitle.setText("Table content");
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
            JournalRecord selectedItem = table.getSelectionModel().getSelectedItem();
            table.getItems().remove(selectedItem);
        });

        clearAllButton.setOnAction(e -> table.getItems().clear());
        loadCSVButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose file to load from");
            Stage loaderStage = new Stage();
            File file = fileChooser.showOpenDialog(loaderStage);
            if (file != null) {
                ErrorOr<List<JournalRecord>> possibleRecords = DataLoader.downloadJournalRecords(file.getName());
                if (possibleRecords.isError()) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Loading problem");
                    alert.setHeaderText("An error happened while trying to upload " + file.getPath() + " csv file.");
                    alert.setContentText(possibleRecords.getError());

                    alert.showAndWait();
                    return;
                }
                for (JournalRecord record : possibleRecords.get()) {
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
                ErrorOr<Void> result = DataLoader.uploadJournalRecords(file.getPath(), table.getItems());
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

    private Node generateNewRowForm(@NotNull Consumer<Supplier<ErrorOr<JournalRecord>>> newRowSupplierConsumer) {
        Label dateLabel = new Label("Date/time");
        Label shipLabel = new Label("Ship");
        Label latLabel = new Label("Latitude");
        Label lngLabel = new Label("Longtitude");
        Label mqkLabel = new Label("MQK");
        Label commentLabel = new Label("Comment");
        VBox labels = new VBox(dateLabel, shipLabel, latLabel, lngLabel, mqkLabel, commentLabel);
        labels.setSpacing(15.0);

        Label latDegLabel = new Label("°");
        Label lngDegLabel = new Label("°");
        Label latMinLabel = new Label("´");
        Label lngMinLabel = new Label("´");

        TextField dateField = new TextField();
        dateField.setPromptText("20.01.2001 20:01");
        TextField shipField = new TextField();

        TextField lat1Field = new TextField();
        lat1Field.setMaxWidth(50);
        lat1Field.setPromptText("0.0");
        TextField lat2Field = new TextField();
        lat2Field.setMaxWidth(50);
        lat2Field.setPromptText("0.0");
        ComboBox latDirs = new ComboBox<String>();
        latDirs.setMinWidth(70);
        latDirs.getItems().addAll("N", "S");
        latDirs.getSelectionModel().select(0);

        HBox latFields = new HBox(lat1Field, latDegLabel, lat2Field, latMinLabel, latDirs);


        TextField lng1Field = new TextField();
        lng1Field.setPromptText("0.0");
        lng1Field.setMaxWidth(50);
        TextField lng2Field = new TextField();
        lng2Field.setPromptText("0.0");
        lng2Field.setMaxWidth(50);
        ComboBox lngDirs = new ComboBox<String>();
        lngDirs.setMinWidth(70);
        lngDirs.getItems().addAll("W", "E");
        lngDirs.getSelectionModel().select(0);
        HBox lngFields = new HBox(lng1Field, lngDegLabel, lng2Field, lngMinLabel, lngDirs);

        TextField mqkField = new TextField();
        mqkField.setPromptText("AA1234");
        TextArea commentText = new TextArea();
        commentText.setMaxWidth(280);

        VBox fields = new VBox(dateField, shipField, latFields, lngFields, mqkField, commentText);
        fields.setSpacing(4.0);

        newRowSupplierConsumer.accept(() -> {
            ErrorOr<JournalRecord> possibleRecord = JournalRecord.tryCreating(
                    dateField.getText(), shipField.getText(),
                    lat1Field.getText(), lat2Field.getText(), latDirs.getSelectionModel().getSelectedItem().toString(),
                    lng1Field.getText(), lng2Field.getText(), lngDirs.getSelectionModel().getSelectedItem().toString(),
                    mqkField.getText(), commentText.getText());
            if (!possibleRecord.isError()) {
                lat1Field.clear();
                lat2Field.clear();
                lng1Field.clear();
                lng2Field.clear();
                mqkField.clear();
                commentText.clear();
            }
            return possibleRecord;
        });

        HBox result = new HBox(labels, fields);
        result.setSpacing(20.0);
        return result;
    }

    public List<JournalRecord> getRecords() {
        return table.getItems();
    }
}
