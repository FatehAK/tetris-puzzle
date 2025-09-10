package ui.highscorescreen;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.HighScore;
import util.HighScoreManager;
import ui.BaseScreen;

import java.time.format.DateTimeFormatter;
import java.util.List;

// Controller for the high score screen displaying game leaderboard
public class HighScoreScreen extends BaseScreen {

    @FXML private Button backButton;
    @FXML private Button clearButton;
    @FXML private VBox scoreTableContainer;

    private Runnable onBack;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void initialize() {
        backButton.setOnAction(e -> {
            if (onBack != null) {
                onBack.run();
            }
        });
        
        clearButton.setOnAction(e -> clearAllScores());
        
        loadAndDisplayScores();
    }

    private void loadAndDisplayScores() {
        List<HighScore> scores = HighScoreManager.getInstance().getTopScores();

        // Clear existing rows except header
        // Assuming header is the first child of scoreTableContainer
        if (!scoreTableContainer.getChildren().isEmpty()) {
            scoreTableContainer.getChildren().remove(1, scoreTableContainer.getChildren().size());
        }

        int rank = 1;
        for (HighScore score : scores) {
            HBox row = createScoreRow(rank, score);
            scoreTableContainer.getChildren().add(row);
            rank++;
        }
    }

    private HBox createScoreRow(int rank, HighScore score) {
        HBox row = new HBox(20); // Add spacing between columns
        row.getStyleClass().add("score-row");
        
        Label rankLabel = new Label(String.valueOf(rank));
        Label nameLabel = new Label(score.getPlayerName());
        Label scoreLabel = new Label(String.valueOf(score.getScore()));
        String formattedDate = (score.getDate() != null) ? score.getDate().format(dateFormatter) : "";
        Label dateLabel = new Label(formattedDate);

        rankLabel.setStyle("-fx-alignment: center-left; -fx-min-width: 80px;");
        nameLabel.setStyle("-fx-alignment: center-left; -fx-min-width: 80px;");
        scoreLabel.setStyle("-fx-alignment: center-left; -fx-min-width: 80px;");
        dateLabel.setStyle("-fx-alignment: center-left; -fx-min-width: 80px;");

        row.getChildren().addAll(rankLabel, nameLabel, scoreLabel, dateLabel);

        return row;
    }

    private void clearAllScores() {
        // clear scores in HighScoreManager (clears both memory and JSON file)
        HighScoreManager.getInstance().clearScores();
        loadAndDisplayScores();
    }

    public static Scene getScene(Runnable onBack) {
        LoadResult<HighScoreScreen> result = loadSceneWithController(HighScoreScreen.class, "highscore.fxml", 800, 700);
        result.controller().onBack = onBack;
        return result.scene();
    }
}