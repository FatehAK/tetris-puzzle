package ui.splashscreen;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class SplashScreen {

    @FXML
    private VBox root;

    public void playSplash(Runnable onFinish) {
        // Fade in
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(2), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Fade out after delay
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(2), root);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.seconds(2.5));

        fadeIn.setOnFinished(e -> fadeOut.play());
        fadeOut.setOnFinished(e -> onFinish.run());

        fadeIn.play();
    }

    public static Scene getScene(Runnable onFinish) {
        try {
            FXMLLoader loader = new FXMLLoader(SplashScreen.class.getResource("splash.fxml"));
            Parent root = loader.load();

            SplashScreen controller = loader.getController();
            controller.playSplash(onFinish);

            return new Scene(root, 600, 400);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load splash screen", e);
        }
    }
}
