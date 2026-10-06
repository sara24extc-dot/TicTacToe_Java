package com.tictactoe.ui;

import com.tictactoe.db.MongoConnection;
import com.tictactoe.model.Player;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Start / Welcome screen where players enter usernames and view connection status.
 */
public class StartScreen {

    private final ScreenController controller;
    private final VBox root;

    public StartScreen(ScreenController controller) {
        this.controller = controller;
        this.root = new VBox(24);
        this.root.setAlignment(Pos.CENTER);
        this.root.setStyle("-fx-padding: 30px;");

        buildUI();
    }

    private void buildUI() {
        // Title
        Label titleLabel = new Label("Tic Tac Toe");
        titleLabel.getStyleClass().add("game-title");

        Label subtitleLabel = new Label("JavaFX & MongoDB Edition");
        subtitleLabel.getStyleClass().add("subtitle");

        // Database Status Badge
        boolean isConnected = MongoConnection.getInstance().isConnected();
        Label statusBadge = new Label(isConnected ? "● MongoDB Connected" : "● Offline Mode (Data not saved)");
        statusBadge.getStyleClass().add(isConnected ? "status-badge-online" : "status-badge-offline");

        // Form Card for Player Input
        VBox formCard = new VBox(14);
        formCard.getStyleClass().add("form-card");
        formCard.setMaxWidth(420);

        Label playerOLabel = new Label("Player O (First Move):");
        playerOLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 14px;");

        TextField playerOField = new TextField(controller.getCurrentPlayerO());
        playerOField.setPromptText("Enter Player O username");
        playerOField.getStyleClass().add("text-input-field");

        Label playerXLabel = new Label("Player X (Second Move):");
        playerXLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 14px;");

        TextField playerXField = new TextField(controller.getCurrentPlayerX());
        playerXField.setPromptText("Enter Player X username");
        playerXField.getStyleClass().add("text-input-field");

        formCard.getChildren().addAll(playerOLabel, playerOField, playerXLabel, playerXField);

        // Buttons
        Button startButton = new Button("Start Game");
        startButton.getStyleClass().addAll("dark-button");
        startButton.setStyle("-fx-font-size: 18px; -fx-padding: 14px 36px; -fx-background-color: #191913;");
        startButton.setOnAction(e -> {
            String nameO = playerOField.getText().trim();
            String nameX = playerXField.getText().trim();
            if (nameO.isEmpty()) nameO = "Player O";
            if (nameX.isEmpty()) nameX = "Player X";

            // Register or fetch players in background / repository
            controller.getPlayerRepository().getOrCreatePlayer(nameO);
            controller.getPlayerRepository().getOrCreatePlayer(nameX);

            controller.showGameScreen(nameO, nameX);
        });

        HBox navButtons = new HBox(16);
        navButtons.setAlignment(Pos.CENTER);

        Button leaderboardButton = new Button("Leaderboard");
        leaderboardButton.getStyleClass().add("dark-button");
        leaderboardButton.setOnAction(e -> controller.showLeaderboardScreen());

        Button historyButton = new Button("Game History");
        historyButton.getStyleClass().add("dark-button");
        historyButton.setOnAction(e -> controller.showHistoryScreen());

        navButtons.getChildren().addAll(leaderboardButton, historyButton);

        root.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                statusBadge,
                formCard,
                startButton,
                navButtons
        );
    }

    public Pane getView() {
        return root;
    }
}
