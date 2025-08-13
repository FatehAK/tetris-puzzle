# Tetris Puzzle Game

A JavaFX Tetris implementation with screen-based architecture and canvas rendering.

## Requirements
- Java 23
- JavaFX 21  
- Maven 3.6+

## Commands
```bash
# Build project
mvn compile      
# Run main application              
mvn javafx:run       
# Development mode         
mvn compile exec:java -Dexec.mainClass="ui.DevLauncher"
# Package application  
mvn package           
# Clean build        
mvn clean                     
```

## Features
- Complete game engine with 60fps rendering
- Screen navigation (splash, menu, gameplay, config, scores)
- Canvas-based game rendering with smooth animations
- Tetris shapes: I, O, T, L, Z with collision detection
- Development tools for testing individual screens

## Architecture
- **Model**: Game logic (GameBoard, GameEngine, TetrisShape)
- **UI**: Screen controllers with FXML layouts
- **Util**: Color management and utilities
