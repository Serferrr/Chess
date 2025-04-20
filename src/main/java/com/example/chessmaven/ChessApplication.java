package com.example.chessmaven;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.game.ChessGame;

import java.io.IOException;
import javafx.util.Duration;
import javafx.scene.layout.Pane;

/**
 * The main application class for the Chess game.
 * It initializes the game, loads the FXML view, and sets up the stage.
 */
public class ChessApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Create the game logic instance. This is the core of the chess game.
        ChessGame game = new ChessGame();

        // Load the FXML file for the chess view. This defines the layout of the UI.
        FXMLLoader fxmlLoader = new FXMLLoader(ChessApplication.class.getResource("chess-view.fxml"));
        Pane root = fxmlLoader.load();
        // Create a scene with the loaded FXML content and set its dimensions.
        Scene scene = new Scene(root, 800, 650);

        // Get the controller instance from the FXML loader.
        ChessController controller = fxmlLoader.getController();
        // Pass the game object to the controller, so the controller can interact with the game logic.
        controller.setGame(game);
        // Initialize the board in the controller, setting up the initial piece positions.
        controller.initializeBoard();

        // Set the title of the application window.
        stage.setTitle("Simple Chess");
        // Set the scene for the stage.
        stage.setScene(scene);

        // Fade-in animation for the root pane.
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        // Fade-out animation when the stage is closing.
        stage.setOnCloseRequest(event -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), root);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.play();
        });

        // Keep resizable false for now.
        stage.setResizable(false);
        // Show the stage, making the application window visible.
        stage.show();
    }

    // Main method to launch the application.
    public static void main(String[] args) {
        launch(); // Use launch() to start the JavaFX application properly
    }
}