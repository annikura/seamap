package ru.annikura.seamap.panes.map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class DynamicImageElements {
    private final DraggableImageView image;
    private final Node controlsPanel;

    private BooleanProperty visibilityProperty;
    private DoubleProperty  opacityProperty;
    private DoubleProperty  turnProperty;
    private StringProperty  widthProperty;
    private StringProperty  heightProperty;


    private VBox createLabelsColumn() {
        Label opacityLabel = new Label("Opacity:");
        Label turnLabel = new Label("Rotation:");
        Label sizeLabel = new Label("Size:");

        VBox labelsColumn = new VBox(opacityLabel, turnLabel, sizeLabel);
        labelsColumn.setSpacing(10);
        return labelsColumn;
    }

    private Node createSizeControls() {
        Label wLabel = new Label("W");
        Label hLabel = new Label("H");

        TextField wField = new TextField(String.valueOf(image.getBoundsInParent().getWidth()));
        TextField hField = new TextField(String.valueOf(image.getBoundsInParent().getHeight()));
        wField.setOnAction(actionEvent -> {
            try {
                image.setFitWidth(Double.valueOf(wField.getText()));
            } catch (NumberFormatException ignored) { }
        });
        hField.setOnAction(actionEvent -> {
            try {
                image.setFitHeight(Double.valueOf(hField.getText()));
            } catch (NumberFormatException ignored) { }
        });
        image.fitHeightProperty().addListener(((observable, oldValue, newValue) -> heightProperty.setValue(newValue.toString())));
        image.fitWidthProperty().addListener(((observable, oldValue, newValue) -> widthProperty.setValue(newValue.toString())));

        widthProperty = wField.textProperty();
        heightProperty = hField.textProperty();

        wField.setMaxWidth(70);
        hField.setMaxWidth(70);

        HBox sizeBox = new HBox(wLabel, wField, hLabel, hField);
        sizeBox.setSpacing(5);
        return sizeBox;
    }

    private Node createDoubleValueSliderControl(
            final @NotNull DoubleProperty property,
            final @NotNull Label label,
            final @NotNull Slider slider) {
        slider.valueProperty().addListener((observableValue, oldVal, newVal) -> {
            property.setValue(newVal.doubleValue());
            label.setText(String.format("%.2f", newVal.doubleValue()));
        });
        label.setMinWidth(40);
        HBox sliderControl = new HBox(slider, label);
        sliderControl.setSpacing(5);
        return sliderControl;
    }

    private VBox createControlsColumn() {
        Label opacityValue = new Label("1");
        Label turnValue = new Label("0");

        Slider opacitySlider = new Slider(0, 1, 1);
        Slider turnSlider = new Slider(0, 360, 0);

        opacityProperty = opacitySlider.valueProperty();
        turnProperty = turnSlider.valueProperty();

        opacitySlider.setBlockIncrement(0.1);
        turnSlider.setBlockIncrement(0.5);

        VBox result = new VBox(
                createDoubleValueSliderControl(image.opacityProperty(), opacityValue, opacitySlider),
                createDoubleValueSliderControl(image.rotateProperty(), turnValue, turnSlider),
                createSizeControls());
        result.setSpacing(10);
        return result;
    }

    private Node createControlsPanel(final @NotNull String name) {
        Label imageLabel = new Label(name);
        imageLabel.setTooltip(new Tooltip(name));
        imageLabel.setMaxWidth(300);
        imageLabel.setMinWidth(300);

        CheckBox visibilityCheckBox = new CheckBox("Set visible");
        visibilityCheckBox.setSelected(true);
        visibilityProperty = visibilityCheckBox.selectedProperty();

        HBox settingsBox = new HBox(createLabelsColumn(), createControlsColumn());
        settingsBox.setSpacing(10);

        VBox controlsBox = new VBox(imageLabel, settingsBox, visibilityCheckBox);
        controlsBox.setSpacing(10);
        controlsBox.setStyle("-fx-padding: 10px;");

        // CheckBox preserveRatioCheckBox = new CheckBox("Preserve ratio");
        // preserveRatioCheckBox.selectedProperty().bind(newImage.preserveRatioProperty());
        return controlsBox;
    }

    public DynamicImageElements(final @NotNull File imageFile) {
        image = new DraggableImageView(new Image(imageFile.toURI().toString()));
        image.setOnScroll(scrollEvent -> {
            double sizeCoefficient = 0.001;
            double sizeChange = Double.max(0.0, 1.0 + (scrollEvent.getDeltaY() + scrollEvent.getDeltaX()) * sizeCoefficient);
            image.fitWidthProperty().setValue(image.getFitWidth() * sizeChange);
            image.fitHeightProperty().setValue(image.getFitHeight() * sizeChange);
            scrollEvent.consume();
        });

        controlsPanel = createControlsPanel(imageFile.getName());
    }

    @NotNull
    public ImageView getImageView() {
        return image;
    }

    @NotNull
    public Node getControlsPanel() {
        return controlsPanel;
    }

    @NotNull
    public BooleanProperty getVisibilityProperty() {
        return visibilityProperty;
    }

    @NotNull
    public DoubleProperty getOpacityProperty() {
        return opacityProperty;
    }

    @NotNull
    public DoubleProperty getTurnProperty() {
        return turnProperty;
    }

    @NotNull
    public StringProperty getWidthProperty() {
        return widthProperty;
    }

    @NotNull
    public StringProperty getHeightProperty() {
        return heightProperty;
    }
}
