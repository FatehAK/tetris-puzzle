package model;

import java.util.Arrays;

// AI that finds the best move for a tetris piece by simulating all possible placements
public class TetrisAI {
    private final BoardEvaluator evaluator = new BoardEvaluator();

    // represents a move with column position and number of rotations
    public record Move(int column, int rotations) {}
    
    public Move findBestMove(GameBoard board, TetrisShape piece) {
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        
        int maxRotations = getMaxRotationsForPiece(piece.getType());
        
        for (int rotation = 0; rotation < maxRotations; rotation++) {
            // create test piece for this rotation
            TetrisShape testPiece = createTestPiece(piece, rotation);
            
            for (int col = 0; col < board.getBoardWidth(); col++) {
                // check if piece can fit in this column
                if (canFitInColumn(board, testPiece, col)) {
                    String[][] simulatedBoard = simulateDrop(board, testPiece, col);
                    int score = evaluator.evaluateBoard(simulatedBoard);
                    
                    if (score > bestScore) {
                        bestScore = score;
                        bestMove = new Move(col, rotation);
                    }
                }
            }
        }
        
        return bestMove;
    }
    
    // optimize rotation attempts based on piece symmetry
    private int getMaxRotationsForPiece(TetrisShape.ShapeType type) {
        return switch (type) {
            case O -> 1; // square/circle doesn't change
            case S, Z, I -> 2; // these have 2 unique states
            case T, L, J -> 4; // these have 4 unique states
        };
    }
    
    // create a test piece with specified rotations applied
    private TetrisShape createTestPiece(TetrisShape original, int rotations) {
        TetrisShape testPiece = new TetrisShape(original.getType(), 0, 0);
        for (int i = 0; i < rotations; i++) {
            testPiece.rotate();
        }
        return testPiece;
    }
    
    // check if a piece can potentially fit in a column
    private boolean canFitInColumn(GameBoard board, TetrisShape piece, int col) {
        return col >= 0 && col + piece.getWidth() <= board.getBoardWidth();
    }
    
    // simulate dropping a piece in a specific column and return resulting board
    private String[][] simulateDrop(GameBoard board, TetrisShape piece, int col) {
        String[][] simulatedBoard = copyBoard(board);
        
        // find the lowest valid position for the piece
        int dropRow = findDropRow(board, piece, col);
        
        // place the piece on the simulated board
        placePieceOnBoard(simulatedBoard, piece, col, dropRow);
        
        // simulate line clearing
        clearFullRowsFromBoard(simulatedBoard);
        
        return simulatedBoard;
    }
    
    // find the row where the piece would land if dropped in the given column
    private int findDropRow(GameBoard board, TetrisShape piece, int col) {
        int row = 0;
        
        // keep moving down until piece can't move further
        while (canPlacePiece(board, piece, col, row)) {
            row++;
        }
        
        return row - 1; // return the last valid row
    }
    
    // check if piece can be placed at given position
    private boolean canPlacePiece(GameBoard board, TetrisShape piece, int col, int row) {
        for (int r = 0; r < piece.getHeight(); r++) {
            for (int c = 0; c < piece.getWidth(); c++) {
                if (piece.isCellFilled(r, c)) {
                    int boardX = col + c;
                    int boardY = row + r;
                    
                    // check bounds
                    if (boardX < 0 || boardX >= board.getBoardWidth() ||
                        boardY < 0 || boardY >= board.getBoardHeight()) {
                        return false;
                    }
                    
                    // check collision with existing pieces
                    if (board.getCellColor(boardY, boardX) != null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    // place piece on the simulated board
    private void placePieceOnBoard(String[][] board, TetrisShape piece, int col, int row) {
        for (int r = 0; r < piece.getHeight(); r++) {
            for (int c = 0; c < piece.getWidth(); c++) {
                if (piece.isCellFilled(r, c)) {
                    int boardX = col + c;
                    int boardY = row + r;
                    
                    if (boardX >= 0 && boardX < board[0].length &&
                        boardY >= 0 && boardY < board.length) {
                        board[boardY][boardX] = piece.getColor();
                    }
                }
            }
        }
    }
    
    // create a copy of the game board for simulation
    private String[][] copyBoard(GameBoard board) {
        String[][] newBoard = new String[board.getBoardHeight()][board.getBoardWidth()];
        for (int y = 0; y < board.getBoardHeight(); y++) {
            for (int x = 0; x < board.getBoardWidth(); x++) {
                newBoard[y][x] = board.getCellColor(y, x);
            }
        }
        return newBoard;
    }
    
    // simulate line clearing on the board copy
    private void clearFullRowsFromBoard(String[][] board) {
        for (int row = 0; row < board.length; ) {
            boolean fullRow = true;
            
            for (int col = 0; col < board[0].length; col++) {
                if (board[row][col] == null) {
                    fullRow = false;
                    break;
                }
            }
            
            if (fullRow) {
                // shift all rows above down by one
                for (int y = row; y > 0; y--) {
                    System.arraycopy(board[y - 1], 0, board[y], 0, board[0].length);
                }
                
                // clear top row
                Arrays.fill(board[0], null);
            } else {
                row++; // only move to next row if no row was cleared
            }
        }
    }
}