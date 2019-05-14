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

        Scene scene = new Scene(root);
        stage.setScene(scene);
        root.getChildren().add(new MainScene(stage).getMainPane());

        stage.show();
    }
}

