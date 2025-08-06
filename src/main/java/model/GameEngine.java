package model;

import java.util.Random;

// Controls the game logic and piece movement
public class GameEngine {
    private GameBoard board;
    private TetrisShape currentPiece;
    private Random random;
    private boolean gameRunning;
    private long lastDropTime = 0;
    private static final long DROP_INTERVAL = 800_000_000L; // 0.8 seconds in nanoseconds
    private double smoothY = 0.0; // smooth Y position for animation
    
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
        
        // create piece centered horizontally, start slightly above the game area for falling animation
        int startX = (GameBoard.BOARD_WIDTH - TetrisShape.getWidthForType(randomType)) / 2; // center horizontally
        int startY = 0; // start at the top of the visible game area
        currentPiece = new TetrisShape(randomType, startX, startY);
        smoothY = startY; // initialize smooth position
        
        // check game over when piece reaches the visible area
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
            smoothY = newY; // update smooth position when piece moves
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
        if (!gameRunning || currentPiece == null) {
            return false;
        }
        
        // smooth falling animation
        double deltaTime = (currentTime - lastDropTime) / (double) DROP_INTERVAL;
        
        // update smooth position based on movement capability
        if (board.isValidPosition(currentPiece, currentPiece.getX(), currentPiece.getY() + 1)) {
            smoothY = currentPiece.getY() + Math.min(deltaTime, 1.0);
        } else {
            smoothY = currentPiece.getY();
        }
        
        // check if enough time has passed for next drop
        if (currentTime - lastDropTime >= DROP_INTERVAL) {
            movePieceDown();
            lastDropTime = currentTime;
        }
        
        return true; // always update display for smooth animation
    }
    
    public double getSmoothY() {
        return smoothY;
    }
}