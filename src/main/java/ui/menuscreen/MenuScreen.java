package ui.menuscreen;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MenuScreen {
    public static Scene getScene() {
        Label label = new Label("Hello World! - Menu Screen");
        StackPane root = new StackPane(label);
        return new Scene(root, 600, 400);
    }
}