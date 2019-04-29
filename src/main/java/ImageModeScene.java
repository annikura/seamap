import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Function;

public class ImageModeScene {
    private ImageView mapImage;
    private BorderPane mainPane = new BorderPane();
    private Accordion leftPane = new Accordion();
    private HBox buttonsPane = new HBox();
    private VBox imageConrolsPane = new VBox();
    private TitledPane imageControls = new TitledPane("Image controls", imageConrolsPane);
    private Button uploadNewImageButton = new Button("Upload new image");
    private Button saveButton = new Button("Save");

    private Button backButton = new Button("Back");

    private ScrollPane scrollPane = new ScrollPane();
    private ArrayList<ImageView> images = new ArrayList<>();

    private AnchorPane anchorPane = new AnchorPane();

    public ImageModeScene(final @NotNull Function<Void, Void> onClose) {
        mapImage = new ImageView();
        anchorPane.getChildren().add(mapImage);

        mapImage.setY(25);

        scrollPane.setContent(anchorPane);
        mapImage.setOnScroll(Event::consume);

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        leftPane.setMinWidth(400);
        leftPane.setMaxWidth(400);
        leftPane.setExpandedPane(imageControls);
        backButton.setOnMouseClicked(mouseEvent -> {
            onClose.apply(null);
            ((Stage) mainPane.getScene().getWindow()).close();
        });
        saveButton.setOnMouseClicked(mouseEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save map");
            Stage saverStage = new Stage();
            File file = fileChooser.showSaveDialog(saverStage);
            if (file != null) {
                WritableImage wim = scrollPane.snapshot(new SnapshotParameters(), null);
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(wim,
                            null), "png", file);
                } catch (IOException ex) {
                    System.out.println("Failed to save image");

                }
            }
        });
        uploadNewImageButton.setOnMouseClicked(mouseEvent -> {
            FileChooser imageChooser = new FileChooser();
            imageChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("All Images", "*.*"),
                    new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                    new FileChooser.ExtensionFilter("PNG", "*.png"));
            File picture = imageChooser.showOpenDialog(mainPane.getScene().getWindow());

            ImageView newImage = new DraggableImageView(new Image(picture.toURI().toString()));

            Label imageLabel = new Label(picture.getName());

            Label opacityLabel = new Label("Opacity:  ");
            Slider opacitySlider = new Slider(0, 1, 1);
            opacitySlider.setBlockIncrement(0.1);
            Label opacityValue = new Label("     1");
            opacitySlider.valueProperty().addListener((observableValue, oldVal, newVal) -> {
                newImage.setVisible(newVal.doubleValue() >= 0.01);
                newImage.setOpacity(newVal.doubleValue());
                opacityValue.setText(String.format("     %.2f", newVal.doubleValue()));
            });
            HBox opacityBox = new HBox(opacityLabel, opacitySlider, opacityValue);

            Label turnLabel = new Label("Rotation: ");
            Slider turnSlider = new Slider(0, 360, 0);
            Label turnValue = new Label("     000.00");
            turnSlider.valueProperty().addListener((observableValue, oldVal, newVal) -> {
                newImage.setRotate(newVal.doubleValue());
                String label = String.format("%.2f", newVal.doubleValue());
                while (label.length() < 6) label = "0" + label;
                turnValue.setText("     " + label);
            });
            turnSlider.setBlockIncrement(0.5);
            HBox turnBox = new HBox(turnLabel, turnSlider, turnValue);

            Label sizeLabel = new Label("Size: ");
            Label wLabel = new Label("W");
            Label hLabel = new Label("H");
            TextField wField = new TextField(String.valueOf(newImage.getBoundsInParent().getWidth()));
            TextField hField = new TextField(String.valueOf(newImage.getBoundsInParent().getHeight()));
            wField.setOnAction(actionEvent -> newImage.setFitWidth(Double.valueOf(wField.getText())));
            hField.setOnAction(actionEvent -> newImage.setFitHeight(Double.valueOf(hField.getText())));
            wField.setMaxWidth(70);
            hField.setMaxWidth(70);
            newImage.fitWidthProperty().addListener((observableValue, oldVal, newVal) ->
                    wField.setText(String.valueOf(newVal.doubleValue())));
            newImage.fitHeightProperty().addListener((observableValue, oldVal, newVal) ->
                    hField.setText(String.valueOf(newVal.doubleValue())));
            newImage.setOnScroll(scrollEvent -> {
                double sizeCoefficient = 0.0005;
                double sizeChange = Double.max(0.0,1.0 + (scrollEvent.getDeltaY() + scrollEvent.getDeltaX()) * sizeCoefficient);
                double newSizeX = Double.valueOf(wField.getText()) * sizeChange;
                double newSizeY = Double.valueOf(hField.getText()) * sizeChange;
                newImage.fitWidthProperty().setValue(newSizeX);
                newImage.fitHeightProperty().setValue(newSizeY);
                scrollEvent.consume();
            });
            HBox sizeBox = new HBox(sizeLabel, wLabel, wField, hLabel, hField);
            sizeBox.setSpacing(5);

            Button setToDefault = new Button("To (0, 0)");
            setToDefault.setOnMouseClicked(mouseEvent1 -> {
                newImage.setX(0);
                newImage.setY(0);
            });


            Image crossImage = new Image(getClass().getResourceAsStream("cross.png"));
            ImageView crossImageView = new ImageView(crossImage);
            Button closeHelpButton = new Button();
            HBox crossButtonBox = new HBox();

            closeHelpButton.setGraphic(crossImageView);
            closeHelpButton.getStylesheets().add("cross_button.css");

            crossButtonBox.setAlignment(Pos.CENTER_RIGHT);
            crossButtonBox.getChildren().add(closeHelpButton);

            CheckBox preserveRatioCheckBox = new CheckBox("Preserve ratio");
            preserveRatioCheckBox.selectedProperty().bindBidirectional(newImage.preserveRatioProperty());
            VBox imageSliders = new VBox(imageLabel, opacityBox, turnBox, sizeBox, preserveRatioCheckBox);
            VBox imageButtonsBox = new VBox(crossButtonBox, setToDefault);
            HBox imageControlsBox = new HBox(imageSliders, imageButtonsBox);
            imageControlsBox.setSpacing(10);
            imageControlsBox.setStyle("-fx-padding: 10px;");


            closeHelpButton.setOnMouseClicked(me -> {
                imageConrolsPane.getChildren().remove(imageControlsBox);
                images.remove(newImage);
                anchorPane.getChildren().remove(newImage);
            });

            imageControlsBox.setAlignment(Pos.TOP_LEFT);
            imageConrolsPane.getChildren().add(imageControlsBox);
            images.add(newImage);

            anchorPane.getChildren().add(newImage);
        });

        buttonsPane.getChildren().addAll(backButton, uploadNewImageButton, saveButton);
        buttonsPane.setSpacing(10);

        imageConrolsPane.setSpacing(10);
        imageConrolsPane.getChildren().addAll(buttonsPane);
        imageConrolsPane.setAlignment(Pos.TOP_LEFT);

        imageControls.setContent(imageConrolsPane);
        leftPane.getPanes().addAll(imageControls);

        mainPane.setCenter(scrollPane);
        mainPane.setLeft(leftPane);
    }

    public Node getMainPane() {
        return mainPane;
    }

    public void setBackgroundImage(@NotNull Image backgroundImage) {
        mapImage.setImage(backgroundImage);
    }

    private class DraggableImageView extends ImageView {
        private Double orgSceneX;
        private Double orgSceneY;

        public DraggableImageView(Image image) {
            super(image);
            this.setOnMouseDragged(imageOnMouseDraggedEventHandler);
            this.setOnMousePressed(imageOnMousePressedEventHandler);
        }

        private EventHandler<MouseEvent> imageOnMousePressedEventHandler =
                new EventHandler<>() {
                    @Override
                    public void handle(MouseEvent t) {
                        orgSceneX = t.getSceneX();
                        orgSceneY = t.getSceneY();
                    }
                };

        private EventHandler<MouseEvent> imageOnMouseDraggedEventHandler =
                new EventHandler<>() {
                    @Override
                    public void handle(MouseEvent t) {
                        double offsetX = t.getSceneX() - orgSceneX;
                        double offsetY = t.getSceneY() - orgSceneY;

                        setX(getX() + offsetX);
                        setY(getY() + offsetY);

                        orgSceneX = t.getSceneX();
                        orgSceneY = t.getSceneY();
                    }
                };
    }
}
