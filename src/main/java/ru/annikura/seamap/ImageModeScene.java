package ru.annikura.seamap;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.annikura.seamap.panes.DynamicImageView;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ImageModeScene {
    /**
     * Anchor pane and scroll pane are representing the area where all images are placed.
     * The main purpose is to bind images to the anchor pane so that they would not shift if the window is resized.
     * As anchor pane tends to resize itself when it can't fit the content, it is nested into the scroll pane
     * with scroll disabled which allows anchor pane to resize without visible side-effects.
     */
    private AnchorPane anchorPane = new AnchorPane();
    private ScrollPane scrollPane = new ScrollPane(anchorPane);

    private VBox imageControlsList = new VBox();
    private VBox imageConrolsPane = new VBox();
    private TitledPane imageControls = new TitledPane("Image controls", imageConrolsPane);

    private ArrayList<DynamicImageView> images = new ArrayList<>();

    private BooleanProperty imagesVisibility = new SimpleBooleanProperty(true);

    private void setupBackground() {
        anchorPane.setOnScroll(Event::consume);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    private Button createSaveButton() {
        Button saveButton = new Button("Save");

        // TODO: fix save
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
                    // TODO: show alert
                }
            }
        });
        return saveButton;
    }

    @Nullable
    private File openPicture(final @NotNull Window window) {
        FileChooser imageChooser = new FileChooser();
        imageChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png"));
        return imageChooser.showOpenDialog(window);
    }

    @NotNull
    private Button createImageUploadButton() {
        Button uploadNewImageButton = new Button("Upload new image");
        uploadNewImageButton.setOnMouseClicked(mouseEvent -> {
            File picture = openPicture(scrollPane.getScene().getWindow());
            if (picture != null) {
                DynamicImageView newImage = new DynamicImageView(picture);

                newImage.getImageView().visibleProperty().bind(
                        newImage.getVisibilityProperty()
                                .and(imagesVisibility)
                                .and(newImage.getOpacityProperty().greaterThan(0.01)));

                Image crossImage = new Image(getClass().getResourceAsStream("/cross.png"));
                ImageView crossImageView = new ImageView(crossImage);
                Button closeHelpButton = new Button();

                closeHelpButton.setGraphic(crossImageView);
                closeHelpButton.getStylesheets().add("/cross_button.css");

                HBox crossButtonBox = new HBox(closeHelpButton);
                crossButtonBox.setAlignment(Pos.TOP_RIGHT);

                HBox closableImangeControlsBox = new HBox(newImage.getControlsPanel(), crossButtonBox);
                closableImangeControlsBox.setAlignment(Pos.TOP_LEFT);

                closeHelpButton.setOnAction(me -> {
                    imageConrolsPane.getChildren().remove(closableImangeControlsBox);
                    images.remove(newImage);
                    anchorPane.getChildren().remove(newImage.getImageView());
                });

                imageControlsList.getChildren().add(closableImangeControlsBox);
                images.add(newImage);

                anchorPane.getChildren().add(newImage.getImageView());
            }
        });
        return uploadNewImageButton;
    }

    private Node createButtonsPane() {
        HBox buttonsPane = new HBox();
        buttonsPane.getChildren().addAll(
                createImageUploadButton(),
                createSaveButton());
        buttonsPane.setSpacing(10);
        return buttonsPane;
    }

    public ImageModeScene() {
        setupBackground();

        ScrollPane scrollableImageControls = new ScrollPane(imageControlsList);
        scrollableImageControls.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        imageConrolsPane.setSpacing(10);
        imageConrolsPane.getChildren().addAll(createButtonsPane(), scrollableImageControls);
        imageConrolsPane.setAlignment(Pos.TOP_LEFT);

        imageControls.expandedProperty().addListener(
                ((observable, oldValue, newValue) -> scrollPane.setMouseTransparent(!imageControls.isExpanded())));
    }

    public Node getMainPane() {
        return scrollPane;
    }

    public TitledPane getControlsPane() {
        return imageControls;
    }

    public BooleanProperty visibilityProperty() {
        return imagesVisibility;
    }
}
