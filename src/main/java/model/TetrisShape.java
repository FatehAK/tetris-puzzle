package model;

// Represents a Tetris piece shape with its pattern and color
public class TetrisShape {
    
    public enum ShapeType {
        I, O, T, L, Z
    }
    
    private final ShapeType type;
    private final boolean[][] pattern;
    private final String color;
    private int x;
    private int y;
    
    public TetrisShape(ShapeType type, int x, int y) {
        this.type = type;
        this.pattern = createPattern(type);
        this.color = switch (type) {
            case I -> "cyan";
            case O -> "yellow";
            case T -> "purple";
            case L -> "orange";
            case Z -> "green";
        };
        this.x = x;
        this.y = y;
    }
    
    public String getColor() {
        return color;
    }
    
    public int getWidth() {
        return pattern[0].length;
    }
    
    public int getHeight() {
        return pattern.length;
    }
    
    public boolean isCellFilled(int row, int col) {
        if (row < 0 || row >= getHeight() || col < 0 || col >= getWidth()) {
            return false;
        }
        return pattern[row][col];
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    // static method to get width without creating object
    public static int getWidthForType(ShapeType type) {
        return createPattern(type)[0].length;
    }
    
    // make createPattern static for reuse
    private static boolean[][] createPattern(ShapeType type) {
        return switch (type) {
            case I -> new boolean[][] {
                {true, true, true, true}
            };
            case O -> new boolean[][] {
                {true, true},
                {true, true}
            };
            case T -> new boolean[][] {
                {false, true, false},
                {true, true, true}
            };
            case L -> new boolean[][] {
                {true, false},
                {true, false},
                {true, true}
            };
            case Z -> new boolean[][] {
                {true, true, false},
                {false, true, true}
            };
        };
    }
}