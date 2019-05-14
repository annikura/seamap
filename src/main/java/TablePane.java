import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TablePane {
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
    private HBox tableEditButtons = new HBox(addButton, deleteButton);
    private VBox tableContentTitleBox = new VBox(generateNewRowForm(t -> {}), tableEditButtons);

    public TablePane() {

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

        tableContentTitle.setText("Table content");
        tableContentTitle.setContent(tableContentTitleBox);

        tableLeftPanel.setMinWidth(400);
        tableLeftPanel.getPanes().add(tableContentTitle);
        tableLeftPanel.setExpandedPane(tableContentTitle);

        tableEditButtons.setSpacing(20);
        tableContentTitleBox.setSpacing(20);

        addButton.setOnAction(a -> {
            JournalRecord record = new JournalRecord();
            record.date = new Date();
            record.ship = "";
            record.lat = 0.0;
            record.lng = 0.0;
            record.comment = "";

            table.getItems().add(record);
        });

        deleteButton.setOnAction(e -> {
            JournalRecord selectedItem = table.getSelectionModel().getSelectedItem();
            table.getItems().remove(selectedItem);
        });
    }

    public Node getTablePane() {
        return tableBorderPane;
    }

    private Node generateNewRowForm(@NotNull Consumer<Supplier<JournalRecord>> newRowSupplierConsumer) {
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
        TextField shipField = new TextField();

        TextField lat1Field = new TextField();
        lat1Field.setMaxWidth(50);
        TextField lat2Field = new TextField();
        lat2Field.setMaxWidth(50);
        ComboBox latDirs = new ComboBox<String>();
        latDirs.setMinWidth(70);
        latDirs.getItems().addAll("N", "S");
        latDirs.setValue("N");

        HBox latFields = new HBox(lat1Field, latDegLabel, lat2Field, latMinLabel, latDirs);


        TextField lng1Field = new TextField();
        lng1Field.setMaxWidth(50);
        TextField lng2Field = new TextField();
        lng2Field.setMaxWidth(50);
        ComboBox lngDirs = new ComboBox<String>();
        lngDirs.setMinWidth(70);
        lngDirs.getItems().addAll("W", "E");
        lngDirs.setValue("W");
        HBox lngFields = new HBox(lng1Field, lngDegLabel, lng2Field, lngMinLabel, lngDirs);

        TextField mqkField = new TextField();
        TextArea commentText = new TextArea();
        commentText.setMaxWidth(280);

        VBox fields = new VBox(dateField, shipField, latFields, lngFields, mqkField, commentText);
        fields.setSpacing(4.0);

        newRowSupplierConsumer.accept(() -> {
            return null;
        });

        HBox result = new HBox(labels, fields);
        result.setSpacing(20.0);
        return result;
    }
}
