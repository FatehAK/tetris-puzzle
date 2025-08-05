package model;

// Manages the 10x20 Tetris game board and collision detection
public class GameBoard {
    public static final int BOARD_WIDTH = 10;
    public static final int BOARD_HEIGHT = 20;
    
    private String[][] board;
    
    public GameBoard() {
        board = new String[BOARD_HEIGHT][BOARD_WIDTH];
        clearBoard();
    }
    
    public void clearBoard() {
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                board[row][col] = null;
            }
        }
    }
    
    public boolean isValidPosition(TetrisShape shape, int newX, int newY) {
        
        for (int row = 0; row < shape.getHeight(); row++) {
            for (int col = 0; col < shape.getWidth(); col++) {
                if (shape.isCellFilled(row, col)) {
                    int boardX = newX + col; // translate to board coordinates
                    int boardY = newY + row;
                    
                    if (boardX < 0 || boardX >= BOARD_WIDTH || 
                        boardY < 0 || boardY >= BOARD_HEIGHT) {
                        return false;
                    }
                    
                    if (board[boardY][boardX] != null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public void placePiece(TetrisShape shape) {
        for (int row = 0; row < shape.getHeight(); row++) {
            for (int col = 0; col < shape.getWidth(); col++) {
                if (shape.isCellFilled(row, col)) {
                    int boardX = shape.getX() + col;
                    int boardY = shape.getY() + row;
                    
                    if (boardX >= 0 && boardX < BOARD_WIDTH && 
                        boardY >= 0 && boardY < BOARD_HEIGHT) {
                        board[boardY][boardX] = shape.getColor();
                    }
                }
            }
        }
    }
    
    
    public String getCellColor(int row, int col) {
        if (row >= 0 && row < BOARD_HEIGHT && col >= 0 && col < BOARD_WIDTH) {
            return board[row][col];
        }
        return null;
    }
}