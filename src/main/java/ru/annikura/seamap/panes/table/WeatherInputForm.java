package ru.annikura.seamap.panes.table;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ru.annikura.seamap.journal.WeatherRecord;
import ru.annikura.seamap.utils.ErrorOr;

public class WeatherInputForm implements InputFormInterface<WeatherRecord> {
    private final Node form;

    private final TextField dateField = new TextField();
    private final TextField sourceField = new TextField();
    private final TextField windStrength = new TextField();
    private final ComboBox windDirectionsBox = new ComboBox<String>();
    private final TextField visibilityRangeField = new TextField();

    private static Node createLabelsColumn() {
        Label dateLabel = new Label("Date/time");
        Label sourceLabel = new Label("Source");
        Label windLabel = new Label("Wind windStrength");
        Label visibilityRangeLabel = new Label("Visibility range");
        VBox labels = new VBox(dateLabel, sourceLabel, windLabel, visibilityRangeLabel);
        labels.setSpacing(15.0);

        return labels;
    }

    private Node createFieldsColumn() {
        dateField.setPromptText("20.01.2001 20:01");

        windDirectionsBox.setMinWidth(70);
        windDirectionsBox.getItems().add("");
        windDirectionsBox.getItems().addAll(WeatherRecord.possibleDirections);
        windDirectionsBox.getSelectionModel().select(0);

        HBox windFields = new HBox(windStrength, windDirectionsBox);
        windFields.setSpacing(10);

        VBox fields = new VBox(dateField, sourceField, windFields, visibilityRangeField);
        fields.setSpacing(4.0);

        return fields;
    }

    public WeatherInputForm() {
        HBox result = new HBox(createLabelsColumn(), createFieldsColumn());
        result.setSpacing(20.0);
        form = result;
    }

    @Override
    public ErrorOr<WeatherRecord> get() {
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
    }

    @Override
    public Node getForm() {
        return form;
    }
}
