package ui.configscreen;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import ui.BaseScreen;

public class ConfigScreen extends BaseScreen {

    @FXML
    private VBox root;

    public void initialize() {
        // Controller initialization logic here
    }

    public static Scene getScene() {
        return loadScene(ConfigScreen.class, "config.fxml", 600, 400);
    }
}