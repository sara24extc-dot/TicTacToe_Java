package com.tictactoe.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.tictactoe.model.GameRecord;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Data access repository for the 'games' collection.
 */
public class GameRepository {

    private static final Logger logger = LoggerFactory.getLogger(GameRepository.class);
    private static final String COLLECTION_NAME = "games";

    private final MongoConnection mongoConnection;

    public GameRepository() {
        this.mongoConnection = MongoConnection.getInstance();
    }

    public GameRepository(MongoConnection mongoConnection) {
        this.mongoConnection = mongoConnection;
    }

    private MongoCollection<Document> getCollection() {
        if (!mongoConnection.isConnected() || mongoConnection.getDatabase() == null) {
            return null;
        }
        return mongoConnection.getDatabase().getCollection(COLLECTION_NAME);
    }

    /**
     * Saves a finished game record to MongoDB.
     *
     * @param record The GameRecord to save.
     * @return true if saved successfully; false otherwise.
     */
    public boolean saveGame(GameRecord record) {
        if (record == null) return false;

        MongoCollection<Document> col = getCollection();
        if (col == null) {
            logger.warn("MongoDB unavailable, skipping game save.");
            return false;
        }

        try {
            Document doc = record.toDocument();
            col.insertOne(doc);
            record.setId(doc.getObjectId("_id"));
            logger.info("Successfully saved game record with ID: {}", record.getId());
            return true;
        } catch (Exception e) {
            logger.error("Failed to save game record: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the most recent games sorted by endedAt descending.
     *
     * @param limit Number of games to retrieve (e.g. 20).
     * @return List of GameRecord objects.
     */
    public List<GameRecord> getRecentGames(int limit) {
        List<GameRecord> games = new ArrayList<>();
        MongoCollection<Document> col = getCollection();
        if (col == null) {
            return games;
        }

        try {
            for (Document doc : col.find()
                    .sort(Sorts.descending("endedAt"))
                    .limit(limit)) {
                games.add(GameRecord.fromDocument(doc));
            }
        } catch (Exception e) {
            logger.error("Error retrieving recent games: {}", e.getMessage());
        }
        return games;
    }

    /**
     * Finds a game record by its ObjectId string.
     *
     * @param idString The hex string of the ObjectId.
     * @return The GameRecord or null if not found.
     */
    public GameRecord getGameById(String idString) {
        if (idString == null || idString.trim().isEmpty()) return null;

        MongoCollection<Document> col = getCollection();
        if (col == null) return null;

        try {
            ObjectId objId = new ObjectId(idString.trim());
            Document doc = col.find(Filters.eq("_id", objId)).first();
            return GameRecord.fromDocument(doc);
        } catch (Exception e) {
            logger.error("Error retrieving game by ID '{}': {}", idString, e.getMessage());
            return null;
        }
    }
}
