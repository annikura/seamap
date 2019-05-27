package ru.annikura.seamap.panes.map;

import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class DraggableImageView extends ImageView {
    private Double orgSceneX;
    private Double orgSceneY;

    public DraggableImageView(Image image) {
        super(image);
        EventHandler<MouseEvent> imageOnMouseDraggedEventHandler = t -> {
            double offsetX = t.getSceneX() - orgSceneX;
            double offsetY = t.getSceneY() - orgSceneY;

            setX(getX() + offsetX);
            setY(getY() + offsetY);

            orgSceneX = t.getSceneX();
            orgSceneY = t.getSceneY();
        };
        this.setOnMouseDragged(imageOnMouseDraggedEventHandler);
        EventHandler<MouseEvent> imageOnMousePressedEventHandler = t -> {
            orgSceneX = t.getSceneX();
            orgSceneY = t.getSceneY();
        };
        this.setOnMousePressed(imageOnMousePressedEventHandler);
    }
}
