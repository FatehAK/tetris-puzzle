package util;

// Observer interface for audio setting changes
public interface AudioObserver {
    void onMusicSettingChanged(boolean enabled);
    
    void onSoundSettingChanged(boolean enabled);
}