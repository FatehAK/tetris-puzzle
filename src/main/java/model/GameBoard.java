package model;
import util.AudioManager;
// Manages the Tetris game board and collision detection with configurable dimensions
public class GameBoard {
    private final int boardWidth;
    private final int boardHeight;
    private String[][] board;

    public GameBoard(int width, int height) {
        this.boardWidth = width;
        this.boardHeight = height;
        board = new String[boardHeight][boardWidth];
        clearBoard();
    }

    public int getBoardWidth() {
        return boardWidth;
    }

    public int getBoardHeight() {
        return boardHeight;
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
            for (int row = 0; row < cells.length && row < boardHeight; row++) {
                for (int col = 0; col < cells[row].length && col < boardWidth; col++) {
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
                    if (boardX < 0 || boardX >= boardWidth || boardY >= boardHeight) {
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

                    if (boardX >= 0 && boardX < boardWidth &&
                            boardY >= 0 && boardY < boardHeight) {
                        board[boardY][boardX] = shape.getColor();
                    }
                }
            }
        }
    }

    public String getCellColor(int row, int col) {
        if (row >= 0 && row < boardHeight && col >= 0 && col < boardWidth) {
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
    public int clearFullRows() {
        int rowsCleared = 0;

        for (int row = 0; row < boardHeight; ) {
            boolean fullRow = true;

            for (int col = 0; col < boardWidth; col++) {
                if (board[row][col] == null) {
                    fullRow = false;
                    break;
                }
            }

            if (fullRow) {
                // play line clear sound effect
                AudioManager.getInstance().playSoundEffect(AudioManager.SOUND_LINE_CLEAR);
                rowsCleared++;

                // shift all rows above down by one
                for (int y = row; y > 0; y--) {
                    System.arraycopy(board[y - 1], 0, board[y], 0, boardWidth);
                }

                // clear top row
                for (int col = 0; col < boardWidth; col++) {
                    board[0][col] = null;
                }

                // stay on the same row to re-check it after the shift
            } else {
                row++; // only move to the next row if no row was cleared
            }
        }
        return rowsCleared;
    }
}