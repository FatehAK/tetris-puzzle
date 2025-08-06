package model;

// Interface for handling input control commands
public interface InputController {
    boolean moveLeft();
    boolean moveRight();
    boolean rotate();
    void setFastDrop(boolean enabled);
}