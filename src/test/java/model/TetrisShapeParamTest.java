package model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

public class TetrisShapeParamTest {

    @ParameterizedTest
    @CsvSource({
        "0,1,true",   // T shape top middle
        "1,0,true",   // T shape bottom left
        "1,1,true",   // T shape bottom middle
        "1,2,true",   // T shape bottom right
        "0,0,false"   // T shape top left
    })
    public void testIsCellFilled(int row, int col, boolean expected) {
        TetrisShape shape = new TetrisShape(TetrisShape.ShapeType.T, 0, 0);
        assertEquals(expected, shape.isCellFilled(row, col));
    }
}