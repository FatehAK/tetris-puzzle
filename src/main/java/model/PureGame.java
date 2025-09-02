package model;

import java.util.Arrays;

// Data class for receiving game state from clients via JSON
public class PureGame {
    private int width;
    private int height;
    private String[][] cells;
    private boolean[][] currentShape;
    private boolean[][] nextShape;
    private int currentShapeX;
    private int currentShapeY;
    private String currentShapeType;
    
    public PureGame() {}
    
    public PureGame(int width, int height, String[][] cells, boolean[][] currentShape, boolean[][] nextShape, int currentShapeX, int currentShapeY, String currentShapeType) {
        this.width = width;
        this.height = height;
        this.cells = cells;
        this.currentShape = currentShape;
        this.nextShape = nextShape;
        this.currentShapeX = currentShapeX;
        this.currentShapeY = currentShapeY;
        this.currentShapeType = currentShapeType;
    }
    
    // getters
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public String[][] getCells() {
        return cells;
    }
    
    public boolean[][] getCurrentShape() {
        return currentShape;
    }
    
    public boolean[][] getNextShape() {
        return nextShape;
    }
    
    public int getCurrentShapeX() {
        return currentShapeX;
    }
    
    public int getCurrentShapeY() {
        return currentShapeY;
    }
    
    public String getCurrentShapeType() {
        return currentShapeType;
    }
    
    // setters
    public void setWidth(int width) {
        this.width = width;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public void setCells(String[][] cells) {
        this.cells = cells;
    }
    
    public void setCurrentShape(boolean[][] currentShape) {
        this.currentShape = currentShape;
    }
    
    public void setNextShape(boolean[][] nextShape) {
        this.nextShape = nextShape;
    }
    
    public void setCurrentShapeX(int currentShapeX) {
        this.currentShapeX = currentShapeX;
    }
    
    public void setCurrentShapeY(int currentShapeY) {
        this.currentShapeY = currentShapeY;
    }
    
    public void setCurrentShapeType(String currentShapeType) {
        this.currentShapeType = currentShapeType;
    }
    
    @Override
    public String toString() {
        return "PureGame{" +
                "width=" + width +
                ", height=" + height +
                ", cells=" + Arrays.deepToString(cells) +
                ", currentShape=" + Arrays.deepToString(currentShape) +
                ", nextShape=" + Arrays.deepToString(nextShape) +
                ", currentShapeX=" + currentShapeX +
                ", currentShapeY=" + currentShapeY +
                ", currentShapeType=" + currentShapeType +
                '}';
    }
}