package ui.gameplayscreen;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import model.GameBoard;
import model.GameEngine;
import model.TetrisShape;
import ui.BaseScreen;
import ui.GameOverDialog;
import util.ShapeColors;
import util.ServerMonitor;
import util.AudioManager;
import util.AudioObserver;
import ui.configscreen.GameConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
// JavaFX controller for the main game screen with falling pieces
public class GameplayScreen extends BaseScreen implements AudioObserver {
    private boolean paused = false;

    // FXML components
    @FXML private Canvas gameCanvas;
    @FXML private VBox gameContainer;
    @FXML private Button backButton;
    @FXML private Label audioStatusLabel;

    private final List<GameEngine> engines = new ArrayList<>();
    private final List<Canvas> canvases = new ArrayList<>();
    private final List<GraphicsContext> contexts = new ArrayList<>();
    private long gameSeed; // seed for synchronized sequences
    
    private AnimationTimer gameLoop;
    private boolean isExtendedMode;
    private boolean serverMonitorStarted = false;
    
    private GameConfig currentConfig;
    
    private boolean finalResultsHandled = false;
    
    // UI constants
    private static final int CELL_SIZE = 25;
    private static final int PADDING = 0;
    private static final Color BACKGROUND_COLOR = Color.web("#111111");
    private static final Color BORDER_COLOR = Color.web("#333333");
    private static final Color PAUSE_TEXT_COLOR = Color.web("#FFFFFF");
    
    private Runnable onBackToMenu;
    private AudioManager audioManager;
    private final ServerMonitor serverMonitor = new ServerMonitor();

    public void initialize() {
        currentConfig = GameConfig.getInstance();
        isExtendedMode = currentConfig.isExtendedMode();
        
        // initialize audio system
        audioManager = AudioManager.getInstance();
        audioManager.addObserver(this);
        audioManager.enterGameplayMode();
        
        // initialize audio status display
        updateAudioStatusDisplay();

        if (isExtendedMode) {
            setupTwoPlayerMode();
        } else {
            setupSinglePlayerMode();
        }
        
        backButton.setOnAction(event -> onBackButtonClicked());
        startGameLoop();
    }

    private void onBackButtonClicked() {
        // if all games are over, directly go back to menu
        if (engines.isEmpty() || engines.stream().noneMatch(GameEngine::isGameRunning)) {
            navigateToMenu();
            return;
        }
        
        boolean wasAlreadyPaused = paused;
        
        // pause game if not already paused
        if (!paused) {
            paused = true;
            audioManager.pauseBackgroundMusic();
        }
        
        // show confirmation dialog
        if (showStopGameConfirmation()) {
            // user confirmed - go to menu
            navigateToMenu();
        } else {
            // user cancelled - resume game only if it wasn't already paused
            if (!wasAlreadyPaused) {
                paused = false;
                audioManager.resumeBackgroundMusic();
            }
            // restore keyboard focus to the game scene
            restoreKeyboardFocus();
        }
    }
    
    private boolean showStopGameConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, 
                               "Are you sure to stop the current game?", 
                               ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirm Stop Game");
        alert.setHeaderText(null);
        
        ButtonType result = alert.showAndWait().orElse(ButtonType.NO);
        return result == ButtonType.YES;
    }
    
    private void navigateToMenu() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        serverMonitor.stop();
        // stop all engines
        for (GameEngine engine : engines) {
            engine.stopGame();
        }
        serverMonitor.hideDialog();

        // stop audio and cleanup
        audioManager.exitGameplayMode();  // stops music and exits gameplay mode
        audioManager.removeObserver(this);

        if (onBackToMenu != null) {
            onBackToMenu.run();
        } else {
            System.out.println("Warning: onBackToMenu callback is null - navigation not configured");
        }
    }
    
    // Safe accessor methods to prevent IndexOutOfBoundsException
    private GameEngine getSafeEngine(int index) {
        return (engines != null && index >= 0 && index < engines.size()) ? engines.get(index) : null;
    }
    
    private Canvas getSafeCanvas(int index) {
        return (canvases != null && index >= 0 && index < canvases.size()) ? canvases.get(index) : null;
    }
    
    private void restoreKeyboardFocus() {
        // restore focus to the game scene so keyboard controls work again
        Canvas firstCanvas = getSafeCanvas(0);
        if (firstCanvas != null && firstCanvas.getScene() != null) {
            firstCanvas.getScene().getRoot().requestFocus();
        }
    }
    
    
    private void handleGameOver() {
        Canvas firstCanvas = getSafeCanvas(0);
        if (firstCanvas == null || firstCanvas.getScene() == null) {
            navigateToMenu(); // fallback if canvas unavailable
            return;
        }

        // pause background music and play game over sound
        audioManager.pauseBackgroundMusic();
        audioManager.playSoundEffect(AudioManager.SOUND_GAME_OVER);

        GameOverDialog.GameOverAction action = GameOverDialog.show(firstCanvas.getScene().getWindow());
        
        if (action == GameOverDialog.GameOverAction.PLAY_AGAIN) {
            restartGame();
        } else {
            navigateToMenu();
        }
    }
    
    
    private void restartGame() {
        // stop all current games
        for (GameEngine engine : engines) {
            engine.stopGame();
        }
        
        // reset pause state, server monitor flag, and game over tracking
        paused = false;
        serverMonitorStarted = false;
        finalResultsHandled = false;
        
        // resume background music (already in gameplay mode, just resume if paused)
        audioManager.resumeBackgroundMusic();
        
        // create new game seed for synchronized sequences
        gameSeed = System.currentTimeMillis();
        
        // restart all engines
        GameConfig config = GameConfig.getInstance();
        for (int i = 0; i < engines.size(); i++) {
            boolean isAI = (i == 0) ? 
                (config.getPlayer1Type() == GameConfig.PlayerType.AI) :
                (config.getPlayer2Type() == GameConfig.PlayerType.AI);
            boolean isExternal = (i == 0) ? 
                (config.getPlayer1Type() == GameConfig.PlayerType.EXTERNAL) :
                (config.getPlayer2Type() == GameConfig.PlayerType.EXTERNAL);
            GameEngine engine = new GameEngine(new Random(gameSeed), isAI, isExternal);
            configureEngine();
            engines.set(i, engine);
            engine.startGame();
        }
        
        // restart game loop if needed
        if (gameLoop != null) {
            gameLoop.stop();
        }
        startGameLoop();
    }

    private void setupSinglePlayerMode() {
        // setup single player with existing canvas
        canvases.add(gameCanvas);
        contexts.add(gameCanvas.getGraphicsContext2D());
        
        // create game seed for single player mode
        gameSeed = System.currentTimeMillis();
        
        // create single engine with proper configuration
        boolean isAI = (currentConfig.getPlayer1Type() == GameConfig.PlayerType.AI);
        boolean isExternal = (currentConfig.getPlayer1Type() == GameConfig.PlayerType.EXTERNAL);
        GameEngine engine = new GameEngine(new Random(gameSeed), isAI, isExternal);
        configureEngine();
        engines.add(engine);
        
        engine.startGame();
        
        drawGames();
    }
    
    private void setupTwoPlayerMode() {
        // remove default single canvas
        gameContainer.getChildren().remove(gameCanvas);
        
        // create side-by-side layout
        HBox playerContainer = new HBox(50);
        playerContainer.setAlignment(Pos.CENTER);
        
        GameConfig config = GameConfig.getInstance();
        
        // create game seed for synchronized two-player sequences
        gameSeed = System.currentTimeMillis();
        
        for (int i = 0; i < 2; i++) {
            VBox playerBox = new VBox(10);
            playerBox.setAlignment(Pos.CENTER);
            
            String playerType = (i == 0) ? config.getPlayer1Type().toString() : config.getPlayer2Type().toString();
            HBox labelContainer = new HBox(5);
            labelContainer.setAlignment(Pos.CENTER);
            Label playerLabel = new Label("Player " + (i + 1));
            playerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
            Label typeLabel = new Label("(" + playerType + ")");
            typeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            labelContainer.getChildren().addAll(playerLabel, typeLabel);
            
            // game canvas
            Canvas canvas = new Canvas(250, 500);
            canvas.getStyleClass().add("game-field");
            
            playerBox.getChildren().addAll(labelContainer, canvas);
            playerContainer.getChildren().add(playerBox);
            
            // add to collections
            canvases.add(canvas);
            contexts.add(canvas.getGraphicsContext2D());
            
            // create engine with identically seeded random
            boolean isAI = (i == 0) ? 
                (config.getPlayer1Type() == GameConfig.PlayerType.AI) :
                (config.getPlayer2Type() == GameConfig.PlayerType.AI);
            boolean isExternal = (i == 0) ? 
                (config.getPlayer1Type() == GameConfig.PlayerType.EXTERNAL) :
                (config.getPlayer2Type() == GameConfig.PlayerType.EXTERNAL);
            GameEngine engine = new GameEngine(new Random(gameSeed), isAI, isExternal);
            configureEngine(); // now just handles server monitoring
            engines.add(engine);
            
            engine.startGame();
        }
        
        gameContainer.getChildren().add(playerContainer);
        
        drawGames();
    }
    
    private void configureEngine() {
        GameConfig config = GameConfig.getInstance();
        
        // start server monitoring if ANY player is external
        if ((config.getPlayer1Type() == GameConfig.PlayerType.EXTERNAL ||
             config.getPlayer2Type() == GameConfig.PlayerType.EXTERNAL) && 
            !serverMonitorStarted) {
            startServerMonitoring();
            serverMonitorStarted = true;
        }
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!paused) {
                    updateGames(now);
                }
                drawGames();
                
                // check for game over
                checkGameOver();
            }
        };
        gameLoop.start();
    }
    
    private void updateGames(long now) {
        for (GameEngine engine : engines) {
            engine.updateGame(now);
        }
    }
    
    private void checkGameOver() {
        if (isExtendedMode && engines.size() == 2) {
            GameEngine player1Engine = getSafeEngine(0);
            GameEngine player2Engine = getSafeEngine(1);
            
            if (player1Engine == null || player2Engine == null) return;
            
            boolean player1Running = player1Engine.isGameRunning();
            boolean player2Running = player2Engine.isGameRunning();
            
            // only stop when both players are done and not already handled
            if (!player1Running && !player2Running && !finalResultsHandled) {
                finalResultsHandled = true;
                gameLoop.stop();
                Platform.runLater(() -> handleGameOver());
            }
        } else if (!isExtendedMode && !engines.isEmpty()) {
            // single-player mode
            GameEngine engine = getSafeEngine(0);
            if (engine != null && !engine.isGameRunning()) {
                gameLoop.stop();
                Platform.runLater(() -> handleGameOver());
            }
        }
    }

    private void drawGames() {
        for (int i = 0; i < engines.size() && i < contexts.size(); i++) {
            drawGame(engines.get(i), contexts.get(i), canvases.get(i));
        }
    }
    
    private void drawGame(GameEngine engine, GraphicsContext gc, Canvas canvas) {
        // clear canvas with background color
        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // draw game board cells
        for (int row = 0; row < GameBoard.BOARD_HEIGHT; row++) {
            for (int col = 0; col < GameBoard.BOARD_WIDTH; col++) {
                double x = PADDING + col * CELL_SIZE;
                double y = PADDING + row * CELL_SIZE;

                // draw empty cell background
                gc.setFill(BACKGROUND_COLOR);
                gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);

                // draw cell borders
                gc.setStroke(BORDER_COLOR);
                gc.setLineWidth(0.5);
                gc.strokeRect(x, y, CELL_SIZE, CELL_SIZE);

                // check if board cell is filled
                String boardColor = engine.getBoard().getCellColor(row, col);
                if (boardColor != null) {
                    drawCell(gc, x, y, CELL_SIZE, boardColor);
                }
            }
        }

        // draw current falling piece with smooth position
        TetrisShape currentShape = engine.getCurrentShape();
        if (currentShape != null && engine.isGameRunning()) {
            double smoothY = engine.getSmoothY();

            for (int row = 0; row < currentShape.getHeight(); row++) {
                for (int col = 0; col < currentShape.getWidth(); col++) {
                    if (currentShape.isCellFilled(row, col)) {
                        int boardCol = currentShape.getX() + col;
                        double boardRow = smoothY + row;

                        // only draw if within visible area
                        if (boardCol >= 0 && boardCol < GameBoard.BOARD_WIDTH &&
                            boardRow >= 0 && boardRow < GameBoard.BOARD_HEIGHT) {
                            double x = PADDING + boardCol * CELL_SIZE;
                            double y = PADDING + boardRow * CELL_SIZE;
                            drawCell(gc, x, y, CELL_SIZE, currentShape.getColor());
                        }
                    }
                }
            }
        }
        
        // draw pause overlay if game is paused
        if (paused) {
            drawPauseOverlay(gc, canvas);
        }
        
        // draw game over overlay if this player's game is over
        if (isExtendedMode && !engine.isGameRunning()) {
            drawGameOverOverlay(gc, canvas);
        }
    }

    private void drawCell(GraphicsContext gc, double x, double y, int size, String colorName) {
        gc.setFill(ShapeColors.getFillColor(colorName));
        gc.fillRect(x, y, size, size);

        gc.setStroke(ShapeColors.getBorderColor(colorName));
        gc.setLineWidth(1);
        gc.strokeRect(x, y, size, size);
    }
    
    private void drawPauseOverlay(GraphicsContext gc, Canvas canvas) {
        gc.setFill(Color.rgb(0, 0, 0, 0.3));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // pause text
        gc.setFill(PAUSE_TEXT_COLOR);
        gc.setFont(Font.font("Arial", 15));
        gc.setTextAlign(TextAlignment.CENTER);
        
        double textX = canvas.getWidth() / 2;
        double textY = 50;
        
        gc.fillText("Game is paused.", textX, textY);
        gc.fillText("Press P to continue.", textX, textY + 20);
    }
    
    private void drawGameOverOverlay(GraphicsContext gc, Canvas canvas) {
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        gc.setFill(Color.web("#FF6B6B"));
        gc.setFont(Font.font("Arial", 20));
        gc.setTextAlign(TextAlignment.CENTER);
        
        double textX = canvas.getWidth() / 2;
        double textY = canvas.getHeight() / 2;
        
        gc.fillText("GAME OVER", textX, textY);
    }


    public void setupKeyboardEvents(Scene scene) {
        scene.setOnKeyPressed(this::handleKeyPressed);
        scene.setOnKeyReleased(this::handleKeyReleased);

        // ensure the scene can receive keyboard focus
        scene.getRoot().setFocusTraversable(true);
        scene.getRoot().requestFocus();
    }
    
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.P) {
            if (!paused || !serverMonitor.isDialogShowing()) {
                paused = !paused;
                // pause/resume music with game state
                if (paused) {
                    audioManager.pauseBackgroundMusic();
                } else {
                    audioManager.resumeBackgroundMusic();
                }
            }
            return;
        }
        
        // Audio toggles
        if (event.getCode() == KeyCode.M) {
            currentConfig.setMusicEnabled(!currentConfig.isMusicEnabled());
            return;
        }
        
        if (event.getCode() == KeyCode.S) {
            currentConfig.setSoundEnabled(!currentConfig.isSoundEnabled());
            return;
        }
        
        if (paused) return;
        
        if (isExtendedMode) {
            handleTwoPlayerInput(event);
        } else {
            handleSinglePlayerInput(event);
        }
    }
    
    private void handleKeyReleased(KeyEvent event) {
        if (paused) return;
        
        if (isExtendedMode) {
            // player 1 fast drop release (S key)
            if (event.getCode() == KeyCode.S && currentConfig.getPlayer1Type() == GameConfig.PlayerType.HUMAN) {
                GameEngine engine = getSafeEngine(0);
                if (engine != null) {
                    engine.setFastDrop(false);
                }
            }
            
            // player 2 fast drop release (DOWN key)
            if (event.getCode() == KeyCode.DOWN && currentConfig.getPlayer2Type() == GameConfig.PlayerType.HUMAN) {
                GameEngine engine = getSafeEngine(1);
                if (engine != null) {
                    engine.setFastDrop(false);
                }
            }
        } else {
            // single-player mode: Player 1 uses DOWN key
            if (event.getCode() == KeyCode.DOWN && currentConfig.getPlayer1Type() == GameConfig.PlayerType.HUMAN) {
                GameEngine engine = getSafeEngine(0);
                if (engine != null) {
                    engine.setFastDrop(false);
                }
            }
        }
    }
    
    private void handleSinglePlayerInput(KeyEvent event) {
        GameEngine engine = getSafeEngine(0);
        
        // only allow keyboard input for HUMAN players
        if (engine == null || !engine.isGameRunning() || currentConfig.getPlayer1Type() != GameConfig.PlayerType.HUMAN) {
            return;
        }
        
        switch (event.getCode()) {
            case LEFT -> engine.moveLeft();
            case RIGHT -> engine.moveRight();
            case DOWN -> engine.setFastDrop(true);
            case UP -> engine.rotate();
        }
    }
    
    private void handleTwoPlayerInput(KeyEvent event) {
        // player 1 controls (WASD) - ONLY for HUMAN players
        if (currentConfig.getPlayer1Type() == GameConfig.PlayerType.HUMAN) {
            GameEngine p1Engine = getSafeEngine(0);
            if (p1Engine != null && p1Engine.isGameRunning()) {
                switch (event.getCode()) {
                    case A -> p1Engine.moveLeft();
                    case D -> p1Engine.moveRight();
                    case W -> p1Engine.rotate();
                    case S -> p1Engine.setFastDrop(true);
                }
            }
        }
        
        // player 2 controls (Arrow keys) - ONLY for HUMAN players
        if (currentConfig.getPlayer2Type() == GameConfig.PlayerType.HUMAN) {
            GameEngine p2Engine = getSafeEngine(1);
            if (p2Engine != null && p2Engine.isGameRunning()) {
                switch (event.getCode()) {
                    case LEFT -> p2Engine.moveLeft();
                    case RIGHT -> p2Engine.moveRight();
                    case UP -> p2Engine.rotate();
                    case DOWN -> p2Engine.setFastDrop(true);
                }
            }
        }
    }
    
    // starts background server monitoring for external player mode
    private void startServerMonitoring() {
        serverMonitor.startMonitoring(
            () -> {
                if (paused && serverMonitor.isDialogShowing()) {
                    paused = false;
                    audioManager.resumeBackgroundMusic();
                    serverMonitor.hideDialog();
                }
            },
            () -> {
                // check if any game is running before showing server dialog
                if (engines.stream().anyMatch(GameEngine::isGameRunning)) {
                    paused = true; // ensure game is paused
                    audioManager.pauseBackgroundMusic();
                    serverMonitor.showDialog(() -> {
                        navigateToMenu();
                    });
                }
            }
        );
    }
    
    // AudioObserver implementation
    @Override
    public void onMusicSettingChanged(boolean enabled) {
        updateAudioStatusDisplay();
    }
    
    @Override
    public void onSoundSettingChanged(boolean enabled) {
        updateAudioStatusDisplay();
    }

    private void updateAudioStatusDisplay() {
        if (audioStatusLabel != null) {
            String musicStatus = audioManager.isMusicEnabled() ? "ON" : "OFF";
            String soundStatus = audioManager.isSoundEnabled() ? "ON" : "OFF";
            audioStatusLabel.setText(String.format("Music: %s  Sound: %s", musicStatus, soundStatus));
        }
    }

    public static Scene getScene(Runnable onBackToMenu) {
        GameConfig config = GameConfig.getInstance();
        
        // dynamic window sizing based on mode
        int width = config.isExtendedMode() ? 700 : 400;
        int height = config.isExtendedMode() ? 660 : 630;
        
        LoadResult<GameplayScreen> result = loadSceneWithController(
            GameplayScreen.class, "gameplay.fxml", width, height);
        
        result.controller().onBackToMenu = onBackToMenu;
        result.controller().setupKeyboardEvents(result.scene());
        return result.scene();
    }
}