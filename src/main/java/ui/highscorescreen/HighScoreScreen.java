package ui.highscorescreen;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class HighScoreScreen {

    @FXML private VBox root;
    @FXML private Label titleLabel;
    @FXML private VBox scoreTableContainer;
    @FXML private VBox scoresContainer;
    @FXML private Button backButton;
    
    private static Runnable backCallback = () -> {};

    @FXML
    public void initialize() {
        // Add dummy scores to the container
        String[] scores = {
            "1. ALICE - 125,000",
            "2. BOB - 98,750", 
            "3. CHARLIE - 87,500",
            "4. DIANA - 76,250",
            "5. EVE - 65,000"
        };
        
        for (String score : scores) {
            Label scoreLabel = new Label(score);
            scoreLabel.getStyleClass().add("score-text");
            scoresContainer.getChildren().add(scoreLabel);
        }
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
}