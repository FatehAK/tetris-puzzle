package model;

import java.util.Random;

// Controls the game logic and piece movement
public class GameEngine {
    private GameBoard board;
    private TetrisShape currentPiece;
    private Random random;
    private boolean gameRunning;
    private long lastDropTime = 0;
    private static final long DROP_INTERVAL = 500_000_000L; // 0.5 seconds in nanoseconds
    
    public GameEngine() {
        board = new GameBoard();
        random = new Random();
        gameRunning = false;
    }
    
    public void startGame() {
        gameRunning = true;
        board.clearBoard();
        lastDropTime = System.nanoTime(); // initialize timing to prevent immediate drop
        spawnNewPiece();
    }
    
    public void stopGame() {
        gameRunning = false;
    }
    
    public boolean isGameRunning() {
        return gameRunning;
    }
    
    public GameBoard getBoard() {
        return board;
    }
    
    public TetrisShape getCurrentPiece() {
        return currentPiece;
    }
    
    public void spawnNewPiece() {
        TetrisShape.ShapeType[] types = TetrisShape.ShapeType.values();
        TetrisShape.ShapeType randomType = types[random.nextInt(types.length)]; // pick random shape
        
        // Create piece centered horizontally, start slightly above the game area for falling animation
        int startX = (GameBoard.BOARD_WIDTH - TetrisShape.getWidthForType(randomType)) / 2; // center horizontally
        int startY = -1; // start just above the game area to create falling effect
        currentPiece = new TetrisShape(randomType, startX, startY);
        
        // Check game over when piece reaches the visible area
        if (!board.isValidPosition(currentPiece, currentPiece.getX(), 0)) {
            gameRunning = false; // game over - can't place new piece at top of visible area
        }
    }
    
    public boolean movePieceDown() {
        if (currentPiece == null || !gameRunning) {
            return false;
        }
        
        if (movePiece(0, 1)) {
            return true;
        } else {
            // can't move down - place piece and spawn new one
            board.placePiece(currentPiece);
            spawnNewPiece();
            return false;
        }
    }
    
    public boolean movePiece(int deltaX, int deltaY) {
        if (currentPiece == null || !gameRunning) {
            return false;
        }
        
        int newX = currentPiece.getX() + deltaX;
        int newY = currentPiece.getY() + deltaY;
        
        if (board.isValidPosition(currentPiece, newX, newY)) {
            currentPiece.setX(newX);
            currentPiece.setY(newY);
            return true;
        }
        return false;
    }
    
    public boolean movePieceLeft() {
        return movePiece(-1, 0);
    }
    
    public boolean movePieceRight() {
        return movePiece(1, 0);
    }
    
    public boolean updateGame(long currentTime) {
        if (!gameRunning) {
            return false;
        }
        
        // check if enough time has passed for next drop
        if (currentTime - lastDropTime >= DROP_INTERVAL) {
            movePieceDown();
            lastDropTime = currentTime;
            return true; // display needs update
        }
        
        return false; // no update needed
    }
}