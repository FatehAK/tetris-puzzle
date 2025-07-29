package ui.highscorescreen;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class HighScoreScreen {
    public static Scene getScene() {
        Label label = new Label("Hello World! - High Score Screen");
        StackPane root = new StackPane(label);
        return new Scene(root, 600, 400);
    }
}