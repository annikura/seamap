package ru.annikura.seamap.panes;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import ru.annikura.seamap.journal.ChangebleStorage;
import ru.annikura.seamap.utils.ErrorOr;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class TablePane<T> {
    protected final InputFormInterface<T> form;
    protected final TableView<T> table;
    protected final BorderPane mainPane;


    public TablePane(final @NotNull InputFormInterface<T> form,
                     final @NotNull TableView<T> table,
                     final @NotNull ChangebleStorage<T> storage,
                     final @NotNull Function<String, ErrorOr<List<T>>> reader,
                     final @NotNull BiFunction<String, List<T>, ErrorOr<Void>> writer) {
        this.form = form;
        this.table = table;

        TextArea errorMessage = new TextArea();

        BorderPane tableBorderPane = new BorderPane();
        tableBorderPane.setCenter(table);
        Accordion tableLeftPanel = new Accordion();
        tableBorderPane.setLeft(tableLeftPanel);

        Button addButton = new Button("Add row");
        Button deleteButton = new Button("Delete selected row");
        Button clearAllButton = new Button("Clear all");
        HBox tableEditButtons = new HBox(addButton, deleteButton, clearAllButton);

        Button loadCSVButton = new Button("Add from CSV");
        Button saveCSVButton = new Button("Save as CSV");
        HBox tableLoaderButtons = new HBox(loadCSVButton, saveCSVButton);

        VBox tableContentTitleBox = new VBox(form.getForm(), tableEditButtons, tableLoaderButtons, errorMessage);

        TitledPane tableContentTitle = new TitledPane();
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

        // Setup button actions

        deleteButton.setOnAction(e -> {
            T selectedItem = table.getSelectionModel().getSelectedItem();
            storage.remove(selectedItem);
        });

        addButton.setOnAction(a -> {
            ErrorOr<T> record = form.get();
            if (record.isError()) {
                errorMessage.setText(record.getError());
            } else {
                errorMessage.clear();
                storage.add(record.get());
                //table.getItems().add(record.get());
            }
        });

        clearAllButton.setOnAction(e -> storage.clear());

        loadCSVButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose file to load from");
            Stage loaderStage = new Stage();
            File file = fileChooser.showOpenDialog(loaderStage);
            if (file != null) {
                ErrorOr<? extends List<? extends T>> possibleRecords = reader.apply(file.getAbsolutePath());
                if (possibleRecords.isError()) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Loading problem");
                    alert.setHeaderText("An error happened while trying to upload " + file.getAbsolutePath() + " csv file.");
                    alert.setContentText(possibleRecords.getError());

                    alert.showAndWait();
                    return;
                }
                for (T record : possibleRecords.get()) {
                    storage.add(record);
                }
            }
        });

        saveCSVButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save to");
            Stage saverStage = new Stage();
            File file = fileChooser.showSaveDialog(saverStage);
            if (file != null) {
                ErrorOr<Void> result = writer.apply(file.getAbsolutePath(), storage.getItems());
                if (result.isError()) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Saving problem");
                    alert.setHeaderText("Could not save file to " + file.getAbsolutePath());
                    alert.setContentText(result.getError());

                    alert.showAndWait();
                }
            }
        });

        storage.addListener(((observable, oldValue, newValue) -> {
            if (oldValue != null && newValue == null) {
                // delete operation
                table.getItems().remove(oldValue);
                return;
            }
            if (oldValue == null && newValue != null) {
                // add operation
                table.getItems().add(newValue);
                return;
            }
            throw new NotImplementedException();
        }));

        mainPane = tableBorderPane;
    }

    public InputFormInterface<T> getForm() {
        return form;
    }

    public TableView<T> getTable() {
        return table;
    }

    public Node getTablePane() {
        return mainPane;
    }
}
