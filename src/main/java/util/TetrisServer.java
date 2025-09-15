package util;

import com.google.gson.Gson;
import model.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Multithreaded server that receives game state and returns optimal moves using AI
public class TetrisServer {
    private static final int PORT = 3000;
    private static final int THREAD_POOL_SIZE = 10;
    
    private final TetrisAI tetrisAI;
    private final Gson gson;
    private final ExecutorService executor;
    
    public TetrisServer() {
        this.tetrisAI = new TetrisAI();
        this.gson = new Gson();
        this.executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }
    
    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("TetrisServer started on localhost:" + PORT + " with " + THREAD_POOL_SIZE + " threads");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected - submitting to thread pool");
                
                // handle each client in separate thread
                executor.submit(() -> handleClient(clientSocket));
            }
        } finally {
            shutdown();
        }
    }
    
    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            String requestJson = in.readLine();
            System.out.println("Received: " + requestJson);
            
            if (requestJson != null) {
                OpMove response = processRequest(requestJson);
                String responseJson = gson.toJson(response);
                
                out.println(responseJson);
                System.out.println("Sent: " + responseJson);
            }
            
        } catch (Exception e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }
    
    private OpMove processRequest(String requestJson) {
        try {
            PureGame pureGame = gson.fromJson(requestJson, PureGame.class);
            
            if (pureGame == null || pureGame.getCells() == null || pureGame.getCurrentShape() == null) {
                return new OpMove(0, 0);
            }
            
            // create game board directly from PureGame cells
            ui.configscreen.GameConfig config = ui.configscreen.GameConfig.getInstance();
            GameBoard gameBoard = new GameBoard(config.getFieldWidth(), config.getFieldHeight());
            gameBoard.setBoardState(pureGame.getCells());
            
            // create tetris shape with actual type and position from game
            TetrisShape.ShapeType shapeType = TetrisShape.ShapeType.valueOf(pureGame.getCurrentShapeType());
            TetrisShape currentShape = new TetrisShape(shapeType, pureGame.getCurrentShape(), 
                                                     pureGame.getCurrentShapeX(), 
                                                     pureGame.getCurrentShapeY());
            
            // get AI recommendation
            TetrisAI.Move bestMove = tetrisAI.findBestMove(gameBoard, currentShape);
            
            if (bestMove != null) {
                return new OpMove(bestMove.column(), bestMove.rotations());
            } else {
                return new OpMove(0, 0);
            }
            
        } catch (Exception e) {
            System.err.println("Error processing request: " + e.getMessage());
            return new OpMove(0, 0);
        }
    }
    
    // graceful shutdown of thread pool
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            System.out.println("Shutting down thread pool...");
            executor.shutdown();
        }
    }
    
    public static void main(String[] args) {
        TetrisServer server = new TetrisServer();
        
        // shutdown hook for graceful cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}