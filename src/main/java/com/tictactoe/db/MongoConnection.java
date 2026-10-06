package com.tictactoe.db;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Singleton database connection manager for MongoDB.
 * Connects to mongodb://localhost:27017 (or MONGO_URI env variable)
 * and database "tictactoe_db".
 * Handles connection failures gracefully without crashing.
 */
public class MongoConnection {

    private static final Logger logger = LoggerFactory.getLogger(MongoConnection.class);

    private static final String DEFAULT_URI = "mongodb://localhost:27017";
    private static final String DEFAULT_DB = "tictactoe_db";

    private static volatile MongoConnection instance;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private boolean connected = false;
    private String statusMessage = "Not connected";

    private MongoConnection() {
        initConnection();
    }

    public static MongoConnection getInstance() {
        if (instance == null) {
            synchronized (MongoConnection.class) {
                if (instance == null) {
                    instance = new MongoConnection();
                }
            }
        }
        return instance;
    }

    public synchronized void initConnection() {
        String uri = System.getenv("MONGO_URI");
        if (uri == null || uri.trim().isEmpty()) {
            uri = System.getProperty("mongo.uri", DEFAULT_URI);
        }

        String dbName = System.getenv("MONGO_DB");
        if (dbName == null || dbName.trim().isEmpty()) {
            dbName = System.getProperty("mongo.db", DEFAULT_DB);
        }

        try {
            logger.info("Attempting to connect to MongoDB at: {}", uri);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(uri))
                    .applyToClusterSettings(builder ->
                            builder.serverSelectionTimeout(2500, TimeUnit.MILLISECONDS))
                    .applyToSocketSettings(builder ->
                            builder.connectTimeout(2500, TimeUnit.MILLISECONDS))
                    .build();

            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(dbName);

            // Ping database to verify connection immediately
            database.runCommand(new Document("ping", 1));

            connected = true;
            statusMessage = "Connected to MongoDB (" + dbName + ")";
            logger.info("Successfully connected to MongoDB database: {}", dbName);

            // Ensure indexes
            createIndexes();
        } catch (Exception e) {
            connected = false;
            statusMessage = "MongoDB offline (" + e.getMessage() + ")";
            logger.warn("Could not connect to MongoDB. Game will run in Offline Mode. Error: {}", e.getMessage());
        }
    }

    private void createIndexes() {
        if (!connected || database == null) return;
        try {
            // 1. Unique index on players.username
            IndexOptions uniqueUsernameOption = new IndexOptions().unique(true);
            database.getCollection("players").createIndex(Indexes.ascending("username"), uniqueUsernameOption);
            logger.info("Ensured unique index on players.username");

            // 2. Descending index on games.endedAt
            database.getCollection("games").createIndex(Indexes.descending("endedAt"));
            logger.info("Ensured descending index on games.endedAt");
        } catch (Exception e) {
            logger.warn("Failed to create database indexes: {}", e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public synchronized void close() {
        if (mongoClient != null) {
            try {
                logger.info("Closing MongoDB connection...");
                mongoClient.close();
            } catch (Exception e) {
                logger.warn("Error while closing MongoDB client: {}", e.getMessage());
            } finally {
                connected = false;
                mongoClient = null;
                database = null;
            }
        }
    }
}
