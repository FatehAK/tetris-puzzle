package ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

// Abstract base class for all screen controllers
public abstract class BaseScreen {
    // common method to load FXML files
    protected static Scene loadScene(Class<?> screenClass, String fxmlFile, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(screenClass.getResource(fxmlFile));
            Parent root = loader.load();
            return new Scene(root, width, height);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load " + screenClass.getSimpleName(), e);
        }
    }
    
    // method for screens that need access to controller after loading
    protected static <T> LoadResult<T> loadSceneWithController(Class<T> screenClass, String fxmlFile, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(screenClass.getResource(fxmlFile));
            Parent root = loader.load();
            T controller = loader.getController();
            Scene scene = new Scene(root, width, height);
            return new LoadResult<>(scene, controller);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load " + screenClass.getSimpleName(), e);
        }
    }
    
    // record to return both scene and controller
    protected record LoadResult<T>(Scene scene, T controller) {}
    
    // abstract method that subclasses can override for initialization
    public abstract void initialize();
}