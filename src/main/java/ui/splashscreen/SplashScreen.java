package ui.splashscreen;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class SplashScreen {
    public static Scene getScene() {
        Label label = new Label("Hello World! - Splash Screen");
        StackPane root = new StackPane(label);
        return new Scene(root, 600, 400);
    }
}