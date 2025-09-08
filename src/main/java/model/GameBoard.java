package model;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
// Manages the 10x20 Tetris game board and collision detection
public class GameBoard {
    public static final int BOARD_WIDTH = 10;
    public static final int BOARD_HEIGHT = 20;
    
    private String[][] board;
    
    public GameBoard() {
        board = new String[BOARD_HEIGHT][BOARD_WIDTH];
        clearBoard();
    }
    
    public void clearBoard() {
        for (String[] row : board) {
            for (int col = 0; col < row.length; col++) {
                row[col] = null;
            }
        }
    }
    
    // sets board state directly from String array (for server use)
    public void setBoardState(String[][] cells) {
        clearBoard();
        if (cells != null) {
            for (int row = 0; row < cells.length && row < BOARD_HEIGHT; row++) {
                for (int col = 0; col < cells[row].length && col < BOARD_WIDTH; col++) {
                    board[row][col] = cells[row][col];
                }
            }
        }
    }
    
    public boolean isValidPosition(TetrisShape shape, int newX, int newY) {
        for (int row = 0; row < shape.getHeight(); row++) {
            for (int col = 0; col < shape.getWidth(); col++) {
                if (shape.isCellFilled(row, col)) {
                    int boardX = newX + col; // translate to board coordinates
                    int boardY = newY + row;
                    
                    // lllow pieces above the game area (negative Y), but check bounds for visible area
                    if (boardX < 0 || boardX >= BOARD_WIDTH || boardY >= BOARD_HEIGHT) {
                        return false;
                    }
                    
                    // only check collision if the cell is within the visible game area
                    if (boardY >= 0 && board[boardY][boardX] != null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public void placePiece(TetrisShape shape) {
        for (int row = 0; row < shape.getHeight(); row++) {
            for (int col = 0; col < shape.getWidth(); col++) {
                if (shape.isCellFilled(row, col)) {
                    int boardX = shape.getX() + col;
                    int boardY = shape.getY() + row;
                    
                    if (boardX >= 0 && boardX < BOARD_WIDTH && 
                        boardY >= 0 && boardY < BOARD_HEIGHT) {
                        board[boardY][boardX] = shape.getColor();
                    }
                }
            }
        }
    }

    public String getCellColor(int row, int col) {
        if (row >= 0 && row < BOARD_HEIGHT && col >= 0 && col < BOARD_WIDTH) {
            return board[row][col];
        }
        return null;
    }

    /**
     * Line-clearing logic
     * Scans the game board for any full lines (rows where every cell is filled).
     * For each full line found, clears the line and shifts all rows above it down by one.
     * Multiple full lines can be cleared in a single call, and the shifting is repeated as needed.
     * This method modifies the board in place.
     */

    public void clearFullRows() {
        for (int row = 0; row < BOARD_HEIGHT; ) {
            boolean fullRow = true;

            for (int col = 0; col < BOARD_WIDTH; col++) {
                if (board[row][col] == null) {
                    fullRow = false;
                    break;
                }
            }

            if (fullRow) {
                // Play erase-line sound effect
                try {
                    String soundPath = getClass().getResource("/audio/erase-line.wav").toExternalForm();
                    Media eraseLineMedia = new Media(soundPath);
                    MediaPlayer eraseLinePlayer = new MediaPlayer(eraseLineMedia);
                    eraseLinePlayer.play();
                    // Optional: release resources after playback
                    eraseLinePlayer.setOnEndOfMedia(() -> eraseLinePlayer.dispose());
                } catch (Exception e) {
                    System.out.println("Erase line sound could not be played: " + e.getMessage());
                }

                // shift all rows above down by one
                for (int y = row; y > 0; y--) {
                    System.arraycopy(board[y - 1], 0, board[y], 0, BOARD_WIDTH);
                }

                // clear top row
                for (int col = 0; col < BOARD_WIDTH; col++) {
                    board[0][col] = null;
                }

                // stay on the same row to re-check it after the shift
            } else {
                row++; // only move to the next row if no row was cleared
            }
        }
    }
}