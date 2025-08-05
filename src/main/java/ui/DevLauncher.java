package ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ui.splashscreen.SplashScreen;
import ui.menuscreen.MenuScreen;
import ui.configscreen.ConfigScreen;
import ui.gameplayscreen.GameplayScreen;
import ui.highscorescreen.HighScoreScreen;

/**
 * DevLauncher.java
 * --------------------------------------
 * Used for launching any individual screen in isolation during development.
 */
public class DevLauncher extends Application {

    @Override
    public void start(Stage stage) {
        ComboBox<String> screenSelector = new ComboBox<>();
        screenSelector.getItems().addAll("splash", "menu", "config", "highscore", "gameplay");
        screenSelector.setValue("splash");

        Button launchButton = new Button("Launch Screen");

        launchButton.setOnAction(e -> {
            String selected = screenSelector.getValue();
            try {
                Scene scene = switch (selected) {
                    case "splash" -> SplashScreen.getScene(() -> {
                        Scene next = MenuScreen.getScene();
                        stage.setScene(next);
                        stage.setTitle("DevLauncher - menu");
                    });
                    case "menu" -> MenuScreen.getScene();
                    case "config" -> ConfigScreen.getScene();
                    case "highscore" -> HighScoreScreen.getScene();
                    case "gameplay" -> GameplayScreen.getScene();
                    default -> throw new IllegalArgumentException("Unknown screen: " + selected);
                };
                stage.setScene(scene);
                stage.setTitle("DevLauncher - " + selected);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        VBox root = new VBox(10, screenSelector, launchButton);
        root.setStyle("-fx-padding: 20; -fx-alignment: center;");
        Scene selectorScene = new Scene(root, 300, 150);

        stage.setTitle("DevLauncher");
        stage.setScene(selectorScene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
