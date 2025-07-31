package ui.splashscreen;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class SplashScreen {

    public static Scene getScene(Runnable onFinish) {

        // Load Tetris logo
        Image logo = new Image(SplashScreen.class.getResourceAsStream("/images/Tetris_logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(250);
        logoView.setPreserveRatio(true);

        // Course info
        Label course = new Label("Course Code: 7010ICT");
        course.setFont(Font.font("Arial", 18));
        course.setTextFill(Color.WHITE);

        // Group info
        Label group = new Label("Group 10: Fateh, Ambrose, Kevin, Patrick");
        group.setFont(Font.font("Arial", 18));
        group.setTextFill(Color.WHITE);

        // Footer
        Label footer = new Label("Welcome to the Tetris Puzzle Challenge!");
        footer.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        footer.setTextFill(Color.LIGHTGRAY);

        // Layout
        VBox root = new VBox(15, logoView, course, group, footer);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2c3e50;");
        root.setOpacity(0);  // Initially transparent

        // Fade in
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(2), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Fade out after delay
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(2), root);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.seconds(2.5)); // Visible for 2.5s

        fadeIn.setOnFinished(e -> fadeOut.play());
        fadeOut.setOnFinished(e -> onFinish.run());

        fadeIn.play();

        return new Scene(root, 600, 400);
    }
}