package util;

import javafx.scene.paint.Color;
import java.util.Map;

// Centralized color management for Tetris shapes
public class ShapeColors {
    private static final Map<String, Color> FILL_COLORS = Map.of(
        "cyan", Color.web("#00bcd4"),
        "yellow", Color.web("#ffeb3b"),
        "purple", Color.web("#9c27b0"),
        "orange", Color.web("#ff9800"),
        "green", Color.web("#4caf50")
    );
    
    private static final Map<String, Color> BORDER_COLORS = Map.of(
        "cyan", Color.web("#00acc1"),
        "yellow", Color.web("#fbc02d"),
        "purple", Color.web("#7b1fa2"),
        "orange", Color.web("#f57c00"),
        "green", Color.web("#388e3c")
    );
    
    private static final Color DEFAULT_FILL = Color.web("#111111");
    private static final Color DEFAULT_BORDER = Color.web("#333333");
    
    public static Color getFillColor(String colorName) {
        return FILL_COLORS.getOrDefault(colorName, DEFAULT_FILL);
    }
    
    public static Color getBorderColor(String colorName) {
        return BORDER_COLORS.getOrDefault(colorName, DEFAULT_BORDER);
    }
}