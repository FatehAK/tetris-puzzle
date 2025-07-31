package ui.menuscreen;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.scene.Node;

public class MenuScreen {

    public static Scene getScene(Runnable onPlay, Runnable onConfig, Runnable onHighScores, Runnable onExit) {
        // Buttons
        Button playBtn = createMenuButton("Play", onPlay);
        Button configBtn = createMenuButton("Configuration", onConfig);
        Button scoresBtn = createMenuButton("High Scores", onHighScores);
        Button exitBtn = createMenuButton("Exit", onExit);

        // Layout
        VBox menuLayout = new VBox(20, playBtn, configBtn, scoresBtn, exitBtn);
        menuLayout.setAlignment(Pos.CENTER);
        menuLayout.setStyle("-fx-background-color: #1e272e;");

        // Optional fade-in effect
        applyFadeIn(menuLayout);

        return new Scene(menuLayout, 600, 400);
    }

    private static Button createMenuButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", 18));
        button.setTextFill(Color.WHITE);
        button.setStyle("-fx-background-color: #3c6382; -fx-background-radius: 10;");
        button.setOnAction(e -> action.run());
        button.setPrefWidth(200);
        return button;
    }

    private static void applyFadeIn(Node node) {
        node.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), node);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
}