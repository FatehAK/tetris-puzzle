package ui.configscreen;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import ui.BaseScreen;

// Controller for the configuration screen where users can set game options
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

    @FXML private Label musicStatus;
    @FXML private Label soundStatus;
    @FXML private Label extendStatus;

    // back button
    @FXML private Button backButton;

    private Runnable onBack;
    private GameConfig gameConfig;


    public void initialize() {
        gameConfig = GameConfig.getInstance(); // Already loads from file automatically
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

        // set player types based on config
        switch (gameConfig.getPlayer1Type()) {
            case HUMAN -> playerOneHuman.setSelected(true);
            case AI -> playerOneAI.setSelected(true);
            case EXTERNAL -> playerOneExternal.setSelected(true);
        }

        switch (gameConfig.getPlayer2Type()) {
            case HUMAN -> playerTwoHuman.setSelected(true);
            case AI -> playerTwoAI.setSelected(true);
            case EXTERNAL -> playerTwoExternal.setSelected(true);
        }

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
        widthSlider.setShowTickLabels(true);
        widthSlider.setShowTickMarks(true);
        widthSlider.setMajorTickUnit(1);
        widthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int value = newVal.intValue();
            widthValue.setText(String.valueOf(value));
            gameConfig.setFieldWidth(value);
            gameConfig.saveConfigurationToFile();
        });
    }

    // sets up the field height slider (15-30 cells)
    private void setupFieldHeightSlider() {
        heightSlider.setMin(15);
        heightSlider.setMax(30);
        // setValue is set by loadExistingSettings()
        heightSlider.setShowTickLabels(true);
        heightSlider.setShowTickMarks(true);
        heightSlider.setMajorTickUnit(1);
        heightSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int value = newVal.intValue();
            heightValue.setText(String.valueOf(value));
            gameConfig.setFieldHeight(value);
            gameConfig.saveConfigurationToFile();
        });
    }

    // sets up the game level slider (1-10)
    private void setupGameLevelSlider() {
        levelSlider.setMin(1);
        levelSlider.setMax(10);
        // setValue is set by loadExistingSettings()
        levelSlider.setShowTickLabels(true);
        levelSlider.setShowTickMarks(true);
        levelSlider.setMajorTickUnit(1);
        levelSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int value = newVal.intValue();
            levelValue.setText(String.valueOf(value));
            gameConfig.setGameLevel(value);
            gameConfig.saveConfigurationToFile();
        });
    }

    // sets up all the checkboxes
    private void setupCheckboxes() {
        // music checkbox
        musicCheckBox.setOnAction(e -> {
            boolean selected = musicCheckBox.isSelected();
            musicStatus.setText(selected ? "On" : "Off");
            gameConfig.setMusicEnabled(selected);
            gameConfig.saveConfigurationToFile();
        });

        // sound effects checkbox
        soundCheckBox.setOnAction(e -> {
            boolean selected = soundCheckBox.isSelected();
            soundStatus.setText(selected ? "On" : "Off");
            gameConfig.setSoundEnabled(selected);
            gameConfig.saveConfigurationToFile();
        });

        // extended mode checkbox
        extendCheckBox.setOnAction(e -> {
            boolean selected = extendCheckBox.isSelected();
            extendStatus.setText(selected ? "On" : "Off");
            gameConfig.setExtendedMode(selected);
            updatePlayerTwoRadioButtons(selected);

            // default Player 2 to HUMAN when extended mode is enabled
            if (selected) {
                gameConfig.setPlayer2Type(GameConfig.PlayerType.HUMAN);
                playerTwoHuman.setSelected(true);
            }
            gameConfig.saveConfigurationToFile();
        });
    }

    // sets up radio button groups for player types
    private void setupRadioButtons() {
        ToggleGroup playerOneGroup = new ToggleGroup();
        ToggleGroup playerTwoGroup = new ToggleGroup();

        playerOneHuman.setToggleGroup(playerOneGroup);
        playerOneAI.setToggleGroup(playerOneGroup);
        playerOneExternal.setToggleGroup(playerOneGroup);

        playerTwoHuman.setToggleGroup(playerTwoGroup);
        playerTwoAI.setToggleGroup(playerTwoGroup);
        playerTwoExternal.setToggleGroup(playerTwoGroup);

        playerOneGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == playerOneHuman) {
                gameConfig.setPlayer1Type(GameConfig.PlayerType.HUMAN);
            } else if (newToggle == playerOneAI) {
                gameConfig.setPlayer1Type(GameConfig.PlayerType.AI);
            } else if (newToggle == playerOneExternal) {
                gameConfig.setPlayer1Type(GameConfig.PlayerType.EXTERNAL);
            }
            gameConfig.saveConfigurationToFile();
        });

        playerTwoGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == playerTwoHuman) {
                gameConfig.setPlayer2Type(GameConfig.PlayerType.HUMAN);
            } else if (newToggle == playerTwoAI) {
                gameConfig.setPlayer2Type(GameConfig.PlayerType.AI);
            } else if (newToggle == playerTwoExternal) {
                gameConfig.setPlayer2Type(GameConfig.PlayerType.EXTERNAL);
            }
            gameConfig.saveConfigurationToFile();
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