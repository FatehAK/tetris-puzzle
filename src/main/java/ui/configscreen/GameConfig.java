package ui.configscreen;

import util.AudioManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

// Holds game configuration settings that can be shared between screens
public class GameConfig {
    public enum PlayerType {
        HUMAN, AI, EXTERNAL
    }
    
    // configuration settings
    private PlayerType player1Type = PlayerType.HUMAN;
    private PlayerType player2Type = PlayerType.HUMAN;

    private String player1Name = "Player 1";
    private String player2Name = "Player 2";

    private boolean musicEnabled = true;
    private boolean soundEnabled = true;
    private boolean extendedMode = false;
    private int fieldWidth = 10;
    private int fieldHeight = 20;
    private int gameLevel = 1;
    
    private static final String CONFIG_FILE = "tetris_config.json";
    private ObjectMapper objectMapper = new ObjectMapper();
    
    private GameConfig() {
        loadConfigurationFromFile();
    }

    // thread-safe singleton pattern
    private static class InstanceHolder {
        private static final GameConfig INSTANCE = new GameConfig();
    }
    
    public static GameConfig getInstance() {
        return InstanceHolder.INSTANCE;
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

    // player1Name getter and setter
    public String getPlayer1Name() {
        return player1Name;
    }

    public void setPlayer1Name(String player1Name) {
        this.player1Name = player1Name;
    }

    // player2Name getter and setter
    public String getPlayer2Name() {
        return player2Name;
    }

    public void setPlayer2Name(String player2Name) {
        this.player2Name = player2Name;
    }

    // music setting
    public boolean isMusicEnabled() {
        return musicEnabled;
    }
    
    public void setMusicEnabled(boolean musicEnabled) {
        this.musicEnabled = musicEnabled;
        // sync with AudioManager
        AudioManager.getInstance().setMusicEnabled(musicEnabled);
    }
    
    // sound setting
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
        // sync with AudioManager
        AudioManager.getInstance().setSoundEnabled(soundEnabled);
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
    
    public void saveConfigurationToFile() {
        try {
            ConfigData configData = new ConfigData(
                    fieldWidth, fieldHeight, gameLevel,
                    musicEnabled, soundEnabled, extendedMode,
                    player1Type.name(), player2Type.name()
            );
            objectMapper.writeValue(new File(CONFIG_FILE), configData);
        } catch (IOException e) {
            System.err.println("Failed to save configuration: " + e.getMessage());
        }
    }

    private void loadConfigurationFromFile() {
        try {
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                ConfigData configData = objectMapper.readValue(configFile, ConfigData.class);

                // Apply loaded data
                this.fieldWidth = configData.getFieldWidth();
                this.fieldHeight = configData.getFieldHeight();
                this.gameLevel = configData.getGameLevel();
                this.musicEnabled = configData.isMusicEnabled();
                this.soundEnabled = configData.isSoundEnabled();
                this.extendedMode = configData.isExtendedMode();

                // Convert string back to enum
                try {
                    this.player1Type = PlayerType.valueOf(configData.getPlayer1Type());
                    this.player2Type = PlayerType.valueOf(configData.getPlayer2Type());
                } catch (IllegalArgumentException e) {
                    // Use defaults if invalid
                    this.player1Type = PlayerType.HUMAN;
                    this.player2Type = PlayerType.HUMAN;
                }

            }
        } catch (IOException e) {
            System.out.println("No config file found or error loading, using defaults");
        }
    }
}