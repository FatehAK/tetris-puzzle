package ui.configscreen;

// Holds game configuration settings that can be shared between screens
public class GameConfig {
    
    public enum PlayerType {
        HUMAN, AI, EXTERNAL
    }
    
    private static GameConfig instance = null;
    
    // configuration settings
    private PlayerType player1Type = PlayerType.HUMAN;
    private PlayerType player2Type = PlayerType.HUMAN;
    private boolean musicEnabled = true;
    private boolean soundEnabled = true;
    private boolean extendedMode = false;
    private int fieldWidth = 10;
    private int fieldHeight = 20;
    private int gameLevel = 1;
    
    private GameConfig() {}
    
    public static GameConfig getInstance() {
        if (instance == null) {
            instance = new GameConfig();
        }
        return instance;
    }
    
    // player type settings
    public PlayerType getPlayer1Type() {
        return player1Type;
    }
    
    public void setPlayer1Type(PlayerType player1Type) {
        this.player1Type = player1Type;
    }
    
    public PlayerType getPlayer2Type() {
        return player2Type;
    }
    
    public void setPlayer2Type(PlayerType player2Type) {
        this.player2Type = player2Type;
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