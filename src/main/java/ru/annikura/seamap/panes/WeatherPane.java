package ru.annikura.seamap.panes;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.jetbrains.annotations.NotNull;
import ru.annikura.seamap.journal.ChangebleStorage;
import ru.annikura.seamap.journal.DataLoader;
import ru.annikura.seamap.journal.WeatherRecord;

public class WeatherPane extends TablePane<WeatherRecord> {
    private static TableView<WeatherRecord> createTable() {
        TableView<WeatherRecord> table = new TableView<>();

        TableColumn dateColumn = new TableColumn("Date");
        TableColumn sourceColumn = new TableColumn("Source");
        TableColumn windDirectionColumn = new TableColumn("Wind direction");
        TableColumn windStrengthColumn = new TableColumn("Wind windStrength");
        TableColumn visibilityRangeColumn = new TableColumn("Visibility range");

        table.getColumns().addAll(dateColumn, sourceColumn, windStrengthColumn, windDirectionColumn, visibilityRangeColumn);
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        dateColumn.setMinWidth(200);
        sourceColumn.setMinWidth(300);
        windDirectionColumn.setMinWidth(150);
        windStrengthColumn.setMinWidth(150);
        visibilityRangeColumn.setMinWidth(150);

        dateColumn.setCellValueFactory(new PropertyValueFactory<WeatherRecord, String>("date"));
        sourceColumn.setCellValueFactory(new PropertyValueFactory<WeatherRecord, String>("source"));
        windDirectionColumn.setCellValueFactory(new PropertyValueFactory<WeatherRecord, String>("windDirection"));
        windStrengthColumn.setCellValueFactory(new PropertyValueFactory<WeatherRecord, Double>("windStrength"));
        visibilityRangeColumn.setCellValueFactory(new PropertyValueFactory<WeatherRecord, Double>("visibilityRange"));

        return table;
    }

    public WeatherPane(final @NotNull ChangebleStorage<WeatherRecord> storage) {
        super(new WeatherInputForm(),
                createTable(),
                storage,
                DataLoader::downloadWeatherRecords,
                DataLoader::uploadWeatherRecords);
    }
}
