import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.Date;

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
    private Button deleteButton = new Button("Delete row");
    private VBox tableContentTitleBox = new VBox(addButton, deleteButton);

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

        addButton.setOnMouseClicked(mouseEvent -> {
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
}
