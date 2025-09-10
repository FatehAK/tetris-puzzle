package util;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import ui.configscreen.GameConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Centralized audio management system using Observer pattern
// Manages background music, sound effects, and notifies observers of setting changes
public class AudioManager implements AudioObserver {
    // singleton instance
    private static class InstanceHolder {
        private static final AudioManager INSTANCE = new AudioManager();
    }
    
    public static AudioManager getInstance() {
        return InstanceHolder.INSTANCE;
    }
    
    // audio resources
    private MediaPlayer backgroundMusicPlayer;
    private final Map<String, Media> soundEffects = new HashMap<>();
    private final List<AudioObserver> observers = new ArrayList<>();
    
    // current settings
    private boolean musicEnabled;
    private boolean soundEnabled;
    private boolean inGameplayMode = false;
    
    private AudioManager() {
        // initialize with current config settings
        GameConfig config = GameConfig.getInstance();
        musicEnabled = config.isMusicEnabled();
        soundEnabled = config.isSoundEnabled();
        
        loadAudioResources();
    }
    
    // load all audio resources at startup
    private void loadAudioResources() {
        try {
            // load background music
            String musicPath = getClass().getResource("/audio/background.mp3").toExternalForm();
            Media backgroundMusic = new Media(musicPath);
            backgroundMusicPlayer = new MediaPlayer(backgroundMusic);
            backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            
            // load sound effects
            loadSoundEffect("line-clear", "/audio/erase-line.wav");
            loadSoundEffect("game-over", "/audio/game-finish.wav");
            loadSoundEffect("move-rotate", "/audio/move-turn.wav");
            
        } catch (Exception e) {
            System.err.println("Error loading audio resources: " + e.getMessage());
        }
    }
    
    // load a single sound effect into memory
    private void loadSoundEffect(String name, String resourcePath) {
        try {
            String soundPath = getClass().getResource(resourcePath).toExternalForm();
            Media sound = new Media(soundPath);
            soundEffects.put(name, sound);
        } catch (Exception e) {
            System.err.println("Error loading sound effect " + name + ": " + e.getMessage());
        }
    }
    
    // add an observer to be notified of audio setting changes
    public void addObserver(AudioObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    // remove an observer
    public void removeObserver(AudioObserver observer) {
        observers.remove(observer);
    }
    
    // start playing background music if enabled and in gameplay mode
    public void startBackgroundMusic() {
        if (musicEnabled && inGameplayMode && backgroundMusicPlayer != null) {
            try {
                backgroundMusicPlayer.play();
            } catch (Exception e) {
                System.err.println("Error starting background music: " + e.getMessage());
            }
        }
    }
    
    // stop background music
    public void stopBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            try {
                backgroundMusicPlayer.stop();
            } catch (Exception e) {
                System.err.println("Error stopping background music: " + e.getMessage());
            }
        }
    }
    
    // pause background music
    public void pauseBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            try {
                backgroundMusicPlayer.pause();
            } catch (Exception e) {
                System.err.println("Error pausing background music: " + e.getMessage());
            }
        }
    }
    
    // resume background music if enabled and in gameplay mode
    public void resumeBackgroundMusic() {
        if (musicEnabled && inGameplayMode && backgroundMusicPlayer != null && 
            backgroundMusicPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
            try {
                backgroundMusicPlayer.play();
            } catch (Exception e) {
                System.err.println("Error resuming background music: " + e.getMessage());
            }
        }
    }
    
    // play a sound effect if sound is enabled
    public void playSoundEffect(String soundName) {
        if (!soundEnabled) {
            return;
        }
        
        Media sound = soundEffects.get(soundName);
        if (sound != null) {
            try {
                MediaPlayer player = new MediaPlayer(sound);
                player.setOnEndOfMedia(() -> player.dispose());
                player.play();
            } catch (Exception e) {
                System.err.println("Error playing sound effect " + soundName + ": " + e.getMessage());
            }
        }
    }
    
    // update music setting and notify observers
    // note: This only updates the setting, actual music playback depends on gameplay mode
    public void setMusicEnabled(boolean enabled) {
        if (this.musicEnabled != enabled) {
            this.musicEnabled = enabled;
            
            // only update background music if we're in gameplay mode
            if (inGameplayMode) {
                if (enabled) {
                    startBackgroundMusic();
                } else {
                    stopBackgroundMusic();
                }
            }
            
            // notify all observers
            notifyMusicSettingChanged(enabled);
        }
    }
    
    // update sound effects setting and notify observers
    public void setSoundEnabled(boolean enabled) {
        if (this.soundEnabled != enabled) {
            this.soundEnabled = enabled;
            
            // notify all observers
            notifySoundSettingChanged(enabled);
        }
    }
    
    // get current music setting
    public boolean isMusicEnabled() {
        return musicEnabled;
    }
    
    // get current sound effects setting
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    // toggle music setting
    public void toggleMusic() {
        setMusicEnabled(!musicEnabled);
    }
    
    // toggle sound effects setting
    public void toggleSound() {
        setSoundEnabled(!soundEnabled);
    }
    
    // enter gameplay mode - allows background music to play if enabled
    public void enterGameplayMode() {
        inGameplayMode = true;
        if (musicEnabled) {
            startBackgroundMusic();
        }
    }
    
    // exit gameplay mode - stops background music regardless of setting
    public void exitGameplayMode() {
        inGameplayMode = false;
        stopBackgroundMusic();
    }
    
    // check if currently in gameplay mode
    public boolean isInGameplayMode() {
        return inGameplayMode;
    }
    
    // notify all observers of music setting change
    private void notifyMusicSettingChanged(boolean enabled) {
        Platform.runLater(() -> {
            for (AudioObserver observer : new ArrayList<>(observers)) {
                try {
                    observer.onMusicSettingChanged(enabled);
                } catch (Exception e) {
                    System.err.println("Error notifying observer of music change: " + e.getMessage());
                }
            }
        });
    }
    
    // notify all observers of sound effects setting change
    private void notifySoundSettingChanged(boolean enabled) {
        Platform.runLater(() -> {
            for (AudioObserver observer : new ArrayList<>(observers)) {
                try {
                    observer.onSoundSettingChanged(enabled);
                } catch (Exception e) {
                    System.err.println("Error notifying observer of sound change: " + e.getMessage());
                }
            }
        });
    }
    
    // clean up audio resources
    public void dispose() {
        stopBackgroundMusic();
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.dispose();
        }
        observers.clear();
    }
    
    @Override
    public void onMusicSettingChanged(boolean enabled) {
    }
    
    @Override
    public void onSoundSettingChanged(boolean enabled) {
    }
    
    // Sound effect constants for easy reference
    public static final String SOUND_LINE_CLEAR = "line-clear";
    public static final String SOUND_GAME_OVER = "game-over";
    public static final String SOUND_MOVE_ROTATE = "move-rotate";
}