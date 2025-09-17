package model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class GameBoardTest {

    @Test
    public void testClearFullRows() {
        GameBoard board = new GameBoard(4, 4);
        String[][] cells = new String[4][4];
        // Fill the first row
        for (int col = 0; col < 4; col++) {
            cells[0][col] = "red";
        }
        board.setBoardState(cells);
        int cleared = board.clearFullRows();
        assertEquals(1, cleared);
    }

    @Test
    public void testGetCellColor() {
        GameBoard board = new GameBoard(2, 2);
        String[][] cells = new String[2][2];
        cells[0][0] = "blue";
        board.setBoardState(cells);
        assertEquals("blue", board.getCellColor(0, 0));
    }
}