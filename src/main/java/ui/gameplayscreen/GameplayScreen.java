package ui.gameplayscreen;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import model.GameBoard;
import model.GameEngine;
import model.TetrisShape;
import util.ShapeColors;

// JavaFX controller for the main game screen with falling pieces
public class GameplayScreen {

    @FXML
    private Canvas gameCanvas;
    
    @FXML
    private Button backButton;
    
    private GraphicsContext gc;
    private GameEngine gameEngine;
    private AnimationTimer gameLoop;
    
    private static final int CELL_SIZE = 25;
    private static final int PADDING = 0;
    private static final Color BACKGROUND_COLOR = Color.web("#111111");
    private static final Color BORDER_COLOR = Color.web("#333333");

    public void initialize() {
        initializeCanvas();
        backButton.setOnAction(event -> onBackButtonClicked());
        initializeGame();
        startGameLoop();
    }
    
    private void onBackButtonClicked() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (gameEngine != null) {
            gameEngine.stopGame();
        }
        System.out.println("Back button clicked");
    }
    
    private void initializeCanvas() {
        gc = gameCanvas.getGraphicsContext2D();
        // initial canvas setup - drawGame will be called after gameEngine is initialized
    }
    
    private void initializeGame() {
        gameEngine = new GameEngine();
        gameEngine.startGame();
        drawGame(); // initial draw after game engine is ready
    }
    
    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameEngine.updateGame(now)) {
                    drawGame();
                }
                
                if (!gameEngine.isGameRunning()) {
                    System.out.println("Game Over!");
                    gameLoop.stop(); // game over
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
    

    public void setupKeyboardEvents(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (gameEngine != null && gameEngine.isGameRunning()) {
                switch (event.getCode()) {
                    case LEFT -> gameEngine.movePieceLeft();
                    case RIGHT -> gameEngine.movePieceRight();
                    case DOWN -> gameEngine.setFastDropEnabled(true);
                    case UP -> gameEngine.rotatePiece();
                }
            }
        });
        
        scene.setOnKeyReleased(event -> {
            if (gameEngine != null && event.getCode() == javafx.scene.input.KeyCode.DOWN) {
                gameEngine.setFastDropEnabled(false);
            }
        });
        
        // ensure the scene can receive keyboard focus
        scene.getRoot().setFocusTraversable(true);
        scene.getRoot().requestFocus();
    }
    
    public static Scene getScene() {
        try {
            FXMLLoader loader = new FXMLLoader(GameplayScreen.class.getResource("gameplay.fxml"));
            Parent root = loader.load();
            GameplayScreen controller = loader.getController();
            
            Scene scene = new Scene(root, 400, 600);
            controller.setupKeyboardEvents(scene);
            
            return scene;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load gameplay screen", e);
        }
    }
}