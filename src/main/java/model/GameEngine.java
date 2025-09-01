package model;

import java.util.Random;

// Controls the game logic and piece movement
public class GameEngine implements InputController {
    private GameBoard board;
    private TetrisShape currentPiece;
    private Random random;
    private boolean gameRunning;
    private long lastDropTime = 0;
    private static final long DROP_INTERVAL = 800_000_000L; // 0.8 seconds in nanoseconds
    private static final long FAST_DROP_INTERVAL = 50_000_000L; // 0.05 seconds in nanoseconds for fast drop
    private boolean fastDropEnabled = false;
    private double smoothY = 0.0; // smooth Y position for animation
    
    // AI related fields
    private boolean aiEnabled = false;
    private TetrisAI tetrisAI;
    private TetrisAI.Move pendingAIMove = null;
    private int aiRotationsCompleted = 0;
    
    public GameEngine() {
        board = new GameBoard();
        random = new Random();
        gameRunning = false;
        tetrisAI = new TetrisAI();
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
    
    // AI mode controls
    public void setAIEnabled(boolean enabled) {
        this.aiEnabled = enabled;
        pendingAIMove = null; // clear any pending move when toggling AI
    }
    
    public boolean isAIEnabled() {
        return aiEnabled;
    }
    
    public void spawnNewPiece() {
        TetrisShape.ShapeType[] types = TetrisShape.ShapeType.values();
        TetrisShape.ShapeType randomType = types[random.nextInt(types.length)]; // pick random shape
        
        // create piece centered horizontally, start above the game area for proper spawning
        int startX = (GameBoard.BOARD_WIDTH - TetrisShape.getWidthForType(randomType)) / 2; // center horizontally
        int startY = -1; // start above the visible game area to allow proper entry
        currentPiece = new TetrisShape(randomType, startX, startY);
        smoothY = startY; // initialize smooth position
        
        // check game over - piece can't be placed at spawn position
        if (!board.isValidPosition(currentPiece, startX, startY)) {
            stopGame(); // game over - can't spawn new piece at all
        }
        
        // calculate AI move for new piece if AI is enabled
        if (aiEnabled && currentPiece != null) {
            pendingAIMove = tetrisAI.findBestMove(board, currentPiece);
            aiRotationsCompleted = 0; // reset rotation counter for new piece
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
            board.clearFullRows();
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
    
    // InputController interface implementations
    @Override
    public boolean moveLeft() {
        return movePieceLeft();
    }
    
    @Override
    public boolean moveRight() {
        return movePieceRight();
    }
    
    @Override
    public boolean rotate() {
        return rotatePiece();
    }
    
    @Override
    public void setFastDrop(boolean enabled) {
        setFastDropEnabled(enabled);
    }
    
    public boolean rotatePiece() {
        if (currentPiece == null || !gameRunning) {
            return false;
        }
        
        // try rotation at current position
        if (tryRotation(0, 0)) {
            return true;
        }
        
        // wall kick attempts - try moving left or right if rotation fails
        if (tryRotation(-1, 0) || tryRotation(1, 0)) {
            return true;
        }
        
        // additional wall kick for I-piece (try moving 2 positions)
        if (currentPiece.getType() == TetrisShape.ShapeType.I) {
            if (tryRotation(-2, 0) || tryRotation(2, 0)) {
                return true;
            }
        }
        
        return false; // rotation failed
    }
    
    private boolean tryRotation(int deltaX, int deltaY) {
        int newX = currentPiece.getX() + deltaX;
        int newY = currentPiece.getY() + deltaY;
        
        // test rotation without creating new objects
        boolean[][] rotatedPattern = currentPiece.getRotatedPattern();
        
        // check if rotated pattern fits at new position
        for (int row = 0; row < rotatedPattern.length; row++) {
            for (int col = 0; col < rotatedPattern[0].length; col++) {
                if (rotatedPattern[row][col]) {
                    int boardX = newX + col;
                    int boardY = newY + row;
                    
                    if (boardX < 0 || boardX >= GameBoard.BOARD_WIDTH || boardY >= GameBoard.BOARD_HEIGHT) {
                        return false;
                    }
                    
                    if (boardY >= 0 && board.getCellColor(boardY, boardX) != null) {
                        return false;
                    }
                }
            }
        }
        
        // rotation is valid - apply it
        currentPiece.rotate();
        currentPiece.setX(newX);
        currentPiece.setY(newY);
        return true;
    }
    
    public boolean updateGame(long currentTime) {
        if (!gameRunning || currentPiece == null) {
            return false;
        }
        
        // execute AI move if enabled and piece is in visible area
        if (aiEnabled && pendingAIMove != null && currentPiece.getY() >= 0) {
            executeNextAIAction();
        }
        
        // choose drop interval based on fast drop setting or AI mode
        long dropInterval = (fastDropEnabled || aiEnabled) ? FAST_DROP_INTERVAL : DROP_INTERVAL;
        
        // smooth falling animation
        double deltaTime = (currentTime - lastDropTime) / (double) dropInterval;
        
        // update smooth position based on movement capability
        if (board.isValidPosition(currentPiece, currentPiece.getX(), currentPiece.getY() + 1)) {
            smoothY = currentPiece.getY() + Math.min(deltaTime, 1.0);
        } else {
            smoothY = currentPiece.getY();
        }
        
        // check if enough time has passed for next drop
        if (currentTime - lastDropTime >= dropInterval) {
            movePieceDown();
            lastDropTime = currentTime;
        }
        
        return true; // always update display for smooth animation
    }
    
    public double getSmoothY() {
        return smoothY;
    }
    
    // execute one AI action at a time with delay
    private void executeNextAIAction() {
        if (pendingAIMove == null || currentPiece == null) {
            return;
        }
        
        // first, handle rotations
        if (aiRotationsCompleted < pendingAIMove.rotations()) {
            rotatePiece();
            aiRotationsCompleted++;
        } else if (currentPiece.getX() < pendingAIMove.column()) {
            movePieceRight();
        } else if (currentPiece.getX() > pendingAIMove.column()) {
            movePieceLeft();
        } else {
            // finally, clear the pending move
            pendingAIMove = null;
            return; // no delay needed when clearing move
        }
        
        // add delay after any action
        try {
            Thread.sleep(50); // 50ms delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public void setFastDropEnabled(boolean enabled) {
        fastDropEnabled = enabled;
    }
    
    public boolean isFastDropEnabled() {
        return fastDropEnabled;
    }
}