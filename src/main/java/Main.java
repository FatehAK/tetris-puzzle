import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.splashscreen.SplashScreen;
import ui.menuscreen.MenuScreen;
import ui.gameplayscreen.GameplayScreen;
import ui.configscreen.ConfigScreen;
import ui.configscreen.GameConfig;
import ui.highscorescreen.HighScoreScreen;
import util.ServerMonitor;

// Main application entry point that launches the Tetris game
public class Main extends Application {
    private Stage primaryStage;
    private final ServerMonitor serverMonitor = new ServerMonitor();

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
        GameConfig config = GameConfig.getInstance();
        
        // check if any player is set to external and server is required
        if (config.getPlayer1Type() == GameConfig.PlayerType.EXTERNAL || 
            config.getPlayer2Type() == GameConfig.PlayerType.EXTERNAL) {
            if (!serverMonitor.isServerRunning()) {
                // show reconnection dialog and start checking
                serverMonitor.showDialog();
                startServerCheckingForPlay();
                return; // stay on menu screen until server is available
            }
        }
        
        Scene gameplayScene = GameplayScreen.getScene(this::showMenuScreen);
        primaryStage.setScene(gameplayScene);
    }
    
    // starts background server checking when trying to play with external mode
    private void startServerCheckingForPlay() {
        serverMonitor.startMonitoring(() -> {
            // server is back - hide dialog and start game
            serverMonitor.hideDialog();
            showGameplayScreen();
        });
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
