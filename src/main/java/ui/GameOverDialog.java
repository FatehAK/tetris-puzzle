package ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

// Dialog shown when the game is over with options to play again or exit
public class GameOverDialog {
    
    public enum GameOverAction {
        PLAY_AGAIN,
        EXIT
    }
    
    public static GameOverAction show(Window owner) {
        ButtonType playAgainButton = new ButtonType("Play Again");
        ButtonType exitButton = new ButtonType("Exit to Menu");
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION, 
                               "Game Over! Would you like to play again?", 
                               playAgainButton, exitButton);
        alert.initOwner(owner);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);
        
        ButtonType result = alert.showAndWait().orElse(exitButton);
        
        if (result == playAgainButton) {
            return GameOverAction.PLAY_AGAIN;
        } else {
            return GameOverAction.EXIT;
        }
    }
}