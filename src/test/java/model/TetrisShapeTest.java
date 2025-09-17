package model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class TetrisShapeTest {

    @Test
    public void testShapeTypeAndColor() {
        TetrisShape shape = new TetrisShape(TetrisShape.ShapeType.I, 0, 0);
        assertEquals(TetrisShape.ShapeType.I, shape.getType());
        assertEquals("cyan", shape.getColor());
    }

    @Test
    public void testDimensionsForOShape() {
        TetrisShape shape = new TetrisShape(TetrisShape.ShapeType.O, 0, 0);
        assertEquals(2, shape.getWidth());
        assertEquals(2, shape.getHeight());
    }

    @Test
    public void testIsCellFilled() {
        TetrisShape shape = new TetrisShape(TetrisShape.ShapeType.T, 0, 0);
        assertTrue(shape.isCellFilled(1, 1)); // center of T
        assertFalse(shape.isCellFilled(0, 0)); // top left of T
    }

    @Test
    public void testRotateIShape() {
        TetrisShape shape = new TetrisShape(TetrisShape.ShapeType.I, 0, 0);
        int originalWidth = shape.getWidth();
        int originalHeight = shape.getHeight();
        shape.rotate();
        assertEquals(originalHeight, shape.getWidth());
        assertEquals(originalWidth, shape.getHeight());
    }

    @Test
    public void testSetAndGetPosition() {
        TetrisShape shape = new TetrisShape(TetrisShape.ShapeType.L, 3, 4);
        assertEquals(3, shape.getX());
        assertEquals(4, shape.getY());
        shape.setX(5);
        shape.setY(6);
        assertEquals(5, shape.getX());
        assertEquals(6, shape.getY());
    }
}