package util;

// holds game configuration settings that can be shared between screens
public class GameConfig {
    private static GameConfig instance = null;
    
    // configuration settings
    private boolean aiEnabled = false;
    private boolean musicEnabled = true;
    private boolean soundEnabled = true;
    private boolean extendedMode = false;
    private int fieldWidth = 10;
    private int fieldHeight = 20;
    private int gameLevel = 1;
    
    private GameConfig() {
        // private constructor for singleton
    }
    
    // get singleton instance
    public static GameConfig getInstance() {
        if (instance == null) {
            instance = new GameConfig();
        }
        return instance;
    }
    
    // AI setting
    public boolean isAiEnabled() {
        return aiEnabled;
    }
    
    public void setAiEnabled(boolean aiEnabled) {
        this.aiEnabled = aiEnabled;
    }
    
    // music setting
    public boolean isMusicEnabled() {
        return musicEnabled;
    }
    
    public void setMusicEnabled(boolean musicEnabled) {
        this.musicEnabled = musicEnabled;
    }
    
    // sound setting
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }
    
    // extended mode setting
    public boolean isExtendedMode() {
        return extendedMode;
    }
    
    public void setExtendedMode(boolean extendedMode) {
        this.extendedMode = extendedMode;
    }
    
    // field dimensions
    public int getFieldWidth() {
        return fieldWidth;
    }
    
    public void setFieldWidth(int fieldWidth) {
        this.fieldWidth = fieldWidth;
    }
    
    public int getFieldHeight() {
        return fieldHeight;
    }
    
    public void setFieldHeight(int fieldHeight) {
        this.fieldHeight = fieldHeight;
    }
    
    // game level
    public int getGameLevel() {
        return gameLevel;
    }
    
    public void setGameLevel(int gameLevel) {
        this.gameLevel = gameLevel;
    }
}