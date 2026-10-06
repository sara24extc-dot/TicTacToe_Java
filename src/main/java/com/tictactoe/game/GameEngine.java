package com.tictactoe.game;

import com.tictactoe.model.Move;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Pure, UI-independent game logic for Tic-Tac-Toe.
 * Follows the exact rules and win patterns of the original web project.
 */
public class GameEngine {

    public static final int[][] WIN_PATTERNS = {
            {0, 1, 2},
            {0, 3, 6},
            {0, 4, 8},
            {1, 4, 7},
            {2, 5, 8},
            {2, 4, 6},
            {3, 4, 5},
            {6, 7, 8}
    };

    private final String[] board;
    private boolean turnO; // true = Player O, false = Player X
    private int moveCount;
    private boolean gameOver;
    private String winner; // "O", "X", "DRAW", or null if game is ongoing
    private int[] winningPattern;
    private final List<Move> moveHistory;
    private Date startedAt;
    private Date endedAt;

    public GameEngine() {
        this.board = new String[9];
        this.moveHistory = new ArrayList<>();
        reset();
    }

    /**
     * Resets the game to the initial state.
     * Player O always goes first, move count is 0, board is cleared.
     */
    public void reset() {
        Arrays.fill(board, "");
        this.turnO = true;
        this.moveCount = 0;
        this.gameOver = false;
        this.winner = null;
        this.winningPattern = null;
        this.moveHistory.clear();
        this.startedAt = new Date();
        this.endedAt = null;
    }

    /**
     * Attempts to place the current player's symbol in the given box index (0-8).
     *
     * @param boxIndex The index on the 3x3 board (0 to 8).
     * @return true if the move was successfully made; false if invalid or game is over.
     */
    public boolean makeMove(int boxIndex) {
        if (boxIndex < 0 || boxIndex >= 9) {
            return false;
        }
        if (gameOver || !board[boxIndex].isEmpty()) {
            return false;
        }

        String symbol = turnO ? "O" : "X";
        board[boxIndex] = symbol;
        moveCount++;

        moveHistory.add(new Move(moveCount, symbol, boxIndex, new Date()));

        int[] winPattern = checkWinnerPattern();
        if (winPattern != null) {
            gameOver = true;
            winner = symbol;
            winningPattern = winPattern;
            endedAt = new Date();
            return true;
        }

        if (moveCount == 9) {
            gameOver = true;
            winner = "DRAW";
            winningPattern = null;
            endedAt = new Date();
            return true;
        }

        // Alternate turn
        turnO = !turnO;
        return true;
    }

    /**
     * Checks if any of the 8 win patterns are satisfied on the board.
     *
     * @return The winning pattern array if found, otherwise null.
     */
    public int[] checkWinnerPattern() {
        for (int[] pattern : WIN_PATTERNS) {
            String val1 = board[pattern[0]];
            String val2 = board[pattern[1]];
            String val3 = board[pattern[2]];

            if (!val1.isEmpty() && !val2.isEmpty() && !val3.isEmpty()) {
                if (val1.equals(val2) && val2.equals(val3)) {
                    return pattern;
                }
            }
        }
        return null;
    }

    // Query Methods
    public String getCurrentSymbol() {
        return turnO ? "O" : "X";
    }

    public boolean isTurnO() {
        return turnO;
    }

    public String getBox(int index) {
        if (index < 0 || index >= 9) return "";
        return board[index];
    }

    public String[] getBoard() {
        return Arrays.copyOf(board, board.length);
    }

    public int getMoveCount() {
        return moveCount;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public String getWinner() {
        return winner;
    }

    public boolean isDraw() {
        return "DRAW".equals(winner);
    }

    public int[] getWinningPattern() {
        return winningPattern != null ? Arrays.copyOf(winningPattern, winningPattern.length) : null;
    }

    public List<Move> getMoveHistory() {
        return new ArrayList<>(moveHistory);
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public Date getEndedAt() {
        return endedAt;
    }
}
