package ui.gameplayscreen;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import model.GameBoard;
import model.GameEngine;
import model.InputController;
import model.TetrisShape;
import ui.BaseScreen;
import ui.GameOverDialog;
import util.ShapeColors;
import util.GameConfig;

// JavaFX controller for the main game screen with falling pieces
public class GameplayScreen extends BaseScreen {
    private boolean paused = false;

    @FXML
    private Canvas gameCanvas;

    @FXML
    private Button backButton;

    private GraphicsContext gc;
    private GameEngine gameEngine;
    private InputController inputController;
    private AnimationTimer gameLoop;

    private static final int CELL_SIZE = 25;
    private static final int PADDING = 0;
    private static final Color BACKGROUND_COLOR = Color.web("#111111");
    private static final Color BORDER_COLOR = Color.web("#333333");
    private static final Color PAUSE_TEXT_COLOR = Color.web("#FFFFFF");
    private Runnable onBackToMenu;

    public void initialize() {
        initializeCanvas();
        backButton.setOnAction(event -> onBackButtonClicked());
        initializeGame();
        startGameLoop();
    }

    private void onBackButtonClicked() {
        // if game is over, directly go back to menu
        if (gameEngine == null || !gameEngine.isGameRunning()) {
            navigateToMenu();
            return;
        }
        
        boolean wasAlreadyPaused = paused;
        
        // pause game if not already paused
        if (!paused) {
            paused = true;
        }
        
        // show confirmation dialog
        if (showStopGameConfirmation()) {
            // user confirmed - go to menu
            navigateToMenu();
        } else {
            // user cancelled - resume game only if it wasn't already paused
            if (!wasAlreadyPaused) {
                paused = false;
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
        if (gameEngine != null) {
            gameEngine.stopGame();
        }
        if (onBackToMenu != null) {
            onBackToMenu.run();
        } else {
            System.out.println("Warning: onBackToMenu callback is null - navigation not configured");
        }
    }
    
    private void restoreKeyboardFocus() {
        // restore focus to the game canvas so keyboard controls work again
        if (gameCanvas != null && gameCanvas.getScene() != null) {
            gameCanvas.getScene().getRoot().requestFocus();
        }
    }
    
    private void handleGameOver() {
        GameOverDialog.GameOverAction action = GameOverDialog.show(gameCanvas.getScene().getWindow());
        
        if (action == GameOverDialog.GameOverAction.PLAY_AGAIN) {
            // restart the game
            restartGame();
        } else {
            // exit to menu
            navigateToMenu();
        }
    }
    
    private void restartGame() {
        // stop current game if running
        if (gameEngine != null) {
            gameEngine.stopGame();
        }
        
        // reset pause state
        paused = false;
        
        // initialize new game
        initializeGame();
        startGameLoop();
    }

    private void initializeCanvas() {
        gc = gameCanvas.getGraphicsContext2D();
        // initial canvas setup - drawGame will be called after gameEngine is initialized
    }

    private void initializeGame() {
        gameEngine = new GameEngine();
        inputController = gameEngine; // use interface for input controls
        
        // apply AI setting from configuration
        GameConfig config = GameConfig.getInstance();
        gameEngine.setAIEnabled(config.isAiEnabled());
        
        gameEngine.startGame();
        drawGame(); // initial draw after game engine is ready
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!paused) {
                    if (gameEngine.updateGame(now)) {
                        drawGame();
                    }
                } else {
                    drawGame(); // redraw to show pause overlay
                }

                if (!gameEngine.isGameRunning()) {
                    gameLoop.stop(); // game over
                    // defer dialog showing to avoid IllegalStateException
                    Platform.runLater(() -> handleGameOver());
                }
            }
        };
        gameLoop.start();
    }

    private void drawGame() {
        // clear canvas with background color
        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

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
                String boardColor = gameEngine.getBoard().getCellColor(row, col);
                if (boardColor != null) {
                    drawCell(x, y, CELL_SIZE, boardColor);
                }
            }
        }

        // draw current falling piece with smooth position
        TetrisShape currentPiece = gameEngine.getCurrentPiece();
        if (currentPiece != null && gameEngine.isGameRunning()) {
            double smoothY = gameEngine.getSmoothY();

            for (int row = 0; row < currentPiece.getHeight(); row++) {
                for (int col = 0; col < currentPiece.getWidth(); col++) {
                    if (currentPiece.isCellFilled(row, col)) {
                        int boardCol = currentPiece.getX() + col;
                        double boardRow = smoothY + row;

                        // only draw if within visible area
                        if (boardCol >= 0 && boardCol < GameBoard.BOARD_WIDTH &&
                            boardRow >= 0 && boardRow < GameBoard.BOARD_HEIGHT) {
                            double x = PADDING + boardCol * CELL_SIZE;
                            double y = PADDING + boardRow * CELL_SIZE;
                            drawCell(x, y, CELL_SIZE, currentPiece.getColor());
                        }
                    }
                }
            }
        }
        
        // draw pause overlay if game is paused
        if (paused) {
            drawPauseOverlay();
        }
    }

    private void drawCell(double x, double y, int size, String colorName) {
        // fill the cell
        gc.setFill(ShapeColors.getFillColor(colorName));
        gc.fillRect(x, y, size, size);

        // draw border
        gc.setStroke(ShapeColors.getBorderColor(colorName));
        gc.setLineWidth(1);
        gc.strokeRect(x, y, size, size);
    }
    
    private void drawPauseOverlay() {
        // semi-transparent overlay
        gc.setFill(Color.rgb(0, 0, 0, 0.3));
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        
        // pause text
        gc.setFill(PAUSE_TEXT_COLOR);
        gc.setFont(Font.font("Arial", 15));
        gc.setTextAlign(TextAlignment.CENTER);
        
        double textX = gameCanvas.getWidth() / 2;
        double textY = 50; // position at top with padding
        
        // split text into two lines
        gc.fillText("Game is paused.", textX, textY);
        gc.fillText("Press P to continue.", textX, textY + 20);
    }


    public void setupKeyboardEvents(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.P) {
                paused = !paused;
                return;
            }
            if (inputController != null && gameEngine != null && gameEngine.isGameRunning() && !paused && !gameEngine.isAIEnabled()) {
                switch (event.getCode()) {
                    case LEFT -> inputController.moveLeft();
                    case RIGHT -> inputController.moveRight();
                    case DOWN -> inputController.setFastDrop(true);
                    case UP -> inputController.rotate();
                }
            }
        });

        scene.setOnKeyReleased(event -> {
            if (inputController != null && event.getCode() == javafx.scene.input.KeyCode.DOWN && !gameEngine.isAIEnabled()) {
                inputController.setFastDrop(false);
            }
        });

        // ensure the scene can receive keyboard focus
        scene.getRoot().setFocusTraversable(true);
        scene.getRoot().requestFocus();
    }

    public static Scene getScene(Runnable onBackToMenu) {
        LoadResult<GameplayScreen> result = loadSceneWithController(GameplayScreen.class, "gameplay.fxml", 400, 600);
        result.controller().onBackToMenu = onBackToMenu;
        result.controller().setupKeyboardEvents(result.scene());
        return result.scene();
    }
}