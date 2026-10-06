package com.tictactoe.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.tictactoe.model.Player;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Data access repository for the 'players' collection.
 */
public class PlayerRepository {

    private static final Logger logger = LoggerFactory.getLogger(PlayerRepository.class);
    private static final String COLLECTION_NAME = "players";

    private final MongoConnection mongoConnection;

    public PlayerRepository() {
        this.mongoConnection = MongoConnection.getInstance();
    }

    public PlayerRepository(MongoConnection mongoConnection) {
        this.mongoConnection = mongoConnection;
    }

    private MongoCollection<Document> getCollection() {
        if (!mongoConnection.isConnected() || mongoConnection.getDatabase() == null) {
            return null;
        }
        return mongoConnection.getDatabase().getCollection(COLLECTION_NAME);
    }

    /**
     * Finds an existing player by username or creates a new one.
     *
     * @param username The player username.
     * @return The Player object, or a fallback in-memory Player if offline.
     */
    public Player getOrCreatePlayer(String username) {
        String cleanUsername = username != null ? username.trim() : "Player";
        if (cleanUsername.isEmpty()) {
            cleanUsername = "Player";
        }

        MongoCollection<Document> col = getCollection();
        if (col == null) {
            logger.info("MongoDB unavailable, returning offline Player: {}", cleanUsername);
            return new Player(cleanUsername);
        }

        try {
            Document foundDoc = col.find(Filters.eq("username", cleanUsername)).first();
            if (foundDoc != null) {
                logger.info("Found existing player: {}", cleanUsername);
                return Player.fromDocument(foundDoc);
            }

            Player newPlayer = new Player(cleanUsername);
            col.insertOne(newPlayer.toDocument());
            logger.info("Created new player document for: {}", cleanUsername);
            return newPlayer;
        } catch (Exception e) {
            logger.error("Error fetching or creating player '{}': {}", cleanUsername, e.getMessage());
            return new Player(cleanUsername);
        }
    }

    /**
     * Atomically updates a player's statistics using MongoDB's $inc operator.
     *
     * @param username Player username.
     * @param isWinner Whether this player won.
     * @param isDraw   Whether the game ended in a draw.
     */
    public boolean updatePlayerStats(String username, boolean isWinner, boolean isDraw) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        MongoCollection<Document> col = getCollection();
        if (col == null) {
            logger.warn("MongoDB unavailable, skipping stats update for: {}", username);
            return false;
        }

        try {
            List<Bson> updates = new ArrayList<>();
            updates.add(Updates.inc("totalGames", 1));

            if (isDraw) {
                updates.add(Updates.inc("draws", 1));
            } else if (isWinner) {
                updates.add(Updates.inc("wins", 1));
            } else {
                updates.add(Updates.inc("losses", 1));
            }

            Bson combinedUpdates = Updates.combine(updates);
            col.updateOne(Filters.eq("username", username.trim()), combinedUpdates);
            logger.info("Updated stats for player: {} (winner={}, draw={})", username, isWinner, isDraw);
            return true;
        } catch (Exception e) {
            logger.error("Error updating player stats for '{}': {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the top players sorted by wins (descending).
     *
     * @param limit Maximum number of players to return.
     * @return List of top players.
     */
    public List<Player> getTopPlayers(int limit) {
        List<Player> players = new ArrayList<>();
        MongoCollection<Document> col = getCollection();
        if (col == null) {
            return players;
        }

        try {
            for (Document doc : col.find()
                    .sort(Sorts.descending("wins"))
                    .limit(limit)) {
                players.add(Player.fromDocument(doc));
            }
        } catch (Exception e) {
            logger.error("Error retrieving leaderboard players: {}", e.getMessage());
        }
        return players;
    }
}
