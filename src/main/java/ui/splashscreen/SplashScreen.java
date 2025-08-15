package ui.splashscreen;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import ui.BaseScreen;

// Controller for the splash screen that displays when the application starts
// This screen shows a fade-in effect followed by a fade-out before transitioning to the main menu
public class SplashScreen extends BaseScreen {

    @FXML
    private VBox root;
    
    public void initialize() {}

    public void playSplash(Runnable onFinish) {
        // fade in
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(2), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // fade out after delay
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(2), root);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.seconds(2.5));

        fadeIn.setOnFinished(e -> fadeOut.play());
        fadeOut.setOnFinished(e -> onFinish.run());

        fadeIn.play();
    }

    public static Scene getScene(Runnable onFinish) {
        LoadResult<SplashScreen> result = loadSceneWithController(SplashScreen.class, "splash.fxml", 600, 400);
        result.controller().playSplash(onFinish);
        return result.scene();
    }
}
