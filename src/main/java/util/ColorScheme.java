package util;

import javafx.scene.paint.Color;

// Record representing a color scheme with fill and border colors
public record ColorScheme(Color fill, Color border) {
    
    public static ColorScheme of(String fillHex, String borderHex) {
        return new ColorScheme(Color.web(fillHex), Color.web(borderHex));
    }
}