package ui.gameplayscreen;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.Random;

public class GameplayScreen {

    @FXML
    private Canvas gameCanvas;
    
    @FXML
    private Button backButton;
    
    private GraphicsContext gc;
    private AnimationTimer gameLoop;

    private static final int CELL_SIZE = 25;
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;
    private static final int PADDING = 0;

    private static final Color BACKGROUND_COLOR = Color.web("#111111");
    private static final Color BORDER_COLOR = Color.web("#333333");

    private String[][] board = new String[BOARD_HEIGHT][BOARD_WIDTH];

    private static final boolean[][][] SHAPES = {
            {{true, true, true, true}},                     // I
            {{true, true}, {true, true}},                   // O
            {{false, true, false}, {true, true, true}},     // T
            {{false, true, true}, {true, true, false}},     // S
            {{true, true, false}, {false, true, true}},     // Z
            {{true, false, false}, {true, true, true}},     // J
            {{false, false, true}, {true, true, true}}      // L
    };

    private static final String[] COLORS = {
            "cyan", "yellow", "purple", "green", "red", "blue", "orange"
    };

    private boolean[][] pieceShape;
    private String pieceColor;
    private int pieceX;
    private int pieceY;

    private Random random = new Random();

    private long lastDropTime = 0;
    private static final long DROP_INTERVAL_NS = 500_000_000; // 500ms in nanoseconds

    public void initialize() {
        gc = gameCanvas.getGraphicsContext2D();
        initBoard();
        spawnNewPiece();
        backButton.setOnAction(e -> onBackButtonClicked());

        // Setup keyboard events on the scene after it's ready:
        gameCanvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupKeyboardEvents(newScene);
            }
        });

        startGameLoop();
    }

    private void onBackButtonClicked() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        System.out.println("Back button clicked");
    }

    private void initBoard() {
        for (int r = 0; r < BOARD_HEIGHT; r++) {
            for (int c = 0; c < BOARD_WIDTH; c++) {
                board[r][c] = null;
            }
        }
    }

    private void spawnNewPiece() {
        int idx = random.nextInt(SHAPES.length);
        pieceShape = SHAPES[idx];
        pieceColor = COLORS[idx];
        pieceX = BOARD_WIDTH / 2 - pieceShape[0].length / 2;
        pieceY = 0;
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastDropTime == 0) {
                    lastDropTime = now;
                }
                if (now - lastDropTime >= DROP_INTERVAL_NS) {
                    moveDown();
                    lastDropTime = now;
                }
                drawGame();
            }
        };
        gameLoop.start();
    }

    private boolean collision(int x, int y, boolean[][] shape) {
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c]) {
                    int boardX = x + c;
                    int boardY = y + r;
                    if (boardX < 0 || boardX >= BOARD_WIDTH || boardY >= BOARD_HEIGHT) {
                        return true;
                    }
                    if (boardY >= 0 && board[boardY][boardX] != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void fixPieceToBoard() {
        for (int r = 0; r < pieceShape.length; r++) {
            for (int c = 0; c < pieceShape[r].length; c++) {
                if (pieceShape[r][c]) {
                    int row = pieceY + r;
                    int col = pieceX + c;
                    if (row >= 0 && row < BOARD_HEIGHT && col >= 0 && col < BOARD_WIDTH) {
                        board[row][col] = pieceColor;
                    }
                }
            }
        }
    }

    private void clearFullRows() {
        for (int r = BOARD_HEIGHT - 1; r >= 0; r--) {
            boolean full = true;
            for (int c = 0; c < BOARD_WIDTH; c++) {
                if (board[r][c] == null) {
                    full = false;
                    break;
                }
            }
            if (full) {
                for (int row = r; row > 0; row--) {
                    System.arraycopy(board[row - 1], 0, board[row], 0, BOARD_WIDTH);
                }
                for (int col = 0; col < BOARD_WIDTH; col++) {
                    board[0][col] = null;
                }
                r++; // check same row again
            }
        }
    }

    private void moveLeft() {
        if (!collision(pieceX - 1, pieceY, pieceShape)) {
            pieceX--;
        }
    }

    private void moveRight() {
        if (!collision(pieceX + 1, pieceY, pieceShape)) {
            pieceX++;
        }
    }

    private void moveDown() {
        if (!collision(pieceX, pieceY + 1, pieceShape)) {
            pieceY++;
        } else {
            fixPieceToBoard();
            clearFullRows();
            spawnNewPiece();
            if (collision(pieceX, pieceY, pieceShape)) {
                gameLoop.stop();
                showGameOverDialog();
            }
        }
    }

    private void rotate() {
        boolean[][] rotated = rotateShape(pieceShape);
        if (!collision(pieceX, pieceY, rotated)) {
            pieceShape = rotated;
        }
    }

    private boolean[][] rotateShape(boolean[][] shape) {
        int rows = shape.length;
        int cols = shape[0].length;
        boolean[][] rotated = new boolean[cols][rows];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                rotated[c][rows - 1 - r] = shape[r][c];
            }
        }
        return rotated;
    }

    public void setupKeyboardEvents(Scene scene) {
        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            switch (code) {
                case LEFT -> moveLeft();
                case RIGHT -> moveRight();
                case UP -> rotate();
                case DOWN -> moveDown();
            }
        });

        scene.getRoot().setFocusTraversable(true);
        scene.getRoot().requestFocus();
    }

    private void drawGame() {
        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        // Draw board
        for (int r = 0; r < BOARD_HEIGHT; r++) {
            for (int c = 0; c < BOARD_WIDTH; c++) {
                double x = PADDING + c * CELL_SIZE;
                double y = PADDING + r * CELL_SIZE;

                gc.setFill(BACKGROUND_COLOR);
                gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);

                gc.setStroke(BORDER_COLOR);
                gc.setLineWidth(0.5);
                gc.strokeRect(x, y, CELL_SIZE, CELL_SIZE);

                String color = board[r][c];
                if (color != null) {
                    drawCell(x, y, CELL_SIZE, color);
                }
            }
        }

        // Draw current piece
        if (pieceShape != null) {
            for (int r = 0; r < pieceShape.length; r++) {
                for (int c = 0; c < pieceShape[r].length; c++) {
                    if (pieceShape[r][c]) {
                        int boardCol = pieceX + c;
                        int boardRow = pieceY + r;

                        if (boardCol >= 0 && boardCol < BOARD_WIDTH && boardRow >= 0 && boardRow < BOARD_HEIGHT) {
                            double x = PADDING + boardCol * CELL_SIZE;
                            double y = PADDING + boardRow * CELL_SIZE;
                            drawCell(x, y, CELL_SIZE, pieceColor);
                        }
                    }
                }
            }
        }
    }

    private void drawCell(double x, double y, int size, String colorName) {
        Color fillColor;
        Color borderColor;

        switch (colorName.toLowerCase()) {
            case "yellow" -> {
                fillColor = Color.YELLOW;
                borderColor = Color.GOLDENROD;
            }
            case "cyan" -> {
                fillColor = Color.CYAN;
                borderColor = Color.DARKCYAN;
            }
            case "purple" -> {
                fillColor = Color.MEDIUMPURPLE;
                borderColor = Color.DARKMAGENTA;
            }
            case "green" -> {
                fillColor = Color.LIMEGREEN;
                borderColor = Color.DARKGREEN;
            }
            case "red" -> {
                fillColor = Color.RED;
                borderColor = Color.DARKRED;
            }
            case "blue" -> {
                fillColor = Color.DODGERBLUE;
                borderColor = Color.DARKBLUE;
            }
            case "orange" -> {
                fillColor = Color.ORANGE;
                borderColor = Color.DARKORANGE;
            }
            default -> {
                fillColor = Color.GRAY;
                borderColor = Color.DARKGRAY;
            }
        }

        gc.setFill(fillColor);
        gc.fillRect(x, y, size, size);

        gc.setStroke(borderColor);
        gc.setLineWidth(1);
        gc.strokeRect(x, y, size, size);
    }

    private void showGameOverDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);
        alert.setContentText("Game Over!\nThanks for playing.");
        alert.showAndWait();
    }

    public static Scene getScene() {
        try {
            FXMLLoader loader = new FXMLLoader(GameplayScreen.class.getResource("gameplay.fxml"));
            Parent root = loader.load();
            GameplayScreen controller = loader.getController();

            Scene scene = new Scene(root, BOARD_WIDTH * CELL_SIZE + 2 * PADDING, BOARD_HEIGHT * CELL_SIZE + 60);
            controller.setupKeyboardEvents(scene);

            return scene;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load gameplay screen", e);
        }
    }
}