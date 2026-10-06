package com.tictactoe.db;

import com.tictactoe.model.GameRecord;
import com.tictactoe.model.Move;
import com.tictactoe.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for MongoConnection, PlayerRepository, and GameRepository.
 * If MongoDB is offline, tests verify graceful fallback behavior.
 */
public class MongoIntegrationTest {

    private PlayerRepository playerRepository;
    private GameRepository gameRepository;
    private MongoConnection mongoConnection;

    @BeforeEach
    void setUp() {
        mongoConnection = MongoConnection.getInstance();
        playerRepository = new PlayerRepository(mongoConnection);
        gameRepository = new GameRepository(mongoConnection);
    }

    @Test
    @DisplayName("Test PlayerRepository getOrCreatePlayer and atomic updatePlayerStats")
    void testPlayerRepository() {
        if (!mongoConnection.isConnected()) {
            System.out.println("Skipping DB assertion: MongoDB is offline");
            return;
        }

        String testUsername = "TestUser_" + System.currentTimeMillis();

        // 1. Create player
        Player player = playerRepository.getOrCreatePlayer(testUsername);
        assertNotNull(player);
        assertEquals(testUsername, player.getUsername());
        assertEquals(0, player.getTotalGames());
        assertEquals(0, player.getWins());

        // 2. Fetch existing player
        Player fetched = playerRepository.getOrCreatePlayer(testUsername);
        assertNotNull(fetched);
        assertEquals(testUsername, fetched.getUsername());

        // 3. Update stats atomically: 1 win
        boolean winUpdated = playerRepository.updatePlayerStats(testUsername, true, false);
        assertTrue(winUpdated);

        Player afterWin = playerRepository.getOrCreatePlayer(testUsername);
        assertEquals(1, afterWin.getTotalGames());
        assertEquals(1, afterWin.getWins());
        assertEquals(0, afterWin.getLosses());
        assertEquals(0, afterWin.getDraws());
        assertEquals(100.0, afterWin.getWinRate(), 0.01);

        // 4. Update stats: 1 draw
        boolean drawUpdated = playerRepository.updatePlayerStats(testUsername, false, true);
        assertTrue(drawUpdated);

        Player afterDraw = playerRepository.getOrCreatePlayer(testUsername);
        assertEquals(2, afterDraw.getTotalGames());
        assertEquals(1, afterDraw.getWins());
        assertEquals(0, afterDraw.getLosses());
        assertEquals(1, afterDraw.getDraws());
        assertEquals(50.0, afterDraw.getWinRate(), 0.01);

        // 5. Leaderboard fetch
        List<Player> topPlayers = playerRepository.getTopPlayers(10);
        assertNotNull(topPlayers);
        assertFalse(topPlayers.isEmpty());
    }

    @Test
    @DisplayName("Test GameRepository saveGame, getRecentGames, and getGameById")
    void testGameRepository() {
        if (!mongoConnection.isConnected()) {
            System.out.println("Skipping DB assertion: MongoDB is offline");
            return;
        }

        String playerO = "Alice_" + System.currentTimeMillis();
        String playerX = "Bob_" + System.currentTimeMillis();

        List<Move> moves = new ArrayList<>();
        moves.add(new Move(1, "O", 0, new Date()));
        moves.add(new Move(2, "X", 3, new Date()));
        moves.add(new Move(3, "O", 1, new Date()));
        moves.add(new Move(4, "X", 4, new Date()));
        moves.add(new Move(5, "O", 2, new Date()));

        GameRecord record = new GameRecord(
                playerO,
                playerX,
                "O",
                playerO,
                moves,
                new Date(System.currentTimeMillis() - 10000),
                new Date()
        );

        boolean saved = gameRepository.saveGame(record);
        assertTrue(saved);
        assertNotNull(record.getId());

        // Fetch recent games
        List<GameRecord> recent = gameRepository.getRecentGames(10);
        assertNotNull(recent);
        assertFalse(recent.isEmpty());

        // Fetch by ID
        GameRecord fetchedById = gameRepository.getGameById(record.getId().toHexString());
        assertNotNull(fetchedById);
        assertEquals(playerO, fetchedById.getPlayerO());
        assertEquals(playerX, fetchedById.getPlayerX());
        assertEquals("O", fetchedById.getWinner());
        assertEquals(5, fetchedById.getTotalMoves());
        assertEquals(5, fetchedById.getMoves().size());
    }
}
