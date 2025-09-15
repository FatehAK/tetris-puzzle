package ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import ui.splashscreen.SplashScreen;
import ui.menuscreen.MenuScreen;
import ui.configscreen.ConfigScreen;
import ui.gameplayscreen.GameplayScreen;
import ui.highscorescreen.HighScoreScreen;

// Used for launching any individual screen in isolation during development.
public class DevLauncher extends Application {

    // Default window dimensions for non-gameplay screens
    private static final double DEFAULT_WIDTH = 700;
    private static final double DEFAULT_HEIGHT = 640;

    @Override
    public void start(Stage stage) {
        ComboBox<String> screenSelector = new ComboBox<>();
        String[] screens = {"splash", "menu", "config", "highscore", "gameplay"};
        for (String screen : screens) {
            screenSelector.getItems().add(screen);
        }
        screenSelector.setValue("splash");

        Button launchButton = new Button("Launch Screen");

        launchButton.setOnAction(e -> {
            String selected = screenSelector.getValue();
            try {
                Scene scene = switch (selected) {
                    case "splash" -> SplashScreen.getScene(() -> {
                        Scene next = MenuScreen.getScene(
                                () -> System.out.println("Play pressed"),
                                () -> System.out.println("Config pressed"),
                                () -> System.out.println("High Scores pressed"),
                                () -> System.out.println("Exit pressed")
                        );
                        setSceneWithResize(stage, next, DEFAULT_WIDTH, DEFAULT_HEIGHT);
                        stage.setTitle("DevLauncher - menu");
                    });
                    case "menu" -> MenuScreen.getSceneWithExitDialog(
                            stage,
                            () -> {
                                // Play button - launch gameplay with dynamic sizing
                                Scene gameplayScene = GameplayScreen.getScene(() -> {
                                    Scene menuScene = MenuScreen.getScene(
                                            () -> System.out.println("Play pressed"),
                                            () -> System.out.println("Config pressed"),
                                            () -> System.out.println("High Scores pressed"),
                                            () -> System.out.println("Exit pressed")
                                    );
                                    // Reset to default size when returning to menu
                                    setSceneWithResize(stage, menuScene, DEFAULT_WIDTH, DEFAULT_HEIGHT);
                                    stage.setTitle("DevLauncher - menu");
                                });
                                // Set gameplay scene with dynamic sizing
                                setSceneWithDynamicResize(stage, gameplayScene);
                                stage.setTitle("DevLauncher - gameplay");
                            },
                            () -> {
                                // Config button - launch config screen
                                Scene configScene = ConfigScreen.getScene(() -> {
                                    Scene menuScene = MenuScreen.getScene(
                                            () -> System.out.println("Play pressed"),
                                            () -> System.out.println("Config pressed"),
                                            () -> System.out.println("High Scores pressed"),
                                            () -> System.out.println("Exit pressed")
                                    );
                                    setSceneWithResize(stage, menuScene, DEFAULT_WIDTH, DEFAULT_HEIGHT);
                                    stage.setTitle("DevLauncher - menu");
                                });
                                setSceneWithResize(stage, configScene, DEFAULT_WIDTH, DEFAULT_HEIGHT);
                                stage.setTitle("DevLauncher - config");
                            },
                            () -> System.out.println("High Scores pressed")
                    );
                    case "config" -> ConfigScreen.getScene(() -> {
                        Scene menuScene = MenuScreen.getScene(
                                () -> System.out.println("Play pressed"),
                                () -> System.out.println("Config pressed"),
                                () -> System.out.println("High Scores pressed"),
                                () -> System.out.println("Exit pressed")
                        );
                        setSceneWithResize(stage, menuScene, DEFAULT_WIDTH, DEFAULT_HEIGHT);
                        stage.setTitle("DevLauncher - menu");
                    });
                    case "highscore" -> HighScoreScreen.getScene(() -> {
                        Scene menuScene = MenuScreen.getScene(
                                () -> System.out.println("Play pressed"),
                                () -> System.out.println("Config pressed"),
                                () -> System.out.println("High Scores pressed"),
                                () -> System.out.println("Exit pressed")
                        );
                        setSceneWithResize(stage, menuScene, DEFAULT_WIDTH, DEFAULT_HEIGHT);
                        stage.setTitle("DevLauncher - menu");
                    });
                    case "gameplay" -> GameplayScreen.getScene(() -> {
                        Scene menuScene = MenuScreen.getScene(
                                () -> System.out.println("Play pressed"),
                                () -> System.out.println("Config pressed"),
                                () -> System.out.println("High Scores pressed"),
                                () -> System.out.println("Exit pressed")
                        );
                        // Reset to default size when returning to menu
                        setSceneWithResize(stage, menuScene, DEFAULT_WIDTH, DEFAULT_HEIGHT);
                        stage.setTitle("DevLauncher - menu");
                    });
                    default -> throw new IllegalArgumentException("Unknown screen: " + selected);
                };

                // For direct screen selection, use appropriate sizing
                if ("gameplay".equals(selected)) {
                    setSceneWithDynamicResize(stage, scene);
                } else {
                    setSceneWithResize(stage, scene, DEFAULT_WIDTH, DEFAULT_HEIGHT);
                }
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

    /**
     * Set scene with specific dimensions and center the window
     */
    private void setSceneWithResize(Stage stage, Scene scene, double width, double height) {
        stage.setScene(scene);
        stage.setWidth(width);
        stage.setHeight(height);
        centerWindow(stage);

        System.out.println("Window resized to: " + width + "x" + height + " (default size)");
    }

    /**
     * Set scene with dynamic sizing based on the scene's preferred dimensions
     */
    private void setSceneWithDynamicResize(Stage stage, Scene scene) {
        stage.setScene(scene);

        // Use the scene's preferred dimensions (set by GameplayScreen.getScene())
        double newWidth = scene.getWidth();
        double newHeight = scene.getHeight();

        stage.setWidth(newWidth);
        stage.setHeight(newHeight);
        centerWindow(stage);

        System.out.println("Window dynamically resized to: " + newWidth + "x" + newHeight);
    }

    /**
     * Center the window on the screen
     */
    private void centerWindow(Stage stage) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double centerX = (screenBounds.getWidth() - stage.getWidth()) / 2;
        double centerY = (screenBounds.getHeight() - stage.getHeight()) / 2;

        stage.setX(centerX);
        stage.setY(centerY);

        System.out.println("Window centered at: " + centerX + ", " + centerY);
    }

    public static void main(String[] args) {
        launch(args);
    }
}