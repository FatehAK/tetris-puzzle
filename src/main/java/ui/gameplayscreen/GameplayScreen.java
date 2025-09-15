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
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import model.GameBoard;
import model.GameCommand;
import model.GameEngine;
import model.HighScore;
import model.TetrisShape;
import ui.BaseScreen;
import ui.GameOverDialog;
import ui.highscorescreen.HighScoreScreen;
import util.HighScoreManager;
import util.ShapeColors;
import util.ServerMonitor;
import ui.configscreen.GameConfig;
import java.util.ArrayList;
import util.AudioManager;
import util.AudioObserver;
import java.util.List;
import java.util.Random;
import java.util.Optional;

// JavaFX controller for the main game screen with falling pieces
public class GameplayScreen extends BaseScreen implements AudioObserver {
    private boolean paused = false;

    // FXML components
    @FXML private Canvas gameCanvas;
    @FXML private VBox gameContainer;
    @FXML private Button backButton;
    @FXML private Label audioStatusLabel;

    // Player 1 info labels
    @FXML private Label currentLevelLabel;
    @FXML private Label linesErasedLabel;
    @FXML private Label scoreValueLabel;
    @FXML private Canvas nextTetrominoCanvas;

    @FXML private HBox root;
    @FXML private VBox infoPanel;
    @FXML private VBox infoPanelsContainer;

    // Player 2 info labels (for two players)
    private VBox playerTwoInfoPanel = null;
    private Label currentLevelLabel2;
    private Label linesErasedLabel2;
    private Label scoreValueLabel2;
    private Canvas nextTetrominoCanvas2;

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
        if (engines.isEmpty())
            return;

        // pause background music and play game over sound
        audioManager.pauseBackgroundMusic();
        audioManager.playSoundEffect(AudioManager.SOUND_GAME_OVER);

        GameEngine player1 = getAliveEngine(0);
        if (player1 == null)
            return;

        if (isExtendedMode && engines.size() == 2) {
            GameEngine player2 = getAliveEngine(1);
            if (player2 == null)
                return;

            if (player1.isGameRunning() || player2.isGameRunning())
                return; // Wait until both finish

            int score1 = player1.getScore();
            int score2 = player2.getScore();

            // Check if both scores zero
            if (score1 == 0 && score2 == 0) {
                showGameOverPrompt();
                return;
            }

            // Save only the winner's score if human
            if (score1 > score2) {
                // Player 1 wins
                if (currentConfig.getPlayer1Type() == GameConfig.PlayerType.HUMAN) {
                    promptNameAndSaveScore(player1, currentConfig.getPlayer1Name(), score1);
                } else {
                    showGameOverPrompt();
                }
            } else if (score2 > score1) {
                // Player 2 wins
                if (currentConfig.getPlayer2Type() == GameConfig.PlayerType.HUMAN) {
                    promptNameAndSaveScore(player2, currentConfig.getPlayer2Name(), score2);
                } else {
                    showGameOverPrompt();
                }
            } else {
                // Tie - no score saved
                showGameOverPrompt();
            }
        } else {
            // Single or other mode
            if (player1.isGameRunning())
                return;

            int score = player1.getScore();

            if (score == 0) {
                showGameOverPrompt();
                return;
            }

            // Only save if single player is HUMAN
            if (currentConfig.getPlayer1Type() == GameConfig.PlayerType.HUMAN) {
                promptNameAndSaveScore(player1, currentConfig.getPlayer1Name(), score);
            } else {
                showGameOverPrompt();
            }
        }
    }

    private void promptNameAndSaveScore(GameEngine engine, String defaultName, int score) {
        Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog(defaultName);
            dialog.setTitle("Game Over");
            dialog.setHeaderText("Enter your name for the leaderboard:");
            Optional<String> result = dialog.showAndWait();

            if (result.isPresent() && !result.get().trim().isEmpty()) {
                String name = result.get().trim();
                HighScore newScore = new HighScore(name, score);
                HighScoreManager.getInstance().addHighScore(newScore);
            }

            showHighScoreScene();
        });
    }

    private void showGameOverPrompt() {
        Platform.runLater(() -> {
            GameOverDialog.GameOverAction action = GameOverDialog.show(getStage());
            if (action == GameOverDialog.GameOverAction.PLAY_AGAIN) {
                restartGame();
            } else {
                navigateToMenu();
            }
        });
    }

    private void showHighScoreScene() {
        Platform.runLater(() -> {
            Scene highScoreScene = HighScoreScreen.getScene(() -> navigateToMenu());
            Stage stage = getStage();
            if (stage != null) {
                stage.setScene(highScoreScene);
            } else {
                navigateToMenu();
            }
        });
    }

    private GameEngine getAliveEngine(int index) {
        if (index >= 0 && index < engines.size()) {
            return engines.get(index);
        }
        return null;
    }

    private Stage getStage() {
        return gameCanvas != null && gameCanvas.getScene() != null
                ? (Stage) gameCanvas.getScene().getWindow()
                : null;
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

        // Create player label container for single player
        HBox labelContainer = new HBox(5);
        labelContainer.setAlignment(Pos.CENTER);
        
        Label playerLabel = new Label(currentConfig.getPlayer1Name());
        playerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label typeLabel = new Label("(" + currentConfig.getPlayer1Type().toString() + ")");
        typeLabel.setStyle("-fx-text-fill: #ffed4e; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        labelContainer.getChildren().addAll(playerLabel, typeLabel);
        
        int audioIndex = -1;
        for (int i = 0; i < gameContainer.getChildren().size(); i++) {
            if (gameContainer.getChildren().get(i).getId() != null && 
                gameContainer.getChildren().get(i).getId().equals("audioStatusLabel")) {
                audioIndex = i;
                break;
            }
        }
        
        if (audioIndex != -1) {
            // insert after audio status label
            if (!gameContainer.getChildren().contains(labelContainer)) {
                gameContainer.getChildren().add(audioIndex + 1, labelContainer);
            }
            if (!gameContainer.getChildren().contains(gameCanvas)) {
                gameContainer.getChildren().add(audioIndex + 2, gameCanvas);
            }
        }

        // remove additional info panels for player 2
        if (infoPanelsContainer.getChildren().size() > 1) {
            infoPanelsContainer.getChildren().remove(1, infoPanelsContainer.getChildren().size());
        }

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

        // Ensure Player 1 info panel is present
        if (!infoPanelsContainer.getChildren().contains(infoPanel)) {
            infoPanelsContainer.getChildren().add(infoPanel);
        }

        // add Player 2 info panel dynamically
        addPlayerTwoInfoPanel();

        for (int i = 0; i < 2; i++) {
            VBox playerBox = new VBox(10);
            playerBox.setAlignment(Pos.CENTER);

            String playerType = (i == 0) ? config.getPlayer1Type().toString() : config.getPlayer2Type().toString();
            HBox labelContainer = new HBox(5);
            labelContainer.setAlignment(Pos.CENTER);
            Label playerLabel = new Label("Player " + (i + 1));
            playerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
            Label typeLabel = new Label("(" + playerType + ")");
            // set color based on player: yellow for Player 1, blue for Player 2
            String typeColor = (i == 0) ? "#ffed4e" : "#74b9ff";
            typeLabel.setStyle("-fx-text-fill: " + typeColor + "; -fx-font-size: 14px; -fx-font-weight: bold;");
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

        int buttonIndex = gameContainer.getChildren().size() - 1;
        gameContainer.getChildren().add(buttonIndex, playerContainer);

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

    private void addPlayerTwoInfoPanel() {
        if (playerTwoInfoPanel != null) return; // already created

        playerTwoInfoPanel = new VBox(10);
        playerTwoInfoPanel.setMaxWidth(130);
        playerTwoInfoPanel.setMinWidth(130);
        playerTwoInfoPanel.getStyleClass().add("info-panel");
        playerTwoInfoPanel.setStyle("-fx-border-color: #74b9ff;");
        playerTwoInfoPanel.setAlignment(Pos.TOP_CENTER);

        // game progress section
        VBox progressSection = new VBox(5);
        progressSection.getStyleClass().add("info-section");
        
        currentLevelLabel2 = createInfoRow(progressSection, "Level:");
        linesErasedLabel2 = createInfoRow(progressSection, "Lines:");
        scoreValueLabel2 = createInfoRow(progressSection, "Score:");
        
        ((HBox)progressSection.getChildren().get(0)).getChildren().get(0).setStyle("-fx-text-fill: #74b9ff;");
        ((HBox)progressSection.getChildren().get(1)).getChildren().get(0).setStyle("-fx-text-fill: #74b9ff;");
        ((HBox)progressSection.getChildren().get(2)).getChildren().get(0).setStyle("-fx-text-fill: #74b9ff;");
        
        playerTwoInfoPanel.getChildren().add(progressSection);

        // next piece section
        VBox nextSection = new VBox(5);
        nextSection.getStyleClass().add("info-section");
        nextSection.setAlignment(Pos.CENTER);
        
        Label nextLabel = new Label("Next Piece");
        nextLabel.getStyleClass().add("next-piece-label");
        nextSection.getChildren().add(nextLabel);

        nextTetrominoCanvas2 = new Canvas(60, 60);
        nextTetrominoCanvas2.getStyleClass().add("next-tetromino-canvas");
        nextSection.getChildren().add(nextTetrominoCanvas2);
        
        playerTwoInfoPanel.getChildren().add(nextSection);
        infoPanelsContainer.getChildren().add(playerTwoInfoPanel);
    }

    private Label createInfoRow(VBox parent, String labelText) {
        Label valueLabel = new Label();
        valueLabel.getStyleClass().add("info-value");

        HBox hbox = new HBox(8);
        Label label = new Label(labelText);
        label.getStyleClass().add("info-label");

        hbox.getChildren().addAll(label, valueLabel);
        parent.getChildren().add(hbox);
        return valueLabel;
    }

    private void updateGameInfo() {
        GameEngine engine1 = getSafeEngine(0);
        if (engine1 == null) return;

        currentLevelLabel.setText(String.valueOf(engine1.getCurrentLevel()));
        linesErasedLabel.setText(String.valueOf(engine1.getLinesErased()));
        scoreValueLabel.setText(String.valueOf(engine1.getScore()));

        drawNextTetromino(engine1.getNextShape(), nextTetrominoCanvas);

        if (playerTwoInfoPanel != null) {
            GameEngine engine2 = getSafeEngine(1);
            if (engine2 != null) {
                currentLevelLabel2.setText(String.valueOf(engine2.getCurrentLevel()));
                linesErasedLabel2.setText(String.valueOf(engine2.getLinesErased()));
                scoreValueLabel2.setText(String.valueOf(engine2.getScore()));

                drawNextTetromino(engine2.getNextShape(), nextTetrominoCanvas2);
            }
        }
    }

    private void drawNextTetromino(TetrisShape shape, Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        if (shape == null) return;
        int blockSize = 15;
        for (int r = 0; r < shape.getHeight(); r++) {
            for (int c = 0; c < shape.getWidth(); c++) {
                if (shape.isCellFilled(r, c)) {
                    gc.setFill(ShapeColors.getFillColor(shape.getColor()));
                    double x = 12 + c * blockSize;
                    double y = 12 + r * blockSize;
                    gc.fillRect(x, y, blockSize, blockSize);
                    gc.setStroke(ShapeColors.getBorderColor(shape.getColor()));
                    gc.strokeRect(x, y, blockSize, blockSize);
                }
            }
        }
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!paused) {
                    updateGames(now);
                    updateGameInfo();
                }
                drawGames();
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
            // player 1 fast drop release (F key)
            if (event.getCode() == KeyCode.F && currentConfig.getPlayer1Type() == GameConfig.PlayerType.HUMAN) {
                GameEngine engine = getSafeEngine(0);
                if (engine != null) {
                    engine.setFastDropEnabled(false);
                }
            }

            // player 2 fast drop release (DOWN key)
            if (event.getCode() == KeyCode.DOWN && currentConfig.getPlayer2Type() == GameConfig.PlayerType.HUMAN) {
                GameEngine engine = getSafeEngine(1);
                if (engine != null) {
                    engine.setFastDropEnabled(false);
                }
            }
        } else {
            // single-player mode: Player 1 uses DOWN key
            if (event.getCode() == KeyCode.DOWN && currentConfig.getPlayer1Type() == GameConfig.PlayerType.HUMAN) {
                GameEngine engine = getSafeEngine(0);
                if (engine != null) {
                    engine.setFastDropEnabled(false);
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
            case LEFT -> engine.executeCommand(GameCommand.moveLeft());
            case RIGHT -> engine.executeCommand(GameCommand.moveRight());
            case DOWN -> engine.executeCommand(GameCommand.setFastDrop(true));
            case UP -> engine.executeCommand(GameCommand.rotate());
        }
    }

    private void handleTwoPlayerInput(KeyEvent event) {
        // player 1 controls (RDFG) - ONLY for HUMAN players
        if (currentConfig.getPlayer1Type() == GameConfig.PlayerType.HUMAN) {
            GameEngine p1Engine = getSafeEngine(0);
            if (p1Engine != null && p1Engine.isGameRunning()) {
                switch (event.getCode()) {
                    case D -> p1Engine.executeCommand(GameCommand.moveLeft());
                    case G -> p1Engine.executeCommand(GameCommand.moveRight());
                    case R -> p1Engine.executeCommand(GameCommand.rotate());
                    case F -> p1Engine.executeCommand(GameCommand.setFastDrop(true));
                }
            }
        }

        // player 2 controls (Arrow keys) - ONLY for HUMAN players
        if (currentConfig.getPlayer2Type() == GameConfig.PlayerType.HUMAN) {
            GameEngine p2Engine = getSafeEngine(1);
            if (p2Engine != null && p2Engine.isGameRunning()) {
                switch (event.getCode()) {
                    case LEFT -> p2Engine.executeCommand(GameCommand.moveLeft());
                    case RIGHT -> p2Engine.executeCommand(GameCommand.moveRight());
                    case UP -> p2Engine.executeCommand(GameCommand.rotate());
                    case DOWN -> p2Engine.executeCommand(GameCommand.setFastDrop(true));
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

        // Constants from GameplayScreen
        final int CELL_SIZE = 25;
        final int UI_WIDTH_MARGIN = 250;   // Space for info panels and controls
        final int UI_HEIGHT_MARGIN = 260;  // Space for top and bottom UI elements
        final int PLAYER_SPACING = 50;     // Space between two player fields

        // Get current field dimensions from config
        int fieldWidth = config.getFieldWidth();
        int fieldHeight = config.getFieldHeight();

        // Calculate game field dimensions in pixels
        int gameFieldWidth = fieldWidth * CELL_SIZE;
        int gameFieldHeight = fieldHeight * CELL_SIZE;

        // Calculate total window dimensions
        int windowWidth;
        int windowHeight = gameFieldHeight + UI_HEIGHT_MARGIN;

        if (config.isExtendedMode()) {
            // Two-player mode: two fields side-by-side with spacing
            windowWidth = (gameFieldWidth * 2) + PLAYER_SPACING + UI_WIDTH_MARGIN;
        } else {
            // Single player mode: one field plus UI space
            windowWidth = gameFieldWidth + UI_WIDTH_MARGIN;
        }

        // Ensure minimum window size for usability
        windowWidth = Math.max(windowWidth, 400);
        windowHeight = Math.max(windowHeight, 650);

        // Debug output for assignment demonstration
        System.out.println("Dynamic window sizing:");
        System.out.println("  Field dimensions: " + fieldWidth + "x" + fieldHeight + " cells");
        System.out.println("  Game field size: " + gameFieldWidth + "x" + gameFieldHeight + " pixels");
        System.out.println("  Extended mode: " + config.isExtendedMode());
        System.out.println("  Final window size: " + windowWidth + "x" + windowHeight + " pixels");

        LoadResult<GameplayScreen> result = loadSceneWithController(
                GameplayScreen.class, "gameplay.fxml", windowWidth, windowHeight);

        result.controller().onBackToMenu = onBackToMenu;
        result.controller().setupKeyboardEvents(result.scene());
        return result.scene();
    }
}