package model;

// evaluates board states for AI decision making using height, holes, lines cleared and bumpiness
public class BoardEvaluator {
    
    public int evaluateBoard(String[][] board) {
        int heightScore = getHeight(board);
        int holesScore = getHoles(board);
        int linesCleared = getClearedLines(board);
        int bumpinessScore = getBumpiness(board);
        
        // weights based on tutorial - prioritize clearing lines and avoiding holes
        return (-4 * heightScore) + (3 * linesCleared) - (5 * holesScore) - (2 * bumpinessScore);
    }
    
    private int getHeight(String[][] board) {
        // calculate the height of the pile (the highest filled row)
        int height = 0;
        for (int x = 0; x < board[0].length; x++) {
            for (int y = 0; y < board.length; y++) {
                if (board[y][x] != null) {
                    height = Math.max(height, board.length - y);
                    break;
                }
            }
        }
        return height;
    }
    
    private int getHoles(String[][] board) {
        // calculate the number of holes (empty spaces beneath filled blocks)
        int holes = 0;
        for (int x = 0; x < board[0].length; x++) {
            boolean foundBlock = false;
            for (int y = 0; y < board.length; y++) {
                if (board[y][x] != null) {
                    foundBlock = true;
                } else if (foundBlock && board[y][x] == null) {
                    holes++;
                }
            }
        }
        return holes;
    }
    
    private int getClearedLines(String[][] board) {
        // calculate how many full lines are cleared
        int clearedLines = 0;
        for (int y = 0; y < board.length; y++) {
            boolean isLineFull = true;
            for (int x = 0; x < board[0].length; x++) {
                if (board[y][x] == null) {
                    isLineFull = false;
                    break;
                }
            }
            if (isLineFull) {
                clearedLines++;
            }
        }
        return clearedLines;
    }
    
    private int getBumpiness(String[][] board) {
        // calculate the bumpiness of the surface
        int bumpiness = 0;
        for (int x = 0; x < board[0].length - 1; x++) {
            int colHeight1 = getColumnHeight(board, x);
            int colHeight2 = getColumnHeight(board, x + 1);
            bumpiness += Math.abs(colHeight1 - colHeight2);
        }
        return bumpiness;
    }
    
    private int getColumnHeight(String[][] board, int col) {
        for (int y = 0; y < board.length; y++) {
            if (board[y][col] != null) {
                return board.length - y;
            }
        }
        return 0;
    }
}