package ui.highscorescreen;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HighScoreScreen {

    @FXML private VBox root;
    @FXML private Label titleLabel;
    @FXML private VBox scoreTableContainer;
    @FXML private VBox scoresContainer;
    @FXML private HBox scoreRowContainer;
    @FXML private Button backButton;
    
    private static Runnable backCallback = () -> {};

    @FXML
    public void initialize() {
    }

    @FXML
    private void handleBackButton() {
        backCallback.run();
    }

    public static Scene getScene() {
        return getScene(() -> {});
    }
    
    public static Scene getScene(Runnable onBack) {
        backCallback = onBack;
        try {
            FXMLLoader loader = new FXMLLoader(HighScoreScreen.class.getResource("highscore.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(HighScoreScreen.class.getResource("highscore.css").toExternalForm());
            
            return scene;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load high score screen", e);
        }
    }

    public void handleBackButton(ActionEvent event) throws Exception {
        Parent menuRoot = FXMLLoader.load(getClass().getResource("/ui/menuscreen/menu.fxml"));
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(menuRoot));
    }

}