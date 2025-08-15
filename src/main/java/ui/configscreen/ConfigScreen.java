package ui.configscreen;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import ui.BaseScreen;

public class ConfigScreen extends BaseScreen {
    // FXML injected UI components
    @FXML private Slider widthSlider;
    @FXML private Slider heightSlider;
    @FXML private Slider levelSlider;

    // Value labels for sliders to display current values
    @FXML private Label widthValue;
    @FXML private Label heightValue;
    @FXML private Label levelValue;

    // Checkboxes for on/off settings
    @FXML private CheckBox musicCheckBox;
    @FXML private CheckBox soundCheckBox;
    @FXML private CheckBox aiCheckBox;
    @FXML private CheckBox extendCheckBox;

    // Status labels for checkboxes (display On/Off)
    @FXML private Label musicStatus;
    @FXML private Label soundStatus;
    @FXML private Label aiStatus;
    @FXML private Label extendStatus;

    // Initialize method
    public void initialize() {
        setupFieldWidthSlider();
        setupFieldHeightSlider();
        setupGameLevelSlider();
        setupCheckboxes();
    }

    // Sets up the field width slider (5-30 cells)
    private void setupFieldWidthSlider() {
        widthSlider.setMin(5);
        widthSlider.setMax(30);
        widthSlider.setValue(10);
        widthSlider.setShowTickLabels(true);
        widthSlider.setShowTickMarks(true);
        widthSlider.setMajorTickUnit(5);
        // Update value label when slider moves
        widthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            widthValue.setText(String.valueOf(newVal.intValue()));
        });
    }

    // Sets up the field height slider (15-30 cells)
    private void setupFieldHeightSlider() {
        heightSlider.setMin(15);
        heightSlider.setMax(30);
        heightSlider.setValue(20);
        heightSlider.setShowTickLabels(true);
        heightSlider.setShowTickMarks(true);
        heightSlider.setMajorTickUnit(5);
        // Update value label when slider moves
        heightSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            heightValue.setText(String.valueOf(newVal.intValue()));
        });
    }

    // Sets up the game level slider (1-10)
    private void setupGameLevelSlider() {
        levelSlider.setMin(1);
        levelSlider.setMax(10);
        levelSlider.setValue(1);
        levelSlider.setShowTickLabels(true);
        levelSlider.setShowTickMarks(true);
        levelSlider.setMajorTickUnit(1);
        // Update value label when slider moves
        levelSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            levelValue.setText(String.valueOf(newVal.intValue()));
        });
    }
    // Sets up all the checkboxes
    private void setupCheckboxes() {
        // Music checkbox
        musicCheckBox.setSelected(true);
        musicCheckBox.setOnAction(e -> {
            musicStatus.setText(musicCheckBox.isSelected() ? "On" : "Off");
        });

        // Sound effects checkbox
        soundCheckBox.setSelected(true);
        soundCheckBox.setOnAction(e -> {
            soundStatus.setText(soundCheckBox.isSelected() ? "On" : "Off");
        });

        // AI play checkbox
        aiCheckBox.setSelected(false);
        aiCheckBox.setOnAction(e -> {
            aiStatus.setText(aiCheckBox.isSelected() ? "On" : "Off");
        });

        // Extended mode checkbox
        extendCheckBox.setSelected(false);
        extendCheckBox.setOnAction(e -> {
            extendStatus.setText(extendCheckBox.isSelected() ? "On" : "Off");
        });
    }

    public static Scene getScene() {
        LoadResult<ConfigScreen> result = loadSceneWithController(ConfigScreen.class, "config.fxml", 600, 400);
        result.scene().getStylesheets().add(ConfigScreen.class.getResource("config.css").toExternalForm());
        return result.scene();
    }
}