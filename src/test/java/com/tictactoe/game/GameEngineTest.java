package com.tictactoe.game;

import com.tictactoe.model.Move;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GameEngine covering all win patterns, draw detection,
 * turn alternation, and move validation.
 */
public class GameEngineTest {

    private GameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GameEngine();
    }

    @Test
    @DisplayName("Initial state: Player O starts, move count 0, board empty")
    void testInitialState() {
        assertTrue(engine.isTurnO());
        assertEquals("O", engine.getCurrentSymbol());
        assertEquals(0, engine.getMoveCount());
        assertFalse(engine.isGameOver());
        assertNull(engine.getWinner());
        assertNull(engine.getWinningPattern());

        for (int i = 0; i < 9; i++) {
            assertEquals("", engine.getBox(i));
        }
    }

    @Test
    @DisplayName("Turn alternation: O then X then O")
    void testTurnAlternation() {
        assertTrue(engine.isTurnO());
        assertTrue(engine.makeMove(0));
        assertEquals("O", engine.getBox(0));

        assertFalse(engine.isTurnO());
        assertEquals("X", engine.getCurrentSymbol());
        assertTrue(engine.makeMove(1));
        assertEquals("X", engine.getBox(1));

        assertTrue(engine.isTurnO());
        assertEquals("O", engine.getCurrentSymbol());
        assertTrue(engine.makeMove(2));
        assertEquals("O", engine.getBox(2));
    }

    @Test
    @DisplayName("Reject move on already occupied box")
    void testRejectOccupiedBox() {
        assertTrue(engine.makeMove(4)); // O on 4
        assertEquals("O", engine.getBox(4));
        assertEquals(1, engine.getMoveCount());

        // X tries to place on 4
        assertFalse(engine.makeMove(4));
        assertEquals("O", engine.getBox(4));
        assertEquals(1, engine.getMoveCount());
        assertFalse(engine.isTurnO()); // Still X's turn
    }

    @Test
    @DisplayName("Reject moves with invalid index")
    void testInvalidIndexMoves() {
        assertFalse(engine.makeMove(-1));
        assertFalse(engine.makeMove(9));
        assertFalse(engine.makeMove(100));
        assertEquals(0, engine.getMoveCount());
    }

    @Test
    @DisplayName("Reject move after game is over")
    void testRejectMoveAfterGameOver() {
        // O wins on top row [0, 1, 2]
        engine.makeMove(0); // O
        engine.makeMove(3); // X
        engine.makeMove(1); // O
        engine.makeMove(4); // X
        engine.makeMove(2); // O wins

        assertTrue(engine.isGameOver());
        assertEquals("O", engine.getWinner());

        // Any further move should be rejected
        assertFalse(engine.makeMove(5));
        assertFalse(engine.makeMove(8));
    }

    private static Stream<Arguments> provideAllWinPatterns() {
        return Stream.of(
                Arguments.of(new int[]{0, 1, 2}, "Row 1 [0,1,2]"),
                Arguments.of(new int[]{3, 4, 5}, "Row 2 [3,4,5]"),
                Arguments.of(new int[]{6, 7, 8}, "Row 3 [6,7,8]"),
                Arguments.of(new int[]{0, 3, 6}, "Col 1 [0,3,6]"),
                Arguments.of(new int[]{1, 4, 7}, "Col 2 [1,4,7]"),
                Arguments.of(new int[]{2, 5, 8}, "Col 3 [2,5,8]"),
                Arguments.of(new int[]{0, 4, 8}, "Diag 1 [0,4,8]"),
                Arguments.of(new int[]{2, 4, 6}, "Diag 2 [2,4,6]")
        );
    }

    @ParameterizedTest(name = "Win Pattern Test: {1}")
    @MethodSource("provideAllWinPatterns")
    @DisplayName("Player O wins across all 8 winning patterns")
    void testAllWinPatternsForPlayerO(int[] pattern, String description) {
        // Find 2 dummy positions for X that are not in the win pattern
        int[] dummyX = new int[2];
        int dIdx = 0;
        for (int i = 0; i < 9 && dIdx < 2; i++) {
            boolean inPattern = false;
            for (int p : pattern) {
                if (p == i) {
                    inPattern = true;
                    break;
                }
            }
            if (!inPattern) {
                dummyX[dIdx++] = i;
            }
        }

        // Move 1: O on pattern[0]
        assertTrue(engine.makeMove(pattern[0]));
        // Move 2: X on dummyX[0]
        assertTrue(engine.makeMove(dummyX[0]));
        // Move 3: O on pattern[1]
        assertTrue(engine.makeMove(pattern[1]));
        // Move 4: X on dummyX[1]
        assertTrue(engine.makeMove(dummyX[1]));
        // Move 5: O on pattern[2] -> WIN!
        assertTrue(engine.makeMove(pattern[2]));

        assertTrue(engine.isGameOver(), "Game should be over for " + description);
        assertEquals("O", engine.getWinner(), "Winner should be O for " + description);
        assertNotNull(engine.getWinningPattern());
        assertArrayEquals(pattern, engine.getWinningPattern());
    }

    @Test
    @DisplayName("Player X wins test")
    void testPlayerXWins() {
        // O: 0, X: 3, O: 1, X: 4, O: 8, X: 5 -> X wins row [3,4,5]
        assertTrue(engine.makeMove(0)); // O
        assertTrue(engine.makeMove(3)); // X
        assertTrue(engine.makeMove(1)); // O
        assertTrue(engine.makeMove(4)); // X
        assertTrue(engine.makeMove(8)); // O
        assertTrue(engine.makeMove(5)); // X wins!

        assertTrue(engine.isGameOver());
        assertEquals("X", engine.getWinner());
        assertFalse(engine.isDraw());
        assertArrayEquals(new int[]{3, 4, 5}, engine.getWinningPattern());
    }

    @Test
    @DisplayName("Draw test: 9 moves with no winner")
    void testDrawGame() {
        // Board layout for draw:
        // O X O
        // O X X
        // X O O
        // Indices:
        // 0: O, 1: X, 2: O
        // 3: O, 4: X, 5: X
        // 6: X, 7: O, 8: O
        // Sequence:
        // O: 0, X: 1
        // O: 2, X: 4
        // O: 3, X: 5
        // O: 7, X: 6
        // O: 8
        assertTrue(engine.makeMove(0)); // O (move 1)
        assertTrue(engine.makeMove(1)); // X (move 2)
        assertTrue(engine.makeMove(2)); // O (move 3)
        assertTrue(engine.makeMove(4)); // X (move 4)
        assertTrue(engine.makeMove(3)); // O (move 5)
        assertTrue(engine.makeMove(5)); // X (move 6)
        assertTrue(engine.makeMove(7)); // O (move 7)
        assertTrue(engine.makeMove(6)); // X (move 8)
        assertTrue(engine.makeMove(8)); // O (move 9)

        assertTrue(engine.isGameOver());
        assertTrue(engine.isDraw());
        assertEquals("DRAW", engine.getWinner());
        assertNull(engine.getWinningPattern());
        assertEquals(9, engine.getMoveCount());
    }

    @Test
    @DisplayName("Reset game restores initial state")
    void testReset() {
        engine.makeMove(0);
        engine.makeMove(1);
        engine.makeMove(2);

        assertEquals(3, engine.getMoveCount());
        assertFalse(engine.getMoveHistory().isEmpty());

        engine.reset();

        assertEquals(0, engine.getMoveCount());
        assertTrue(engine.isTurnO());
        assertFalse(engine.isGameOver());
        assertNull(engine.getWinner());
        assertTrue(engine.getMoveHistory().isEmpty());
        for (int i = 0; i < 9; i++) {
            assertEquals("", engine.getBox(i));
        }
    }

    @Test
    @DisplayName("Move history correctly tracks symbol, index, and moveNumber")
    void testMoveHistory() {
        engine.makeMove(4); // O
        engine.makeMove(0); // X
        engine.makeMove(8); // O

        List<Move> history = engine.getMoveHistory();
        assertEquals(3, history.size());

        assertEquals(1, history.get(0).getMoveNumber());
        assertEquals("O", history.get(0).getSymbol());
        assertEquals(4, history.get(0).getBoxIndex());

        assertEquals(2, history.get(1).getMoveNumber());
        assertEquals("X", history.get(1).getSymbol());
        assertEquals(0, history.get(1).getBoxIndex());

        assertEquals(3, history.get(2).getMoveNumber());
        assertEquals("O", history.get(2).getSymbol());
        assertEquals(8, history.get(2).getBoxIndex());
    }
}
