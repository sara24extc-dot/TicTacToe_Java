package com.tictactoe.ui;

import com.tictactoe.model.GameRecord;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Game history screen displaying the last 20 recorded matches with replay functionality.
 */
public class HistoryScreen {

    private final ScreenController controller;
    private final VBox root;
    private final TableView<GameRecord> table;
    private final ObservableList<GameRecord> gameList;
    private final Label statusLabel;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public HistoryScreen(ScreenController controller) {
        this.controller = controller;
        this.root = new VBox(20);
        this.root.setAlignment(Pos.CENTER);
        this.root.setStyle("-fx-padding: 24px;");

        this.table = new TableView<>();
        this.gameList = FXCollections.observableArrayList();
        this.statusLabel = new Label();
        this.statusLabel.setStyle("-fx-text-fill: #ffffc7; -fx-font-size: 14px;");

        buildUI();
        loadHistoryData();
    }

    private void buildUI() {
        Label titleLabel = new Label("Game History");
        titleLabel.getStyleClass().add("game-title");

        Label subtitle = new Label("Recent 20 Games (Sorted by Date)");
        subtitle.getStyleClass().add("subtitle");

        table.getStyleClass().add("custom-table");
        table.setPrefWidth(720);
        table.setPrefHeight(360);
        table.setMaxWidth(720);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Date Column
        TableColumn<GameRecord, String> dateCol = new TableColumn<>("Date & Time");
        dateCol.setPrefWidth(160);
        dateCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getEndedAt() != null) {
                return new SimpleStringProperty(dateFormat.format(cellData.getValue().getEndedAt()));
            }
            return new SimpleStringProperty("-");
        });

        // Player O Column
        TableColumn<GameRecord, String> oCol = new TableColumn<>("Player O");
        oCol.setPrefWidth(110);
        oCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPlayerO()));

        // Player X Column
        TableColumn<GameRecord, String> xCol = new TableColumn<>("Player X");
        xCol.setPrefWidth(110);
        xCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPlayerX()));

        // Winner Column
        TableColumn<GameRecord, String> winnerCol = new TableColumn<>("Winner");
        winnerCol.setPrefWidth(110);
        winnerCol.setCellValueFactory(cellData -> {
            String winner = cellData.getValue().getWinner();
            if ("DRAW".equalsIgnoreCase(winner)) {
                return new SimpleStringProperty("Draw");
            }
            return new SimpleStringProperty(cellData.getValue().getWinnerName() + " (" + winner + ")");
        });

        // Total Moves Column
        TableColumn<GameRecord, Integer> movesCol = new TableColumn<>("Moves");
        movesCol.setPrefWidth(70);
        movesCol.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getTotalMoves()));

        // Duration Column
        TableColumn<GameRecord, String> durationCol = new TableColumn<>("Duration");
        durationCol.setPrefWidth(80);
        durationCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDurationSeconds() + "s"));

        // Action Column: Replay Button
        TableColumn<GameRecord, Void> actionCol = new TableColumn<>("Replay");
        actionCol.setPrefWidth(90);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button replayBtn = new Button("Replay");
            {
                replayBtn.setStyle("-fx-background-color: #191913; -fx-text-fill: #ffffff; -fx-font-size: 12px; -fx-padding: 4px 10px; -fx-background-radius: 8px; -fx-cursor: hand;");
                replayBtn.setOnAction(event -> {
                    GameRecord record = getTableView().getItems().get(getIndex());
                    if (record != null) {
                        ReplayDialog dialog = new ReplayDialog(controller.getPrimaryStage(), record);
                        dialog.show();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(replayBtn);
                }
            }
        });

        table.getColumns().addAll(dateCol, oCol, xCol, winnerCol, movesCol, durationCol, actionCol);
        table.setItems(gameList);

        // Buttons
        HBox buttonBox = new HBox(16);
        buttonBox.setAlignment(Pos.CENTER);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("dark-button");
        refreshBtn.setOnAction(e -> loadHistoryData());

        Button playBtn = new Button("Play Game");
        playBtn.getStyleClass().add("dark-button");
        playBtn.setOnAction(e -> controller.showGameScreen(controller.getCurrentPlayerO(), controller.getCurrentPlayerX()));

        Button menuBtn = new Button("Back to Menu");
        menuBtn.getStyleClass().add("dark-button");
        menuBtn.setOnAction(e -> controller.showStartScreen());

        buttonBox.getChildren().addAll(refreshBtn, playBtn, menuBtn);

        root.getChildren().addAll(
                titleLabel,
                subtitle,
                table,
                statusLabel,
                buttonBox
        );
    }

    private void loadHistoryData() {
        gameList.clear();
        Thread thread = new Thread(() -> {
            try {
                List<GameRecord> recentGames = controller.getGameRepository().getRecentGames(20);
                javafx.application.Platform.runLater(() -> {
                    gameList.addAll(recentGames);
                    if (recentGames.isEmpty()) {
                        statusLabel.setText("No saved games found yet. Play a game to record history!");
                    } else {
                        statusLabel.setText("Showing " + recentGames.size() + " recent games.");
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                        statusLabel.setText("Could not load history: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public Pane getView() {
        return root;
    }
}
