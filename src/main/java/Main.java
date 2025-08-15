import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.splashscreen.SplashScreen;
import ui.menuscreen.MenuScreen;
import ui.gameplayscreen.GameplayScreen;
import ui.configscreen.ConfigScreen;
import ui.highscorescreen.HighScoreScreen;

// Main application entry point that launches the Tetris game
public class Main extends Application {
    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Tetris Puzzle");
        
        // start with splash screen
        showSplashScreen();
        
        primaryStage.show();
    }

    private void showSplashScreen() {
        Scene splashScene = SplashScreen.getScene(this::showMenuScreen);
        primaryStage.setScene(splashScene);
    }

    private void showMenuScreen() {
        Scene menuScene = MenuScreen.getSceneWithExitDialog(
            primaryStage,
            this::showGameplayScreen,
            this::showConfigScreen,
            this::showHighScoreScreen
        );
        primaryStage.setScene(menuScene);
    }

    private void showGameplayScreen() {
        Scene gameplayScene = GameplayScreen.getScene(this::showMenuScreen);
        primaryStage.setScene(gameplayScene);
    }

    private void showConfigScreen() {
        Scene configScene = ConfigScreen.getScene(this::showMenuScreen);
        primaryStage.setScene(configScene);
    }

    private void showHighScoreScreen() {
        Scene highScoreScene = HighScoreScreen.getScene(this::showMenuScreen);
        primaryStage.setScene(highScoreScene);
    }
}
