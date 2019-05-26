package ru.annikura.seamap.panes;

import javafx.scene.Node;
import ru.annikura.seamap.utils.ErrorOr;

public interface InputFormInterface<T> {
    ErrorOr<T> get();
    Node getForm();
}
