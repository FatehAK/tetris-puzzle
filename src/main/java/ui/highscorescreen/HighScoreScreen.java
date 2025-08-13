package ui.highscorescreen;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import ui.BaseScreen;

public class HighScoreScreen extends BaseScreen {

    @FXML
    private VBox root;

    public void initialize() {
        // Controller initialization logic here
    }

    public static Scene getScene() {
        return loadScene(HighScoreScreen.class, "highscore.fxml", 600, 400);
    }
}