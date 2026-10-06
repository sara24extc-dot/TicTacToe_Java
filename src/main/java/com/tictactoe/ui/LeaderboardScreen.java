package com.tictactoe.ui;

import com.tictactoe.model.Player;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Leaderboard screen displaying top players, win counts, and win rates.
 */
public class LeaderboardScreen {

    private final ScreenController controller;
    private final VBox root;
    private final TableView<Player> table;
    private final ObservableList<Player> playerList;
    private final Label statusLabel;

    public LeaderboardScreen(ScreenController controller) {
        this.controller = controller;
        this.root = new VBox(20);
        this.root.setAlignment(Pos.CENTER);
        this.root.setStyle("-fx-padding: 24px;");

        this.table = new TableView<>();
        this.playerList = FXCollections.observableArrayList();
        this.statusLabel = new Label();
        this.statusLabel.setStyle("-fx-text-fill: #ffffc7; -fx-font-size: 14px;");

        buildUI();
        loadLeaderboardData();
    }

    private void buildUI() {
        Label titleLabel = new Label("Leaderboard");
        titleLabel.getStyleClass().add("game-title");

        Label subtitle = new Label("Top 10 Players by Wins");
        subtitle.getStyleClass().add("subtitle");

        table.getStyleClass().add("custom-table");
        table.setPrefWidth(680);
        table.setPrefHeight(360);
        table.setMaxWidth(680);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Rank Column
        TableColumn<Player, Integer> rankCol = new TableColumn<>("#");
        rankCol.setPrefWidth(50);
        rankCol.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(table.getItems().indexOf(cellData.getValue()) + 1));

        // Username Column
        TableColumn<Player, String> userCol = new TableColumn<>("Username");
        userCol.setPrefWidth(160);
        userCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUsername()));

        // Wins Column
        TableColumn<Player, Integer> winsCol = new TableColumn<>("Wins");
        winsCol.setPrefWidth(80);
        winsCol.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getWins()));

        // Losses Column
        TableColumn<Player, Integer> lossCol = new TableColumn<>("Losses");
        lossCol.setPrefWidth(80);
        lossCol.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getLosses()));

        // Draws Column
        TableColumn<Player, Integer> drawCol = new TableColumn<>("Draws");
        drawCol.setPrefWidth(80);
        drawCol.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getDraws()));

        // Total Games Column
        TableColumn<Player, Integer> totalCol = new TableColumn<>("Total");
        totalCol.setPrefWidth(80);
        totalCol.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getTotalGames()));

        // Win Rate Column
        TableColumn<Player, String> winRateCol = new TableColumn<>("Win Rate");
        winRateCol.setPrefWidth(100);
        winRateCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getWinRateFormatted()));

        table.getColumns().addAll(rankCol, userCol, winsCol, lossCol, drawCol, totalCol, winRateCol);
        table.setItems(playerList);

        // Buttons
        HBox buttonBox = new HBox(16);
        buttonBox.setAlignment(Pos.CENTER);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("dark-button");
        refreshBtn.setOnAction(e -> loadLeaderboardData());

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

    private void loadLeaderboardData() {
        playerList.clear();
        Thread thread = new Thread(() -> {
            try {
                List<Player> topPlayers = controller.getPlayerRepository().getTopPlayers(10);
                javafx.application.Platform.runLater(() -> {
                    playerList.addAll(topPlayers);
                    if (topPlayers.isEmpty()) {
                        statusLabel.setText("No player records found. Play games to populate the leaderboard!");
                    } else {
                        statusLabel.setText("Showing top " + topPlayers.size() + " players.");
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                        statusLabel.setText("Could not load leaderboard: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public Pane getView() {
        return root;
    }
}
