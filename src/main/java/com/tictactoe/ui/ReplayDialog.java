package com.tictactoe.ui;

import com.tictactoe.model.GameRecord;
import com.tictactoe.model.Move;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

/**
 * Interactive dialog that replays a completed game move-by-move.
 */
public class ReplayDialog {

    private final Stage dialogStage;
    private final GameRecord gameRecord;
    private final List<Move> moves;
    private final Button[] boxes = new Button[9];

    private int currentMoveIndex = 0; // 0 means empty board, 1..moves.size()
    private final Label moveInfoLabel;
    private final Button prevBtn;
    private final Button nextBtn;
    private final Button playPauseBtn;
    private Timeline autoPlayTimeline;
    private boolean isPlaying = false;

    public ReplayDialog(Stage parentStage, GameRecord gameRecord) {
        this.gameRecord = gameRecord;
        this.moves = gameRecord.getMoves() != null ? gameRecord.getMoves() : List.of();

        this.dialogStage = new Stage();
        this.dialogStage.initModality(Modality.APPLICATION_MODAL);
        this.dialogStage.initOwner(parentStage);
        this.dialogStage.setTitle("Game Replay - " + gameRecord.getPlayerO() + " vs " + gameRecord.getPlayerX());

        VBox layout = new VBox(16);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #548687; -fx-padding: 24px;");

        // Title & Info
        Label titleLabel = new Label("Game Replay");
        titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        Label matchLabel = new Label(gameRecord.getPlayerO() + " (O)  vs  " + gameRecord.getPlayerX() + " (X)");
        matchLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffc7;");

        String outcomeText = "DRAW".equalsIgnoreCase(gameRecord.getWinner())
                ? "Result: Draw (" + gameRecord.getTotalMoves() + " moves)"
                : "Winner: " + gameRecord.getWinnerName() + " (" + gameRecord.getWinner() + ")";
        Label outcomeLabel = new Label(outcomeText);
        outcomeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffffc7;");

        this.moveInfoLabel = new Label("Move 0 of " + moves.size() + " (Game Start)");
        this.moveInfoLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ffffc7; -fx-background-color: rgba(0,0,0,0.2); -fx-background-radius: 8px; -fx-padding: 6px 12px;");

        // 3x3 Grid
        GridPane grid = new GridPane();
        grid.getStyleClass().add("board-grid");
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        for (int i = 0; i < 9; i++) {
            Button box = new Button("");
            box.getStyleClass().add("box");
            box.setStyle("-fx-min-width: 90px; -fx-min-height: 90px; -fx-pref-width: 90px; -fx-pref-height: 90px; -fx-font-size: 42px;");
            box.setDisable(true);
            boxes[i] = box;

            grid.add(box, i % 3, i / 3);
        }

        // Controls
        HBox controls = new HBox(12);
        controls.setAlignment(Pos.CENTER);

        Button startBtn = new Button("|<");
        startBtn.getStyleClass().add("dark-button");
        startBtn.setOnAction(e -> { stopAutoPlay(); goToMove(0); });

        prevBtn = new Button("< Prev");
        prevBtn.getStyleClass().add("dark-button");
        prevBtn.setOnAction(e -> { stopAutoPlay(); prevMove(); });

        playPauseBtn = new Button("Auto Play");
        playPauseBtn.getStyleClass().add("dark-button");
        playPauseBtn.setOnAction(e -> toggleAutoPlay());

        nextBtn = new Button("Next >");
        nextBtn.getStyleClass().add("dark-button");
        nextBtn.setOnAction(e -> { stopAutoPlay(); nextMove(); });

        Button endBtn = new Button(">|");
        endBtn.getStyleClass().add("dark-button");
        endBtn.setOnAction(e -> { stopAutoPlay(); goToMove(moves.size()); });

        controls.getChildren().addAll(startBtn, prevBtn, playPauseBtn, nextBtn, endBtn);

        Button closeBtn = new Button("Close Replay");
        closeBtn.getStyleClass().add("dark-button");
        closeBtn.setStyle("-fx-background-color: #191913; -fx-font-size: 15px;");
        closeBtn.setOnAction(e -> {
            stopAutoPlay();
            dialogStage.close();
        });

        layout.getChildren().addAll(titleLabel, matchLabel, outcomeLabel, moveInfoLabel, grid, controls, closeBtn);

        Scene scene = new Scene(layout, 550, 640);
        String cssPath = getClass().getResource("/style.css") != null
                ? getClass().getResource("/style.css").toExternalForm()
                : null;
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
        }
        dialogStage.setScene(scene);
        dialogStage.setResizable(false);

        setupAutoPlayTimeline();
        updateUI();
    }

    private void setupAutoPlayTimeline() {
        autoPlayTimeline = new Timeline(new KeyFrame(Duration.millis(850), e -> {
            if (currentMoveIndex < moves.size()) {
                nextMove();
            } else {
                stopAutoPlay();
            }
        }));
        autoPlayTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void toggleAutoPlay() {
        if (isPlaying) {
            stopAutoPlay();
        } else {
            if (currentMoveIndex >= moves.size()) {
                goToMove(0);
            }
            isPlaying = true;
            playPauseBtn.setText("Pause");
            autoPlayTimeline.play();
        }
    }

    private void stopAutoPlay() {
        isPlaying = false;
        playPauseBtn.setText("Auto Play");
        if (autoPlayTimeline != null) {
            autoPlayTimeline.stop();
        }
    }

    private void nextMove() {
        if (currentMoveIndex < moves.size()) {
            goToMove(currentMoveIndex + 1);
        }
    }

    private void prevMove() {
        if (currentMoveIndex > 0) {
            goToMove(currentMoveIndex - 1);
        }
    }

    private void goToMove(int targetStep) {
        if (targetStep < 0) targetStep = 0;
        if (targetStep > moves.size()) targetStep = moves.size();

        currentMoveIndex = targetStep;

        // Clear board
        for (Button b : boxes) {
            b.setText("");
            b.getStyleClass().remove("box-winning");
        }

        // Apply moves up to currentMoveIndex
        for (int i = 0; i < currentMoveIndex; i++) {
            Move m = moves.get(i);
            if (m.getBoxIndex() >= 0 && m.getBoxIndex() < 9) {
                boxes[m.getBoxIndex()].setText(m.getSymbol());
            }
        }

        updateUI();
    }

    private void updateUI() {
        prevBtn.setDisable(currentMoveIndex == 0);
        nextBtn.setDisable(currentMoveIndex >= moves.size());

        if (currentMoveIndex == 0) {
            moveInfoLabel.setText("Move 0 of " + moves.size() + " (Game Start)");
        } else {
            Move lastMove = moves.get(currentMoveIndex - 1);
            String mover = "O".equals(lastMove.getSymbol()) ? gameRecord.getPlayerO() : gameRecord.getPlayerX();
            moveInfoLabel.setText(String.format("Move %d of %d: %s placed '%s' on box %d",
                    currentMoveIndex, moves.size(), mover, lastMove.getSymbol(), lastMove.getBoxIndex()));
        }
    }

    public void show() {
        dialogStage.showAndWait();
    }
}
