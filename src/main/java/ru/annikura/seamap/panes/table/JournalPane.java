package ru.annikura.seamap.panes.table;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.jetbrains.annotations.NotNull;
import ru.annikura.seamap.journal.ChangebleStorage;
import ru.annikura.seamap.journal.DataLoader;
import ru.annikura.seamap.journal.JournalRecord;

public class JournalPane extends TablePane<JournalRecord> {
    private static TableView<JournalRecord> createTable() {
        TableView<JournalRecord> table = new TableView<>();
        TableColumn dateColumn = new TableColumn("Date");
        TableColumn shipColumn = new TableColumn("Ship");
        TableColumn latLngCoodrColumn = new TableColumn("Lat/Lng coordinate");
        TableColumn latColumn = new TableColumn("Latitude");
        TableColumn lngColumn = new TableColumn("Longtitude");
        TableColumn mqkColumn = new TableColumn("MQK");
        TableColumn commentColumn = new TableColumn("Comment");

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

        dateColumn.setCellValueFactory(new PropertyValueFactory<JournalRecord, String>("date"));
        shipColumn.setCellValueFactory(new PropertyValueFactory<JournalRecord, String>("ship"));
        latColumn.setCellValueFactory(new PropertyValueFactory<JournalRecord, Double>("lat"));
        lngColumn.setCellValueFactory(new PropertyValueFactory<JournalRecord, Double>("lng"));
        mqkColumn.setCellValueFactory(new PropertyValueFactory<JournalRecord, String>("mqk"));
        commentColumn.setCellValueFactory(new PropertyValueFactory<JournalRecord, String>("comment"));

        return table;
    }

    public JournalPane(final @NotNull ChangebleStorage<JournalRecord> storage) {
        super(new JournalInputForm(),
                createTable(),
                storage,
                DataLoader::downloadJournalRecords,
                DataLoader::uploadJournalRecords);
    }
}
