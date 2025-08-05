package ui.highscorescreen;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;

public class HighScoreScreen {

    @FXML
    private VBox root;

    public void initialize() {
        // Controller initialization logic here
    }

    public static Scene getScene() {
        try {
            FXMLLoader loader = new FXMLLoader(HighScoreScreen.class.getResource("highscore.fxml"));
            Parent root = loader.load();

            return new Scene(root, 600, 400);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load high score screen", e);
        }
    }
}