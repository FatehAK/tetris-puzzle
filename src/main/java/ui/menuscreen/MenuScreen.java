package ui.menuscreen;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import ui.BaseScreen;
import ui.ExitDialog;

// Controller for the main menu screen of the Tetris game
public class MenuScreen extends BaseScreen {
    @FXML
    private VBox root;

    @FXML
    private Button playBtn, configBtn, scoresBtn, exitBtn;

    private Runnable onPlay, onConfig, onHighScores, onExit;

    public void initialize() {
        // wire button actions after FXML is loaded
        playBtn.setOnAction(e -> onPlay.run());
        configBtn.setOnAction(e -> onConfig.run());
        scoresBtn.setOnAction(e -> onHighScores.run());
        exitBtn.setOnAction(e -> onExit.run());

        applyFadeIn(root);
    }

    private void applyFadeIn(VBox node) {
        node.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), node);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    public static Scene getScene(Runnable onPlay, Runnable onConfig, Runnable onHighScores, Runnable onExit) {
        LoadResult<MenuScreen> result = loadSceneWithController(MenuScreen.class, "menu.fxml", 600, 400);
        MenuScreen controller = result.controller();
        controller.onPlay = onPlay;
        controller.onConfig = onConfig;
        controller.onHighScores = onHighScores;
        controller.onExit = onExit;
        return result.scene();
    }

    public static Scene getSceneWithExitDialog(Stage stage, Runnable onPlay, Runnable onConfig, Runnable onHighScores) {
        return getScene(
            onPlay,
            onConfig,
            onHighScores,
            () -> {
                System.out.println("Exit button clicked");
                boolean confirm = ExitDialog.show(stage);
                if (confirm) {
                    stage.close();
                }
            }
        );
    }
}
