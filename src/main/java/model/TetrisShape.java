package model;

// Represents a Tetris piece shape with its pattern and color
public class TetrisShape {
    
    public enum ShapeType {
        I, O, T, L, J, Z, S
    }
    
    private final ShapeType type;
    private boolean[][] pattern;
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
            case J -> "blue";
            case Z -> "green";
            case S -> "red";
        };
        this.x = x;
        this.y = y;
    }
    
    public String getColor() {
        return color;
    }
    
    public ShapeType getType() {
        return type;
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
    
    // rotates the piece 90 degrees clockwise
    public void rotate() {
        // O-shape doesn't need rotation
        if (type == ShapeType.O) {
            return;
        }
        
        pattern = rotatePattern(pattern);
    }
    
    // creates a rotated copy without modifying this piece (for testing)
    public boolean[][] getRotatedPattern() {
        if (type == ShapeType.O) {
            return pattern;
        }
        return rotatePattern(pattern);
    }
    
    // rotates a 2D boolean array 90 degrees clockwise
    private boolean[][] rotatePattern(boolean[][] original) {
        int originalRows = original.length;
        int originalCols = original[0].length;
        
        // rotated dimensions are swapped
        boolean[][] rotated = new boolean[originalCols][originalRows];
        
        for (int row = 0; row < originalRows; row++) {
            for (int col = 0; col < originalCols; col++) {
                // 90 degree clockwise rotation formula: (row, col) -> (col, originalRows - 1 - row)
                rotated[col][originalRows - 1 - row] = original[row][col];
            }
        }
        
        return rotated;
    }
    
    private static boolean[][] createPattern(ShapeType type) {
        return switch (type) {
            case I -> new boolean[][] {
                {true},
                {true},
                {true},
                {true}
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
            case J -> new boolean[][] {
                {false, true},
                {false, true},
                {true, true}
            };
            case Z -> new boolean[][] {
                {true, true, false},
                {false, true, true}
            };
            case S -> new boolean[][] {
                {false, true, true},
                {true, true, false}
            };
        };
    }
}