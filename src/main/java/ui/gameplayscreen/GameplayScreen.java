package ui.gameplayscreen;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

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