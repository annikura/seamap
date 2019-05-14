import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

public class MainScene {
    final private TabPane mainPane = new TabPane();
    final private Tab mapTab = new Tab("Map");
    final private Tab tableTap = new Tab("Table");

    final private TablePane tablePane = new TablePane();
    final private MapPane mapPane;

    public MainScene(final @NotNull Stage stage) {

        tableTap.setContent(tablePane.getTablePane());
        mapPane = new MapPane(stage);

        mainPane.getTabs().addAll(mapTab, tableTap);
        mapTab.setContent(mapPane.getMapPane());
    }

    public Node getMainPane() {
        return mainPane;
    }
}