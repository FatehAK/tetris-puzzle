package ui.configscreen;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class ConfigScreen {
    public static Scene getScene() {
        Label label = new Label("Hello World! - Config Screen");
        StackPane root = new StackPane(label);
        return new Scene(root, 600, 400);
    }
}