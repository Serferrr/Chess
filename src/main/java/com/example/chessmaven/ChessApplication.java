package com.example.chessmaven;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.game.ChessGame;

import java.io.IOException;
import java.net.URL;

public class ChessApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Create the game logic instance
        ChessGame game = new ChessGame();

        // Load the FXML file for the chess view
        FXMLLoader fxmlLoader = new FXMLLoader(ChessApplication.class.getResource("chess-view.fxml"));
        // Update the Scene width to match the FXML prefWidth
        Scene scene = new Scene(fxmlLoader.load(), 800, 650); // Increased width

        // Get the controller instance and pass the game object
        ChessController controller = fxmlLoader.getController();
        controller.setGame(game); // Pass the game instance to the controller
        controller.initializeBoard(); // Draw the initial board

        stage.setTitle("Simple Chess");
        stage.setScene(scene);
        // Keep resizable false for now, or set min/max width/height if you allow resizing
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(); // Use launch() to start the JavaFX application properly
    }
}