package ui.configscreen;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import ui.BaseScreen;
import util.GameConfig;

// csontroller for the configuration screen where users can set game options
public class ConfigScreen extends BaseScreen {
    // FXML injected UI components
    @FXML private Slider widthSlider;
    @FXML private Slider heightSlider;
    @FXML private Slider levelSlider;

    // value labels for sliders to display current values
    @FXML private Label widthValue;
    @FXML private Label heightValue;
    @FXML private Label levelValue;

    // checkboxes for on/off settings
    @FXML private CheckBox musicCheckBox;
    @FXML private CheckBox soundCheckBox;
    @FXML private CheckBox extendCheckBox;

    // radio buttons for player types
    @FXML private RadioButton playerOneHuman;
    @FXML private RadioButton playerOneAI;
    @FXML private RadioButton playerOneExternal;
    @FXML private RadioButton playerTwoHuman;
    @FXML private RadioButton playerTwoAI;
    @FXML private RadioButton playerTwoExternal;

    // status labels for checkboxes (display On/Off)
    @FXML private Label musicStatus;
    @FXML private Label soundStatus;
    @FXML private Label extendStatus;

    // back button
    @FXML private Button backButton;
    
    private Runnable onBack;
    private GameConfig gameConfig;

    // initialize method
    public void initialize() {
        gameConfig = GameConfig.getInstance();
        loadExistingSettings();
        setupFieldWidthSlider();
        setupFieldHeightSlider();
        setupGameLevelSlider();
        setupCheckboxes();
        setupRadioButtons();
        setupBackButton();
    }
    
    // load existing settings from game config
    private void loadExistingSettings() {
        widthSlider.setValue(gameConfig.getFieldWidth());
        heightSlider.setValue(gameConfig.getFieldHeight());
        levelSlider.setValue(gameConfig.getGameLevel());
        
        musicCheckBox.setSelected(gameConfig.isMusicEnabled());
        soundCheckBox.setSelected(gameConfig.isSoundEnabled());
        extendCheckBox.setSelected(gameConfig.isExtendedMode());
        
        // set player one type based on AI setting
        if (gameConfig.isAiEnabled()) {
            playerOneAI.setSelected(true);
        } else {
            playerOneHuman.setSelected(true);
        }
        // player two defaults to Human (dummy for now)
        playerTwoHuman.setSelected(true);
        
        updateStatusLabels();
    }
    
    // update status labels to match current settings
    private void updateStatusLabels() {
        widthValue.setText(String.valueOf(gameConfig.getFieldWidth()));
        heightValue.setText(String.valueOf(gameConfig.getFieldHeight()));
        levelValue.setText(String.valueOf(gameConfig.getGameLevel()));
        
        musicStatus.setText(gameConfig.isMusicEnabled() ? "On" : "Off");
        soundStatus.setText(gameConfig.isSoundEnabled() ? "On" : "Off");
        extendStatus.setText(gameConfig.isExtendedMode() ? "On" : "Off");
    }

    // sets up the field width slider (5-15 cells)
    private void setupFieldWidthSlider() {
        widthSlider.setMin(5);
        widthSlider.setMax(15);
        widthSlider.setValue(10);
        widthSlider.setShowTickLabels(true);
        widthSlider.setShowTickMarks(true);
        widthSlider.setMajorTickUnit(1);
        // update value label when slider moves
        widthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int value = newVal.intValue();
            widthValue.setText(String.valueOf(value));
            gameConfig.setFieldWidth(value);
        });
    }

    // sets up the field height slider (15-30 cells)
    private void setupFieldHeightSlider() {
        heightSlider.setMin(15);
        heightSlider.setMax(30);
        heightSlider.setValue(20);
        heightSlider.setShowTickLabels(true);
        heightSlider.setShowTickMarks(true);
        heightSlider.setMajorTickUnit(1);
        // update value label when slider moves
        heightSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int value = newVal.intValue();
            heightValue.setText(String.valueOf(value));
            gameConfig.setFieldHeight(value);
        });
    }

    // sets up the game level slider (1-10)
    private void setupGameLevelSlider() {
        levelSlider.setMin(1);
        levelSlider.setMax(10);
        levelSlider.setValue(1);
        levelSlider.setShowTickLabels(true);
        levelSlider.setShowTickMarks(true);
        levelSlider.setMajorTickUnit(1);
        // update value label when slider moves
        levelSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int value = newVal.intValue();
            levelValue.setText(String.valueOf(value));
            gameConfig.setGameLevel(value);
        });
    }
    // sets up all the checkboxes
    private void setupCheckboxes() {
        // music checkbox
        musicCheckBox.setOnAction(e -> {
            boolean selected = musicCheckBox.isSelected();
            musicStatus.setText(selected ? "On" : "Off");
            gameConfig.setMusicEnabled(selected);
        });

        // sound effects checkbox
        soundCheckBox.setOnAction(e -> {
            boolean selected = soundCheckBox.isSelected();
            soundStatus.setText(selected ? "On" : "Off");
            gameConfig.setSoundEnabled(selected);
        });

        // extended mode checkbox
        extendCheckBox.setOnAction(e -> {
            boolean selected = extendCheckBox.isSelected();
            extendStatus.setText(selected ? "On" : "Off");
            gameConfig.setExtendedMode(selected);
            updatePlayerTwoRadioButtons(selected);
        });
    }
    
    // sets up radio button groups for player types
    private void setupRadioButtons() {
        // create toggle groups
        // toggle groups for radio buttons
        ToggleGroup playerOneGroup = new ToggleGroup();
        ToggleGroup playerTwoGroup = new ToggleGroup();
        
        // assign radio buttons to groups
        playerOneHuman.setToggleGroup(playerOneGroup);
        playerOneAI.setToggleGroup(playerOneGroup);
        playerOneExternal.setToggleGroup(playerOneGroup);
        
        playerTwoHuman.setToggleGroup(playerTwoGroup);
        playerTwoAI.setToggleGroup(playerTwoGroup);
        playerTwoExternal.setToggleGroup(playerTwoGroup);
        
        // add listener for player one type changes (only AI matters for game config)
        playerOneGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            gameConfig.setAiEnabled(newToggle == playerOneAI);
        });
        
        // player two group listener (dummy - no functionality yet)
        playerTwoGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            // Placeholder for future player two functionality
        });
        
        // initialize player two radio buttons state based on extended mode
        updatePlayerTwoRadioButtons(gameConfig.isExtendedMode());
    }
    
    // enable or disable player two radio buttons based on extended mode
    private void updatePlayerTwoRadioButtons(boolean enabled) {
        playerTwoHuman.setDisable(!enabled);
        playerTwoAI.setDisable(!enabled);
        playerTwoExternal.setDisable(!enabled);
    }
    
    // sets up the back button
    private void setupBackButton() {
        if (backButton != null) {
            backButton.setOnAction(e -> {
                if (onBack != null) {
                    onBack.run();
                }
            });
        }
    }

    public static Scene getScene(Runnable onBack) {
        LoadResult<ConfigScreen> result = loadSceneWithController(ConfigScreen.class, "config.fxml", 700, 640);
        result.controller().onBack = onBack;
        return result.scene();
    }
}