package ru.annikura.seamap;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import ru.annikura.seamap.journal.ChangebleStorage;
import ru.annikura.seamap.journal.JournalRecord;
import ru.annikura.seamap.journal.WeatherRecord;
import ru.annikura.seamap.panes.JournalPane;
import ru.annikura.seamap.panes.MapPane;
import ru.annikura.seamap.panes.WeatherPane;

import java.nio.file.Paths;

public class MainScene {
    final private TabPane mainPane = new TabPane();
    final private Tab journalTap = new Tab("Journal");
    final private Tab weatherTap = new Tab("Weather");
    final private Tab mapTab = new Tab("Map");

    final private ChangebleStorage<JournalRecord> journalStorage = new ChangebleStorage<>();
    final private ChangebleStorage<WeatherRecord> weatherStorage = new ChangebleStorage<>();

    final private JournalPane journalPane = new JournalPane(journalStorage);
    final private WeatherPane weatherPane = new WeatherPane(weatherStorage);
    final private MapPane mapPane;

    public MainScene(final @NotNull Stage stage) {
        mainPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        journalTap.setContent(journalPane.getTablePane());
        weatherTap.setContent(weatherPane.getTablePane());
        mapPane = new MapPane(stage, journalStorage, weatherStorage,
                Paths.get(Paths.get(
                        this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParent().toString(),
                        "seamap-cache").toString());

        mainPane.getTabs().addAll(mapTab, journalTap, weatherTap);
        mapTab.setContent(mapPane.getMapPane());
    }

    public Node getMainPane() {
        return mainPane;
    }
}