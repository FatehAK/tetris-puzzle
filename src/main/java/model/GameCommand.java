package model;

// Functional interface implementing Command pattern for game actions
// Uses method references for minimal memory footprint and maximum performance
@FunctionalInterface
public interface GameCommand {
    // execute the command on the given game engine
    boolean execute(GameEngine engine);
    
    // creates a command to move the current piece left
    static GameCommand moveLeft() {
        return GameEngine::movePieceLeft;
    }
    
    // creates a command to move the current piece right
    static GameCommand moveRight() {
        return GameEngine::movePieceRight;
    }
    
    // creates a command to rotate the current piece
    static GameCommand rotate() {
        return GameEngine::rotatePiece;
    }
    
    // creates a command to move the current piece down
    static GameCommand moveDown() {
        return GameEngine::movePieceDown;
    }
    
    // creates a command to enable/disable fast drop
    static GameCommand setFastDrop(boolean enabled) {
        return engine -> {
            engine.setFastDropEnabled(enabled);
            return true;
        };
    }
}