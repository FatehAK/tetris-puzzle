package model;

import java.time.LocalDate;

public class HighScore {
    private String playerName;
    private int score;
    private LocalDate date;

    // No-arg constructor required by Jackson
    public HighScore() {
    }

    public HighScore(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
        this.date = LocalDate.now();
    }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return playerName + ": " + score;
    }
}
