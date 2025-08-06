package util;

import javafx.scene.paint.Color;
import java.util.Map;

// Centralized color management for Tetris shapes
public class ShapeColors {
    private static final Map<String, ColorScheme> COLOR_SCHEMES = Map.of(
        "cyan", ColorScheme.of("#00bcd4", "#00acc1"),
        "yellow", ColorScheme.of("#ffeb3b", "#fbc02d"),
        "purple", ColorScheme.of("#9c27b0", "#7b1fa2"),
        "orange", ColorScheme.of("#ff9800", "#f57c00"),
        "blue", ColorScheme.of("#2196f3", "#1976d2"),
        "green", ColorScheme.of("#4caf50", "#388e3c"),
        "red", ColorScheme.of("#f44336", "#d32f2f")
    );
    
    private static final ColorScheme DEFAULT_SCHEME = ColorScheme.of("#111111", "#333333");
    
    public static Color getFillColor(String colorName) {
        return COLOR_SCHEMES.getOrDefault(colorName, DEFAULT_SCHEME).fill();
    }
    
    public static Color getBorderColor(String colorName) {
        return COLOR_SCHEMES.getOrDefault(colorName, DEFAULT_SCHEME).border();
    }
    
    public static ColorScheme getColorScheme(String colorName) {
        return COLOR_SCHEMES.getOrDefault(colorName, DEFAULT_SCHEME);
    }
}