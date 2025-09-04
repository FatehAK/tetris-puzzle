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
    @FXML private VBox scoreTableContainer;

    private Runnable onBack;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public void initialize() {
        backButton.setOnAction(e -> {
            if (onBack != null) {
                onBack.run();
            }
        });
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
        HBox row = new HBox();
        row.getStyleClass().add("score-row");  // Optional style class, add for CSS

        // Rank Label
        Label rankLabel = new Label(String.valueOf(rank));
        rankLabel.getStyleClass().addAll("score-cell", "score-rank");

        // Player Name Label
        Label nameLabel = new Label(score.getPlayerName());
        nameLabel.getStyleClass().addAll("score-cell", "score-name");

        // Score Label
        Label scoreLabel = new Label(String.valueOf(score.getScore()));
        scoreLabel.getStyleClass().addAll("score-cell", "score-score");

        // Date Label (format your stored date; adjust if HighScore stores differently)
        String formattedDate = (score.getDate() != null) ? score.getDate().format(dateFormatter) : "";
        Label dateLabel = new Label(formattedDate);
        dateLabel.getStyleClass().addAll("score-cell", "score-date");

        // Add all columns to the row
        row.getChildren().addAll(rankLabel, nameLabel, scoreLabel, dateLabel);

        return row;
    }

    public static Scene getScene(Runnable onBack) {
        LoadResult<HighScoreScreen> result = loadSceneWithController(HighScoreScreen.class, "highscore.fxml", 800, 600);
        result.controller().onBack = onBack;
        return result.scene();
    }
}