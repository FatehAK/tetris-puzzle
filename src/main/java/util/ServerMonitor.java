package util;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.util.Duration;
import java.io.IOException;
import java.net.Socket;

// Server connection testing, dialog management, and monitoring
public class ServerMonitor {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 3000;
    private static final int CONNECTION_TIMEOUT = 2000; // 2 seconds
    
    private Timeline serverCheckTimer;
    private Alert currentDialog = null;
    private final Object dialogLock = new Object();
    
    // checks if TetrisServer is running and accepting connections
    public boolean isServerRunning() {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(SERVER_HOST, SERVER_PORT), CONNECTION_TIMEOUT);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    // gets server address for display purposes
    public String getServerAddress() {
        return SERVER_HOST + ":" + SERVER_PORT;
    }
    
    public void showDialog(Runnable onBackToMenu) {
        synchronized (dialogLock) {
            if (currentDialog != null) {
                return;
            }
        }
        
        Platform.runLater(() -> {
            synchronized (dialogLock) {
                if (currentDialog != null) return;
                
                currentDialog = new Alert(Alert.AlertType.WARNING);
                currentDialog.setTitle("TetrisServer Unavailable");
                currentDialog.setHeaderText("Connecting to TetrisServer...");
                currentDialog.setContentText("The external TetrisServer at " + getServerAddress() + 
                                           " is not available. Trying to reconnect...");
                
                currentDialog.getButtonTypes().clear();
                ButtonType backToMenuButton = new ButtonType("Back to Menu");
                currentDialog.getButtonTypes().add(backToMenuButton);
                
                currentDialog.setOnHidden(e -> {
                    synchronized (dialogLock) {
                        if (currentDialog != null) {
                            ButtonType result = currentDialog.getResult();
                            if (result == backToMenuButton && onBackToMenu != null) {
                                onBackToMenu.run();
                            }
                            currentDialog = null;
                        }
                    }
                });
                
                currentDialog.show();
            }
        });
    }
    
    public void showDialog() {
        showDialog(null);
    }
    
    // hides the reconnection dialog
    public void hideDialog() {
        synchronized (dialogLock) {
            if (currentDialog != null) {
                Platform.runLater(() -> {
                    synchronized (dialogLock) {
                        if (currentDialog != null) {
                            currentDialog.close();
                            currentDialog = null;
                        }
                    }
                });
            }
        }
    }
    
    // checks if dialog is currently showing
    public boolean isDialogShowing() {
        synchronized (dialogLock) {
            return currentDialog != null;
        }
    }
    
    // starts monitoring server with callback for when server becomes available
    public void startMonitoring(Runnable onServerAvailable) {
        if (serverCheckTimer != null) {
            serverCheckTimer.stop();
        }
        
        serverCheckTimer = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            if (isServerRunning()) {
                stop();
                if (onServerAvailable != null) {
                    onServerAvailable.run();
                }
            }
        }));
        serverCheckTimer.setCycleCount(Timeline.INDEFINITE);
        serverCheckTimer.play();
    }
    
    // starts monitoring server with callbacks for both connection and disconnection
    public void startMonitoring(Runnable onServerAvailable, Runnable onServerUnavailable) {
        if (serverCheckTimer != null) {
            serverCheckTimer.stop();
        }
        
        boolean[] lastServerState = {isServerRunning()};
        
        serverCheckTimer = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            boolean currentServerState = isServerRunning();
            
            if (currentServerState != lastServerState[0]) {
                lastServerState[0] = currentServerState;
                
                if (currentServerState && onServerAvailable != null) {
                    onServerAvailable.run();
                } else if (!currentServerState && onServerUnavailable != null) {
                    onServerUnavailable.run();
                }
            }
        }));
        serverCheckTimer.setCycleCount(Timeline.INDEFINITE);
        serverCheckTimer.play();
    }
    
    // stops monitoring
    public void stop() {
        if (serverCheckTimer != null) {
            serverCheckTimer.stop();
            serverCheckTimer = null;
        }
    }
}