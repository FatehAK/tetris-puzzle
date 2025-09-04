package model;

public class HighScore {
    private String playerName;
    private int score;

    public HighScore() {
        // No-arg constructor required for JSON deserialization
    }

    public HighScore(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return playerName + ": " + score;
    }
}

