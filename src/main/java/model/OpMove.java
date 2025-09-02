package model;

// Record for server response containing optimal move
public record OpMove(int opX, int opRotate) {
    // opX: The optimal X-position where the current tetromino should be placed
    // opRotate: The optimal number of rotations to apply to the current tetromino
    //
    // Special interpretations:
    // - opX = 0 means place at left-most position
    // - opRotate = 0 means no rotation needed
}