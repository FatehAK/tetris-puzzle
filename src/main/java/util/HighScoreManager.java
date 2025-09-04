package util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.HighScore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HighScoreManager {

    private static final String FILE_PATH = "highscores.json";

    private static HighScoreManager instance;

    private final ObjectMapper mapper;
    private List<HighScore> scores;

    // Private constructor to enforce singleton
    private HighScoreManager() {
        mapper = new ObjectMapper();
        scores = new ArrayList<>();
        loadScores();
    }

    // Thread-safe singleton instance retrieval
    public static synchronized HighScoreManager getInstance() {
        if (instance == null) {
            instance = new HighScoreManager();
        }
        return instance;
    }

    // Load scores from JSON file
    private void loadScores() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try {
                scores = mapper.readValue(file, new TypeReference<List<HighScore>>() {});
            } catch (IOException e) {
                e.printStackTrace();
                scores = new ArrayList<>();
            }
        } else {
            scores = new ArrayList<>();
        }
    }

    // Save scores to JSON file
    private void saveScores() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), scores);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Get immutable copy of top scores
    public List<HighScore> getTopScores() {
        return new ArrayList<>(scores);
    }

    // Add new score and update list
    public void addHighScore(HighScore newScore) {
        scores.add(newScore);
        // Sort descending
        scores.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        // Trim to top 10
        if (scores.size() > 10) {
            scores = scores.subList(0, 10);
        }
        saveScores();
    }

    // Clear all scores
    public void clearScores() {
        scores.clear();
        saveScores();
    }
}
