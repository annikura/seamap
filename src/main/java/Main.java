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

        root.getChildren().add(new MapScene().getMapScene());

        stage.setScene(new Scene(root, 1500, 1000));
        stage.show();
    }
}

