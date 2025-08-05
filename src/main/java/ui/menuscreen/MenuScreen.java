package ui.menuscreen;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

public class MenuScreen {

    @FXML
    private VBox root;

    @FXML
    private Button playBtn, configBtn, scoresBtn, exitBtn;

    private Runnable onPlay, onConfig, onHighScores, onExit;

    public void initialize() {
        // Wire button actions after FXML is loaded
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

    // Factory method to load the scene with callbacks
    public static Scene getScene(Runnable onPlay, Runnable onConfig, Runnable onHighScores, Runnable onExit) {
        try {
            FXMLLoader loader = new FXMLLoader(MenuScreen.class.getResource("menu.fxml"));
            VBox root = loader.load();

            MenuScreen controller = loader.getController();
            controller.onPlay = onPlay;
            controller.onConfig = onConfig;
            controller.onHighScores = onHighScores;
            controller.onExit = onExit;

            return new Scene(root, 600, 400);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load menu screen", e);
        }
    }
}
