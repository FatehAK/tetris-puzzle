package model;

import java.io.*;
import java.net.*;
import java.util.Random;
import com.google.gson.Gson;

import util.AudioManager;

// Controls the game logic and piece movement
public class GameEngine {
    private GameBoard board;
    private TetrisShape currentShape;
    private TetrisShape.ShapeType nextShapeType;
    private Random random;
    private boolean gameRunning;
    private long lastDropTime = 0;
    private static final long BASE_DROP_INTERVAL = 800_000_000L; // 0.8 seconds in nanoseconds at level 1
    private static final long FAST_DROP_INTERVAL = 50_000_000L; // 0.05 seconds in nanoseconds for fast drop
    private static final long MIN_DROP_INTERVAL = 100_000_000L; // 0.1 seconds minimum drop time
    private boolean fastDropEnabled = false;
    private double smoothY = 0.0; // smooth Y position for animation
    
    // AI related fields
    private boolean aiEnabled = false;
    private TetrisAI tetrisAI;
    private TetrisAI.Move pendingAIMove = null;
    private int aiRotationsCompleted = 0;
    
    // External player mode fields
    private boolean externalPlayerMode = false;
    private OpMove pendingExternalMove = null;
    private int externalRotationsCompleted = 0;
    private Gson gson;

    private int currentScore = 0;
    private int initialLevel = 1;   // default start level
    private int currentLevel = 1;   // increments as needed
    private int linesErased = 0;    // total cleared lines tracker

    public int getInitialLevel() {
        return initialLevel;
    }

    public void setInitialLevel(int level) {
        this.initialLevel = level;
        this.currentLevel = level; // sync current level when initial level set
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int level) {
        this.currentLevel = level;
    }

    public int getLinesErased() {
        return linesErased;
    }

    public void addLinesErased(int count) {
        linesErased += count;
        // Example: increase level every 10 lines cleared
        currentLevel = initialLevel + (linesErased / 10);
    }

    // calculate drop interval based on current level (higher level = faster drops)
    public long getDropInterval() {
        if (fastDropEnabled || aiEnabled || externalPlayerMode) {
            return FAST_DROP_INTERVAL;
        }

        // level 1 starts at BASE_DROP_INTERVAL (0.8s)
        // each level reduces drop time by 50ms down to MIN_DROP_INTERVAL (0.1s)
        long intervalReduction = (currentLevel - 1) * 50_000_000L; // 50ms per level
        long calculatedInterval = BASE_DROP_INTERVAL - intervalReduction;

        // don't go below minimum drop interval
        return Math.max(calculatedInterval, MIN_DROP_INTERVAL);
    }

    public GameEngine() {
        this(new Random(),
             ui.configscreen.GameConfig.getInstance().getFieldWidth(),
             ui.configscreen.GameConfig.getInstance().getFieldHeight());
    }
    
    public GameEngine(Random sharedRandom, int boardWidth, int boardHeight) {
        board = new GameBoard(boardWidth, boardHeight);
        random = sharedRandom;
        gameRunning = false;
        tetrisAI = new TetrisAI();
        gson = new Gson();
        aiEnabled = false;
        externalPlayerMode = false;
    }

    // constructor for multi-player scenarios
    public GameEngine(Random sharedRandom, int boardWidth, int boardHeight, boolean isAIPlayer, boolean isExternalPlayer) {
        board = new GameBoard(boardWidth, boardHeight);
        random = sharedRandom;
        gameRunning = false;
        tetrisAI = new TetrisAI();
        gson = new Gson();
        aiEnabled = isAIPlayer;
        externalPlayerMode = isExternalPlayer;
    }
    
    public void startGame() {
        gameRunning = true;
        board.clearBoard();
        lastDropTime = System.nanoTime(); // initialize timing to prevent immediate drop
        nextShapeType = null; // reset next shape to trigger random first piece
        currentScore = 0;

        // initialize levels and lines for new game
        ui.configscreen.GameConfig config = ui.configscreen.GameConfig.getInstance();
        setInitialLevel(config.getGameLevel());
        linesErased = 0;

        spawnNewShape();
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
    
    public TetrisShape getCurrentShape() {
        return currentShape;
    }
    
    public TetrisShape.ShapeType getNextShapeType() {
        return nextShapeType;
    }
    
    // creates a preview shape from the next shape type
    public TetrisShape getNextShape() {
        if (nextShapeType == null) {
            return null;
        }
        return new TetrisShape(nextShapeType, 0, 0);
    }

    public void spawnNewShape() {
        TetrisShape.ShapeType shapeType;
        
        if (nextShapeType == null) {
            // first shape
            TetrisShape.ShapeType[] types = TetrisShape.ShapeType.values();
            shapeType = types[random.nextInt(types.length)];
        } else {
            // use the next shape
            shapeType = nextShapeType;
        }
        
        // generate new next shape
        TetrisShape.ShapeType[] types = TetrisShape.ShapeType.values();
        nextShapeType = types[random.nextInt(types.length)];
        
        // create shape centered horizontally, start above the game area for proper spawning
        int startX = (board.getBoardWidth() - TetrisShape.getWidthForType(shapeType)) / 2; // center horizontally
        int startY = -1; // start above the visible game area to allow proper entry
        currentShape = new TetrisShape(shapeType, startX, startY);
        smoothY = startY; // initialize smooth position
        
        // check game over - shape can't be placed at spawn position
        if (!board.isValidPosition(currentShape, startX, startY)) {
            stopGame(); // game over - can't spawn new shape at all
        }
        
        // calculate AI move for new shape if AI is enabled
        if (aiEnabled && currentShape != null) {
            pendingAIMove = tetrisAI.findBestMove(board, currentShape);
            aiRotationsCompleted = 0; // reset rotation counter for new shape
        }
        
        // get move from external server if external player mode is enabled
        if (externalPlayerMode && currentShape != null) {
            pendingExternalMove = requestMoveFromServer();
            externalRotationsCompleted = 0; // reset rotation counter for new shape
        }
    }
    
    public boolean movePieceDown() {
        if (currentShape == null || !gameRunning) {
            return false;
        }
        
        if (movePiece(0, 1)) {
            return true;
        } else {
            // Can't move down: place piece and clear rows
            board.placePiece(currentShape);

            int rowsCleared = board.clearFullRows();

            // UPDATE SCORE BASED ON ROWS CLEARED
            if (rowsCleared > 0) {
                int pointsEarned = calculatePointsForRows(rowsCleared);
                addScore(pointsEarned);
                addLinesErased(rowsCleared); // update lines & level
            }

            spawnNewShape();
            return false;
        }
    }

    private int calculatePointsForRows(int rowsCleared) {
        // Standard Tetris scoring rules
        return switch (rowsCleared) {
            case 1 -> 100;   // Single
            case 2 -> 300;   // Double  
            case 3 -> 600;   // Triple
            case 4 -> 1000;  // Tetris
            default -> 0;    // No lines cleared
        };
    }

    public boolean movePiece(int deltaX, int deltaY) {
        if (currentShape == null || !gameRunning) {
            return false;
        }
        
        int newX = currentShape.getX() + deltaX;
        int newY = currentShape.getY() + deltaY;
        
        if (board.isValidPosition(currentShape, newX, newY)) {
            currentShape.setX(newX);
            currentShape.setY(newY);
            smoothY = newY; // update smooth position when piece moves
            return true;
        }
        return false;
    }
    
    public boolean movePieceLeft() {
        boolean moved = movePiece(-1, 0);
        if (moved) {
            AudioManager.getInstance().playSoundEffect(AudioManager.SOUND_MOVE_ROTATE);
        }
        return moved;
    }
    
    public boolean movePieceRight() {
        boolean moved = movePiece(1, 0);
        if (moved) {
            AudioManager.getInstance().playSoundEffect(AudioManager.SOUND_MOVE_ROTATE);
        }
        return moved;
    }
    
    public boolean executeCommand(GameCommand command) {
        return command.execute(this);
    }
    
    public boolean rotatePiece() {
        if (currentShape == null || !gameRunning) {
            return false;
        }
        
        // try rotation at current position
        if (tryRotation(0, 0)) {
            AudioManager.getInstance().playSoundEffect(AudioManager.SOUND_MOVE_ROTATE);
            return true;
        }
        
        // wall kick attempts - try moving left or right if rotation fails
        if (tryRotation(-1, 0) || tryRotation(1, 0)) {
            AudioManager.getInstance().playSoundEffect(AudioManager.SOUND_MOVE_ROTATE);
            return true;
        }
        
        // additional wall kick for I-piece (try moving 2 positions)
        if (currentShape.getType() == TetrisShape.ShapeType.I) {
            if (tryRotation(-2, 0) || tryRotation(2, 0)) {
                AudioManager.getInstance().playSoundEffect(AudioManager.SOUND_MOVE_ROTATE);
                return true;
            }
        }
        
        return false; // rotation failed
    }
    
    private boolean tryRotation(int deltaX, int deltaY) {
        int newX = currentShape.getX() + deltaX;
        int newY = currentShape.getY() + deltaY;
        
        // test rotation without creating new objects
        boolean[][] rotatedPattern = currentShape.getRotatedPattern();
        
        // check if rotated pattern fits at new position
        for (int row = 0; row < rotatedPattern.length; row++) {
            for (int col = 0; col < rotatedPattern[0].length; col++) {
                if (rotatedPattern[row][col]) {
                    int boardX = newX + col;
                    int boardY = newY + row;
                    
                    if (boardX < 0 || boardX >= board.getBoardWidth() || boardY >= board.getBoardHeight()) {
                        return false;
                    }
                    
                    if (boardY >= 0 && board.getCellColor(boardY, boardX) != null) {
                        return false;
                    }
                }
            }
        }
        
        // rotation is valid - apply it
        currentShape.rotate();
        currentShape.setX(newX);
        currentShape.setY(newY);
        return true;
    }
    
    public boolean updateGame(long currentTime) {
        if (!gameRunning || currentShape == null) {
            return false;
        }
        
        // execute AI move if enabled and piece is in visible area
        if (aiEnabled && pendingAIMove != null && currentShape.getY() >= 0) {
            executeNextAIAction();
        }
        
        // execute external move if enabled and piece is in visible area
        if (externalPlayerMode && pendingExternalMove != null && currentShape.getY() >= 0) {
            executeNextExternalAction();
        }
        
        // get level-based drop interval
        long dropInterval = getDropInterval();
        
        // smooth falling animation
        double deltaTime = (currentTime - lastDropTime) / (double) dropInterval;
        
        // update smooth position based on movement capability
        if (board.isValidPosition(currentShape, currentShape.getX(), currentShape.getY() + 1)) {
            smoothY = currentShape.getY() + Math.min(deltaTime, 1.0);
        } else {
            smoothY = currentShape.getY();
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

    public int getScore() {
        return currentScore;
    }

    public void addScore(int points) {
        currentScore += points;
    }

    // execute one AI action at a time with delay
    private void executeNextAIAction() {
        if (pendingAIMove == null || currentShape == null) {
            return;
        }
        
        // first, handle rotations
        if (aiRotationsCompleted < pendingAIMove.rotations()) {
            executeCommand(GameCommand.rotate());
            aiRotationsCompleted++;
        } else if (currentShape.getX() < pendingAIMove.column()) {
            executeCommand(GameCommand.moveRight());
        } else if (currentShape.getX() > pendingAIMove.column()) {
            executeCommand(GameCommand.moveLeft());
        } else {
            // finally, clear the pending move
            pendingAIMove = null;
            return;
        }
        
        // add delay after any action
        try {
            Thread.sleep(50); // 50ms delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    // executes the next action from external server move
    private void executeNextExternalAction() {
        if (pendingExternalMove == null || currentShape == null) {
            return;
        }
        
        
        // first, handle rotations
        if (externalRotationsCompleted < pendingExternalMove.opRotate()) {
            executeCommand(GameCommand.rotate());
            externalRotationsCompleted++;
        } else if (currentShape.getX() < pendingExternalMove.opX()) {
            executeCommand(GameCommand.moveRight());
        } else if (currentShape.getX() > pendingExternalMove.opX()) {
            executeCommand(GameCommand.moveLeft());
        } else {
            // finally, clear the pending move
            pendingExternalMove = null;
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
    
    
    // requests optimal move from external TetrisServer - fails fast, no blocking
    private OpMove requestMoveFromServer() {
        try {
            PureGame pureGame = createPureGameFromCurrentState();
            
            try (Socket socket = new Socket("localhost", 3000);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                
                String requestJson = gson.toJson(pureGame);
                out.println(requestJson);
                
                String responseJson = in.readLine();
                if (responseJson != null) {
                    return gson.fromJson(responseJson, OpMove.class);
                }
            }
        } catch (IOException e) {
            // server connection failed - UI handles reconnection
        }
        
        return new OpMove(0, 0); // default safe move
    }
    
    
    // creates PureGame object from current game state
    private PureGame createPureGameFromCurrentState() {
        // get current board state as String[][]
        String[][] cells = new String[board.getBoardHeight()][board.getBoardWidth()];
        for (int row = 0; row < board.getBoardHeight(); row++) {
            for (int col = 0; col < board.getBoardWidth(); col++) {
                cells[row][col] = board.getCellColor(row, col);
            }
        }
        
        // get current shape pattern
        boolean[][] currentShapePattern = null;
        if (currentShape != null) {
            currentShapePattern = new boolean[currentShape.getHeight()][currentShape.getWidth()];
            for (int row = 0; row < currentShape.getHeight(); row++) {
                for (int col = 0; col < currentShape.getWidth(); col++) {
                    currentShapePattern[row][col] = currentShape.isCellFilled(row, col);
                }
            }
        }
        
        // get next shape pattern
        boolean[][] nextShapePattern = null;
        TetrisShape nextShape = getNextShape();
        if (nextShape != null) {
            nextShapePattern = new boolean[nextShape.getHeight()][nextShape.getWidth()];
            for (int row = 0; row < nextShape.getHeight(); row++) {
                for (int col = 0; col < nextShape.getWidth(); col++) {
                    nextShapePattern[row][col] = nextShape.isCellFilled(row, col);
                }
            }
        }
        
        int shapeX = currentShape != null ? currentShape.getX() : 0;
        int shapeY = currentShape != null ? currentShape.getY() : 0;
        String shapeType = currentShape != null ? currentShape.getType().name() : "T";
        
        return new PureGame(board.getBoardWidth(), board.getBoardHeight(),
                           cells, currentShapePattern, nextShapePattern, shapeX, shapeY, shapeType);
    }
}
