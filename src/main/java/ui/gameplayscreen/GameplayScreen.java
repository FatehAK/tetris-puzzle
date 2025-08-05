package ui.gameplayscreen;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import model.GameBoard;
import model.GameEngine;
import model.TetrisShape;

// JavaFX controller for the main game screen with falling pieces
public class GameplayScreen {

    @FXML
    private GridPane gameField;
    
    @FXML
    private Button backButton;
    
    private Region[][] cells;
    private GameEngine gameEngine;
    private AnimationTimer gameLoop;

    public void initialize() {
        initializeGameField();
        backButton.setOnAction(event -> onBackButtonClicked());
        initializeGame();
        startGameLoop();
    }
    
    private void onBackButtonClicked() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        System.out.println("Back button clicked");
    }
    
    private void initializeGameField() {
        cells = new Region[GameBoard.BOARD_HEIGHT][GameBoard.BOARD_WIDTH];
        
        for (int row = 0; row < GameBoard.BOARD_HEIGHT; row++) {
            for (int col = 0; col < GameBoard.BOARD_WIDTH; col++) {
                Region cell = new Region();
                cell.getStyleClass().add("game-cell");
                cells[row][col] = cell;
                gameField.add(cell, col, row);
            }
        }
    }
    
    private void initializeGame() {
        gameEngine = new GameEngine();
        gameEngine.startGame();
    }
    
    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameEngine.updateGame(now)) {
                    updateDisplay();
                }
                
                if (!gameEngine.isGameRunning()) {
                    gameLoop.stop(); // game over
                }
            }
        };
        gameLoop.start();
    }
    
    private void updateDisplay() {
        TetrisShape currentPiece = gameEngine.getCurrentPiece();
        
        for (int row = 0; row < GameBoard.BOARD_HEIGHT; row++) {
            for (int col = 0; col < GameBoard.BOARD_WIDTH; col++) {
                Region cell = cells[row][col];
                cell.getStyleClass().clear();
                cell.getStyleClass().add("game-cell");
                
                // check if current piece occupies this cell
                boolean isCurrentPiece = false;
                if (currentPiece != null && gameEngine.isGameRunning()) {
                    int relativeRow = row - currentPiece.getY();
                    int relativeCol = col - currentPiece.getX();
                    
                    if (relativeRow >= 0 && relativeRow < currentPiece.getHeight() &&
                        relativeCol >= 0 && relativeCol < currentPiece.getWidth() &&
                        currentPiece.isCellFilled(relativeRow, relativeCol)) {
                        cell.getStyleClass().add("shape-" + currentPiece.getColor());
                        isCurrentPiece = true;
                    }
                }
                
                // if not current piece, check board for placed pieces
                if (!isCurrentPiece) {
                    String boardColor = gameEngine.getBoard().getCellColor(row, col);
                    if (boardColor != null) {
                        cell.getStyleClass().add("shape-" + boardColor);
                    }
                }
            }
        }
    }
    

    public static Scene getScene() {
        try {
            FXMLLoader loader = new FXMLLoader(GameplayScreen.class.getResource("gameplay.fxml"));
            Parent root = loader.load();

            return new Scene(root, 400, 600);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load gameplay screen", e);
        }
    }
}