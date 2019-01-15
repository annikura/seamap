import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) throws Exception {
        StackPane root = new StackPane();

        Scene scene = new Scene(root, 1500, 1000);
        root.getChildren().add(new MapScene(scene).getMapScene());

        stage.setScene(scene);
        stage.show();
    }
}

