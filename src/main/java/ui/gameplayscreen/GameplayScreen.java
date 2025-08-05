package ui.gameplayscreen;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import model.TetrisShape;

public class GameplayScreen {

    @FXML
    private GridPane gameField;
    
    @FXML
    private Button backButton;
    
    private static final int FIELD_WIDTH = 10;
    private static final int FIELD_HEIGHT = 20;
    private Region[][] cells;

    public void initialize() {
        initializeGameField();
        backButton.setOnAction(event -> onBackButtonClicked());
        demonstrateShapes();
    }
    
    private void onBackButtonClicked() {
        System.out.println("Back button clicked");
    }
    
    private void initializeGameField() {
        cells = new Region[FIELD_HEIGHT][FIELD_WIDTH];
        
        for (int row = 0; row < FIELD_HEIGHT; row++) {
            for (int col = 0; col < FIELD_WIDTH; col++) {
                Region cell = new Region();
                cell.getStyleClass().add("game-cell");
                cells[row][col] = cell;
                gameField.add(cell, col, row);
            }
        }
    }
    
    private void demonstrateShapes() {
        // Place all 5 shapes on the field
        placeShape(new TetrisShape(TetrisShape.ShapeType.I), 1, 1);   // I-piece: 4 cells wide
        placeShape(new TetrisShape(TetrisShape.ShapeType.O), 6, 1);   // O-piece: 2x2 square
        placeShape(new TetrisShape(TetrisShape.ShapeType.T), 3, 4);   // T-piece: 3 cells wide
        placeShape(new TetrisShape(TetrisShape.ShapeType.L), 1, 7);   // L-piece: 2 cells wide, 3 tall
        placeShape(new TetrisShape(TetrisShape.ShapeType.Z), 6, 7);   // Z-piece: 3 cells wide, 2 tall
    }
    
    private void placeShape(TetrisShape shape, int startX, int startY) {
        for (int row = 0; row < shape.getHeight(); row++) {
            for (int col = 0; col < shape.getWidth(); col++) {
                if (shape.isCellFilled(row, col)) {
                    int fieldRow = startY + row;
                    int fieldCol = startX + col;
                    
                    if (fieldRow >= 0 && fieldRow < FIELD_HEIGHT && 
                        fieldCol >= 0 && fieldCol < FIELD_WIDTH) {
                        Region cell = cells[fieldRow][fieldCol];
                        cell.getStyleClass().removeAll("game-cell");
                        cell.getStyleClass().addAll("game-cell", "shape-" + shape.getColor());
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