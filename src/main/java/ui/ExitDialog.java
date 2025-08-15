package ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

public class ExitDialog {
    public static boolean show(Window owner) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to exit?", ButtonType.YES, ButtonType.NO);
        alert.initOwner(owner);
        alert.setTitle("Confirm Exit");
        alert.setHeaderText(null);

        ButtonType result = alert.showAndWait().orElse(ButtonType.NO);
        return result == ButtonType.YES;
    }
}
