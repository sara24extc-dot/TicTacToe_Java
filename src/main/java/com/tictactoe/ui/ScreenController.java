package com.tictactoe.ui;

import com.tictactoe.db.GameRepository;
import com.tictactoe.db.MongoConnection;
import com.tictactoe.db.PlayerRepository;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Manages scenes and navigation between screens in the application.
 */
public class ScreenController {

    private final Stage primaryStage;
    private final StackPane rootContainer;
    private final Scene mainScene;

    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;

    private String currentPlayerO = "Player O";
    private String currentPlayerX = "Player X";

    public ScreenController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.rootContainer = new StackPane();
        this.rootContainer.getStyleClass().add("main-container");

        this.mainScene = new Scene(rootContainer, 850, 720);
        String cssPath = getClass().getResource("/style.css") != null
                ? getClass().getResource("/style.css").toExternalForm()
                : null;
        if (cssPath != null) {
            this.mainScene.getStylesheets().add(cssPath);
        }

        this.playerRepository = new PlayerRepository();
        this.gameRepository = new GameRepository();

        this.primaryStage.setTitle("Tic-Tac-Toe Game");
        this.primaryStage.setScene(mainScene);
        this.primaryStage.setMinWidth(700);
        this.primaryStage.setMinHeight(650);
    }

    public void setView(Pane view) {
        rootContainer.getChildren().clear();
        rootContainer.getChildren().add(view);
    }

    public void showStartScreen() {
        StartScreen startScreen = new StartScreen(this);
        setView(startScreen.getView());
    }

    public void showGameScreen(String playerO, String playerX) {
        this.currentPlayerO = (playerO != null && !playerO.trim().isEmpty()) ? playerO.trim() : "Player O";
        this.currentPlayerX = (playerX != null && !playerX.trim().isEmpty()) ? playerX.trim() : "Player X";

        GameScreen gameScreen = new GameScreen(this, currentPlayerO, currentPlayerX);
        setView(gameScreen.getView());
    }

    public void showLeaderboardScreen() {
        LeaderboardScreen leaderboardScreen = new LeaderboardScreen(this);
        setView(leaderboardScreen.getView());
    }

    public void showHistoryScreen() {
        HistoryScreen historyScreen = new HistoryScreen(this);
        setView(historyScreen.getView());
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public PlayerRepository getPlayerRepository() {
        return playerRepository;
    }

    public GameRepository getGameRepository() {
        return gameRepository;
    }

    public String getCurrentPlayerO() {
        return currentPlayerO;
    }

    public String getCurrentPlayerX() {
        return currentPlayerX;
    }
}
