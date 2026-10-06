package com.tictactoe;

import com.tictactoe.db.MongoConnection;
import com.tictactoe.ui.ScreenController;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application entry point for the JavaFX Tic-Tac-Toe game.
 */
public class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting Tic-Tac-Toe Desktop Application...");

        // Eagerly initialize DB connection in background
        Thread dbInitThread = new Thread(() -> MongoConnection.getInstance());
        dbInitThread.setDaemon(true);
        dbInitThread.start();

        ScreenController screenController = new ScreenController(primaryStage);
        screenController.showStartScreen();

        primaryStage.show();
    }

    @Override
    public void stop() {
        logger.info("Application shutting down. Closing MongoDB resources...");
        MongoConnection.getInstance().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
