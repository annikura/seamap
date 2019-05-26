package ru.annikura.seamap.journal;

import javafx.beans.value.ChangeListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChangebleStorage<T> {
    private volatile Set<T> values = new HashSet<>();
    private volatile List<ChangeListener<T>> listeners = new ArrayList<>();

    public synchronized void addListener(final @NotNull ChangeListener<T> changeListener) {
        listeners.add(changeListener);
    }

    public synchronized void add(final @NotNull T record) {
        if (values.add(record)) {
            for (ChangeListener<T> listener : listeners) {
                listener.changed(null, null, record);
            }
        }
    }

    public synchronized void remove(final @NotNull T record) {
        if (values.remove(record)) {
            for (ChangeListener<T> listener : listeners) {
                listener.changed(null, record, null);
            }
        }
    }

    public synchronized void clear() {
        for (T value: values) {
            remove(value);
        }
    }

    public synchronized List<T> getItems() {
        return new ArrayList<>(values);
    }
}
