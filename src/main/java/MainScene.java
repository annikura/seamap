import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

public class MainScene {
    final private TabPane mainPane = new TabPane();
    final private Tab journalTap = new Tab("Journal");
    final private Tab weatherTap = new Tab("Weather");
    final private Tab mapTab = new Tab("Map");

    final private JournalPane journalPane = new JournalPane();
    final private WeatherPane weatherPane = new WeatherPane();
    final private MapPane mapPane;

    public MainScene(final @NotNull Stage stage) {
        mainPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        journalTap.setContent(journalPane.getTablePane());
        weatherTap.setContent(weatherPane.getTablePane());
        mapPane = new MapPane(stage, journalPane, weatherPane);

        mainPane.getTabs().addAll(mapTab, journalTap, weatherTap);
        mapTab.setContent(mapPane.getMapPane());
    }

    public Node getMainPane() {
        return mainPane;
    }
}