package ru.annikura.seamap.panes.table;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import ru.annikura.seamap.journal.JournalRecord;
import ru.annikura.seamap.utils.ErrorOr;

public class JournalInputForm implements InputFormInterface<JournalRecord> {
    private final Node form;

    private final TextField dateField = new TextField();
    private final TextField shipField = new TextField();
    private final TextField lat1Field = new TextField();
    private final TextField lat2Field = new TextField();

    private final TextField lng1Field = new TextField();
    private final TextField lng2Field = new TextField();
    private final ComboBox<String> latDirs = new ComboBox<>();
    private final ComboBox<String> lngDirs = new ComboBox<>();

    private final TextField mqkField = new TextField();
    private final TextArea commentText = new TextArea();

    private static Node createLabelsColumn() {
        Label dateLabel = new Label("Date/time");
        Label shipLabel = new Label("Ship");
        Label latLabel = new Label("Latitude");
        Label lngLabel = new Label("Longtitude");
        Label mqkLabel = new Label("MQK");
        Label commentLabel = new Label("Comment");
        VBox labels = new VBox(dateLabel, shipLabel, latLabel, lngLabel, mqkLabel, commentLabel);
        labels.setSpacing(15.0);
        return labels;
    }

    private static Node createCoordinatesInput(
            final @NotNull TextField degField,
            final @NotNull TextField minField,
            final @NotNull ComboBox<String> directions) {
        Label degLabel = new Label("°");
        Label minLabel = new Label("´");

        degField.setMaxWidth(50);
        degField.setPromptText("0.0");
        minField.setMaxWidth(50);
        minField.setPromptText("0.0");
        directions.setMinWidth(70);
        directions.getSelectionModel().select(0);
        return new HBox(degField, degLabel, minField, minLabel, directions);
    }


    private Node createFieldsColumn() {
        dateField.setPromptText("20.01.2001 20:01");

        latDirs.getItems().addAll("N", "S");
        lngDirs.getItems().addAll("W", "E");

        mqkField.setPromptText("AA1234");
        commentText.setMaxWidth(280);

        VBox fields = new VBox(dateField, shipField,
                createCoordinatesInput(lat1Field, lat2Field, latDirs),
                createCoordinatesInput(lng1Field, lng2Field, lngDirs),
                mqkField, commentText);
        fields.setSpacing(4.0);
        return fields;
    }

    public JournalInputForm() {
        HBox result = new HBox(createLabelsColumn(), createFieldsColumn());
        result.setSpacing(20.0);
        form = result;
    }

    @Override
    public ErrorOr<JournalRecord> get() {
        ErrorOr<JournalRecord> possibleRecord = JournalRecord.tryCreating(
                dateField.getText(), shipField.getText(),
                lat1Field.getText(), lat2Field.getText(), latDirs.getSelectionModel().getSelectedItem(),
                lng1Field.getText(), lng2Field.getText(), lngDirs.getSelectionModel().getSelectedItem(),
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
    }

    @Override
    public Node getForm() {
        return form;
    }
}
