package util;

// Simple record for representing 2D coordinates
public record Point(int x, int y) {
    
    public Point translate(int deltaX, int deltaY) {
        return new Point(x + deltaX, y + deltaY);
    }
}