package model;

public class TetrisShape {
    
    public enum ShapeType {
        I, O, T, L, Z
    }
    
    private final ShapeType type;
    private final boolean[][] pattern;
    private final String color;
    
    public TetrisShape(ShapeType type) {
        this.type = type;
        this.pattern = createPattern(type);
        this.color = getColorForType(type);
    }
    
    private boolean[][] createPattern(ShapeType type) {
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
    
    private String getColorForType(ShapeType type) {
        return switch (type) {
            case I -> "cyan";
            case O -> "yellow";
            case T -> "purple";
            case L -> "orange";
            case Z -> "green";
        };
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
}