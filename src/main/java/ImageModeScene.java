import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

public class ImageModeScene {
    private StackPane centralStack = new StackPane();
    private ImageView mapImage;
    private BorderPane mainPane = new BorderPane();
    private Accordion leftPane = new Accordion();
    private VBox imageConrolsPane = new VBox();
    private TitledPane imageControls = new TitledPane("Image controls", imageConrolsPane);
    private Button uploadNewImageButton = new Button("Upload new image");

    private Button backButton = new Button("Back");

    private ScrollPane scrollPane = new ScrollPane();
    private ArrayList<ImageView> images = new ArrayList<>();

    private AnchorPane anchorPane = new AnchorPane();

    public ImageModeScene(final @NotNull Stage stage, final @NotNull Scene backScene) {
        mapImage = new ImageView();
        anchorPane.getChildren().add(mapImage);
        mapImage.setY(25);

        scrollPane.setContent(anchorPane);

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        leftPane.setMinWidth(400);
        leftPane.setMaxWidth(400);
        leftPane.setExpandedPane(imageControls);
        backButton.setOnMouseClicked(mouseEvent -> {
            stage.setScene(backScene);
            stage.setFullScreen(true);
        });
        uploadNewImageButton.setOnMouseClicked(mouseEvent -> {
            FileChooser imageChooser = new FileChooser();
            imageChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("All Images", "*.*"),
                    new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                    new FileChooser.ExtensionFilter("PNG", "*.png"));
            File picture = imageChooser.showOpenDialog(stage);

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
            Label turnValue = new Label("     0");
                turnSlider.valueProperty().addListener((observableValue, oldVal, newVal) -> {
                newImage.setRotate(newVal.doubleValue());
                turnValue.setText(String.format("     %.2f", newVal.doubleValue()));
            });
            turnSlider.setBlockIncrement(0.5);
            HBox turnBox = new HBox(turnLabel, turnSlider, turnValue);

            Label sizeLabel = new Label("Size: ");
            Label wLabel = new Label("W");
            Label hLabel = new Label("H");
            TextField wField = new TextField(String.valueOf(newImage.getBoundsInParent().getWidth()));
            TextField hField = new TextField(String.valueOf(newImage.getBoundsInParent().getHeight()));
            wField.setOnAction(actionEvent -> {
                newImage.setFitWidth(Double.valueOf(wField.getText()));
            });
            hField.setOnAction(actionEvent -> {
                newImage.setFitHeight(Double.valueOf(hField.getText()));
            });
            wField.setMaxWidth(70);
            hField.setMaxWidth(70);
            newImage.fitWidthProperty().addListener((observableValue, oldVal, newVal) ->
                    wField.setText(String.valueOf(newVal.doubleValue())));
            newImage.fitHeightProperty().addListener((observableValue, oldVal, newVal) ->
                    hField.setText(String.valueOf(newVal.doubleValue())));
            HBox sizeBox = new HBox(sizeLabel, wLabel, wField, hLabel, hField);
            sizeBox.setSpacing(5);

            Button setToDefault = new Button("To (0, 0)");
            setToDefault.setOnMouseClicked(mouseEvent1 -> {
                newImage.setX(0);
                newImage.setY(0);
            });
            CheckBox preserveRatioCheckBox = new CheckBox("Preserve ratio");
            preserveRatioCheckBox.selectedProperty().bindBidirectional(newImage.preserveRatioProperty());
            VBox imageSliders = new VBox(imageLabel, opacityBox, turnBox, sizeBox, preserveRatioCheckBox);
            HBox imageControlsBox = new HBox(imageSliders, setToDefault);
            imageControlsBox.setSpacing(10);
            imageControlsBox.setStyle("-fx-padding: 15px;");

            imageControlsBox.setAlignment(Pos.CENTER);
            imageConrolsPane.getChildren().add(imageControlsBox);
            images.add(newImage);

            anchorPane.getChildren().add(newImage);
        });

        imageConrolsPane.setSpacing(10);
        imageConrolsPane.getChildren().addAll(backButton, uploadNewImageButton);

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
