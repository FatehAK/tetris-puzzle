package ui.highscorescreen;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import ui.BaseScreen;

// Controller for the high score screen displaying game leaderboard
public class HighScoreScreen extends BaseScreen {

    @FXML private Button backButton;
    
    private Runnable onBack;

    public void initialize() {
        backButton.setOnAction(e -> {
            if (onBack != null) {
                onBack.run();
            }
        });
    }

    public static Scene getScene(Runnable onBack) {
        LoadResult<HighScoreScreen> result = loadSceneWithController(HighScoreScreen.class, "highscore.fxml", 800, 600);
        result.controller().onBack = onBack;
        return result.scene();
    }
}