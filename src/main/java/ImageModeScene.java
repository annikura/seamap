import javafx.event.EventHandler;
import javafx.scene.Node;
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

    private ArrayList<ImageView> images = new ArrayList<>();

    private AnchorPane anchorPane = new AnchorPane();

    public ImageModeScene(final @NotNull Image backgroundImage, final @NotNull Stage stage) {
        mapImage = new ImageView(backgroundImage);
        centralStack.getChildren().addAll(mapImage, anchorPane);

        leftPane.setMinWidth(400);
        leftPane.setMaxWidth(400);
        leftPane.setExpandedPane(imageControls);
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

            VBox imageControlsBox = new VBox(imageLabel, opacityBox, turnBox);
            imageControlsBox.setStyle("-fx-padding: 15px;");


            imageConrolsPane.getChildren().add(imageControlsBox);
            images.add(newImage);

            anchorPane.getChildren().add(newImage);
        });

        imageConrolsPane.getChildren().addAll(uploadNewImageButton);

        imageControls.setContent(imageConrolsPane);
        leftPane.getPanes().addAll(imageControls);

        mainPane.setCenter(centralStack);
        mainPane.setLeft(leftPane);
    }

    public Node getMainPane() {
        return mainPane;
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
