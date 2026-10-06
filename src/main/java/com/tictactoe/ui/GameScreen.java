package com.tictactoe.ui;

import com.tictactoe.game.GameEngine;
import com.tictactoe.model.GameRecord;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Date;

/**
 * Main game screen displaying the 3x3 board, controls, and game outcomes.
 */
public class GameScreen {

    private final ScreenController controller;
    private final String playerO;
    private final String playerX;

    private final GameEngine engine;
    private final Button[] boxes = new Button[9];

    private final StackPane rootStack;
    private final VBox mainLayout;
    private final VBox msgContainer;
    private final Label msgLabel;
    private final Label turnLabel;
    private final Button newGameBtn;
    private final Button resetBtn;

    private boolean gameSaved = false;

    public GameScreen(ScreenController controller, String playerO, String playerX) {
        this.controller = controller;
        this.playerO = playerO;
        this.playerX = playerX;
        this.engine = new GameEngine();

        this.rootStack = new StackPane();
        this.rootStack.setAlignment(Pos.CENTER);

        this.mainLayout = new VBox(16);
        this.mainLayout.setAlignment(Pos.CENTER);
        this.mainLayout.setStyle("-fx-padding: 16px;");

        // Result Message Container (Overlay / Modal)
        this.msgContainer = new VBox(20);
        this.msgContainer.getStyleClass().add("msg-container");
        this.msgContainer.setMaxWidth(460);
        this.msgContainer.setVisible(false);
        this.msgContainer.setManaged(false);

        this.msgLabel = new Label();
        this.msgLabel.setId("msg");
        this.msgLabel.getStyleClass().add("result-msg");

        this.newGameBtn = new Button("New Game");
        this.newGameBtn.setId("new-btn");
        this.newGameBtn.getStyleClass().add("dark-button");
        this.newGameBtn.setOnAction(e -> resetGame());

        this.resetBtn = new Button("Reset Game");
        this.resetBtn.setId("reset-btn");
        this.resetBtn.getStyleClass().add("dark-button");
        this.resetBtn.setOnAction(e -> resetGame());

        this.turnLabel = new Label();
        this.turnLabel.getStyleClass().add("turn-indicator");

        buildUI();
        updateTurnDisplay();
    }

    private void buildUI() {
        // Title
        Label titleLabel = new Label("Tic Tac Toe");
        titleLabel.getStyleClass().add("game-title");

        // Player vs Player Info
        Label vsLabel = new Label(playerO + " (O)  vs  " + playerX + " (X)");
        vsLabel.setStyle("-fx-text-fill: #ffffc7; -fx-font-size: 15px; -fx-font-weight: bold;");

        // 3x3 Grid
        GridPane grid = new GridPane();
        grid.getStyleClass().add("board-grid");

        for (int i = 0; i < 9; i++) {
            final int index = i;
            Button box = new Button("");
            box.getStyleClass().add("box");
            box.setOnAction(e -> handleBoxClick(index));
            boxes[i] = box;

            int row = i / 3;
            int col = i % 3;
            grid.add(box, col, row);
        }

        // Bottom Navigation & Controls
        HBox bottomControls = new HBox(14);
        bottomControls.setAlignment(Pos.CENTER);

        Button leaderboardBtn = new Button("Leaderboard");
        leaderboardBtn.getStyleClass().add("dark-button");
        leaderboardBtn.setOnAction(e -> controller.showLeaderboardScreen());

        Button historyBtn = new Button("History");
        historyBtn.getStyleClass().add("dark-button");
        historyBtn.setOnAction(e -> controller.showHistoryScreen());

        Button menuBtn = new Button("Menu");
        menuBtn.getStyleClass().add("dark-button");
        menuBtn.setOnAction(e -> controller.showStartScreen());

        bottomControls.getChildren().addAll(resetBtn, leaderboardBtn, historyBtn, menuBtn);

        mainLayout.getChildren().addAll(
                titleLabel,
                vsLabel,
                turnLabel,
                grid,
                bottomControls
        );

        // Result Container content
        msgContainer.getChildren().addAll(msgLabel, newGameBtn);

        rootStack.getChildren().addAll(mainLayout, msgContainer);
    }

    private void handleBoxClick(int index) {
        if (engine.isGameOver()) return;

        String currentSymbol = engine.getCurrentSymbol();
        boolean moved = engine.makeMove(index);

        if (moved) {
            boxes[index].setText(currentSymbol);
            boxes[index].setDisable(true);

            if (engine.isGameOver()) {
                handleGameOver();
            } else {
                updateTurnDisplay();
            }
        }
    }

    private void handleGameOver() {
        disableAllBoxes();
        turnLabel.setVisible(false);

        if (engine.isDraw()) {
            msgLabel.setText("Game was a Draw.");
        } else {
            String winnerSymbol = engine.getWinner();
            String winnerPlayer = "O".equals(winnerSymbol) ? playerO : playerX;
            msgLabel.setText("Congratulations, Winner is " + winnerSymbol);

            // Highlight winning combination
            int[] winPattern = engine.getWinningPattern();
            if (winPattern != null) {
                for (int idx : winPattern) {
                    boxes[idx].getStyleClass().add("box-winning");
                }
            }
        }

        msgContainer.setVisible(true);
        msgContainer.setManaged(true);

        // Save game and update statistics asynchronously if not already saved
        if (!gameSaved) {
            gameSaved = true;
            persistGameResults();
        }
    }

    private void persistGameResults() {
        Thread saveThread = new Thread(() -> {
            try {
                String winnerSymbol = engine.getWinner();
                boolean isDraw = engine.isDraw();
                String winnerName = isDraw ? "Draw" : ("O".equals(winnerSymbol) ? playerO : playerX);

                // 1. Create and save GameRecord
                GameRecord record = new GameRecord(
                        playerO,
                        playerX,
                        winnerSymbol,
                        winnerName,
                        engine.getMoveHistory(),
                        engine.getStartedAt(),
                        engine.getEndedAt() != null ? engine.getEndedAt() : new Date()
                );
                controller.getGameRepository().saveGame(record);

                // 2. Update player statistics atomically
                if (isDraw) {
                    controller.getPlayerRepository().updatePlayerStats(playerO, false, true);
                    controller.getPlayerRepository().updatePlayerStats(playerX, false, true);
                } else {
                    boolean oWon = "O".equals(winnerSymbol);
                    controller.getPlayerRepository().updatePlayerStats(playerO, oWon, false);
                    controller.getPlayerRepository().updatePlayerStats(playerX, !oWon, false);
                }
            } catch (Exception e) {
                System.err.println("Error saving game results: " + e.getMessage());
            }
        });
        saveThread.setDaemon(true);
        saveThread.start();
    }

    private void resetGame() {
        engine.reset();
        gameSaved = false;

        for (Button box : boxes) {
            box.setText("");
            box.setDisable(false);
            box.getStyleClass().remove("box-winning");
        }

        msgContainer.setVisible(false);
        msgContainer.setManaged(false);

        turnLabel.setVisible(true);
        updateTurnDisplay();
    }

    private void disableAllBoxes() {
        for (Button box : boxes) {
            box.setDisable(true);
        }
    }

    private void updateTurnDisplay() {
        if (engine.isTurnO()) {
            turnLabel.setText("Current Turn: " + playerO + " (O)");
        } else {
            turnLabel.setText("Current Turn: " + playerX + " (X)");
        }
    }

    public Pane getView() {
        return rootStack;
    }
}
