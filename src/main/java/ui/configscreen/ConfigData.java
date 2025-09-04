package ui.configscreen;

/**
 * Data class for configuration settings - used for JSON serialization
 * Maps to the GameConfig singleton for persistence
 */
public class ConfigData {
    private int fieldWidth;
    private int fieldHeight;
    private int gameLevel;
    private boolean musicEnabled;
    private boolean soundEnabled;
    private boolean extendedMode;
    private String player1Type;
    private String player2Type;

    // Default constructor (required for JSON deserialization)
    public ConfigData() {
        this.fieldWidth = 10;
        this.fieldHeight = 20;
        this.gameLevel = 1;
        this.musicEnabled = true;
        this.soundEnabled = true;
        this.extendedMode = false;
        this.player1Type = "HUMAN";
        this.player2Type = "HUMAN";
    }

    // Constructor with all parameters
    public ConfigData(int fieldWidth, int fieldHeight, int gameLevel,
                      boolean musicEnabled, boolean soundEnabled, boolean extendedMode,
                      String player1Type, String player2Type) {
        this.fieldWidth = fieldWidth;
        this.fieldHeight = fieldHeight;
        this.gameLevel = gameLevel;
        this.musicEnabled = musicEnabled;
        this.soundEnabled = soundEnabled;
        this.extendedMode = extendedMode;
        this.player1Type = player1Type;
        this.player2Type = player2Type;
    }

    // Getters
    public int getFieldWidth() { return fieldWidth; }
    public int getFieldHeight() { return fieldHeight; }
    public int getGameLevel() { return gameLevel; }
    public boolean isMusicEnabled() { return musicEnabled; }
    public boolean isSoundEnabled() { return soundEnabled; }
    public boolean isExtendedMode() { return extendedMode; }
    public String getPlayer1Type() { return player1Type; }
    public String getPlayer2Type() { return player2Type; }

    // Setters
    public void setFieldWidth(int fieldWidth) { this.fieldWidth = fieldWidth; }
    public void setFieldHeight(int fieldHeight) { this.fieldHeight = fieldHeight; }
    public void setGameLevel(int gameLevel) { this.gameLevel = gameLevel; }
    public void setMusicEnabled(boolean musicEnabled) { this.musicEnabled = musicEnabled; }
    public void setSoundEnabled(boolean soundEnabled) { this.soundEnabled = soundEnabled; }
    public void setExtendedMode(boolean extendedMode) { this.extendedMode = extendedMode; }
    public void setPlayer1Type(String player1Type) { this.player1Type = player1Type; }
    public void setPlayer2Type(String player2Type) { this.player2Type = player2Type; }

    @Override
    public String toString() {
        return String.format(
                "ConfigData{width=%d, height=%d, level=%d, music=%s, sound=%s, extended=%s, p1=%s, p2=%s}",
                fieldWidth, fieldHeight, gameLevel, musicEnabled, soundEnabled, extendedMode, player1Type, player2Type
        );
    }
}