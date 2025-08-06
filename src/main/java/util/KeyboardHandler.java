package util;

import javafx.scene.input.KeyCode;
import java.util.HashSet;
import java.util.Set;

// Simple keyboard input handler to track pressed keys
public class KeyboardHandler {
    private Set<KeyCode> pressedKeys;
    
    public KeyboardHandler() {
        pressedKeys = new HashSet<>();
    }
    
    public void keyPressed(KeyCode keyCode) {
        pressedKeys.add(keyCode);
    }
    
    public void keyReleased(KeyCode keyCode) {
        pressedKeys.remove(keyCode);
    }
    
    public boolean isKeyPressed(KeyCode keyCode) {
        return pressedKeys.contains(keyCode);
    }
    
    public boolean isDownArrowPressed() {
        return isKeyPressed(KeyCode.DOWN);
    }
    
    public void clearAll() {
        pressedKeys.clear();
    }
}