package com.tictactoe.model;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.Date;

/**
 * Represents a registered player and their game statistics.
 */
public class Player {
    private ObjectId id;
    private String username;
    private Date createdAt;
    private int totalGames;
    private int wins;
    private int losses;
    private int draws;

    public Player() {
        this.createdAt = new Date();
    }

    public Player(String username) {
        this.username = username;
        this.createdAt = new Date();
        this.totalGames = 0;
        this.wins = 0;
        this.losses = 0;
        this.draws = 0;
    }

    public Player(ObjectId id, String username, Date createdAt, int totalGames, int wins, int losses, int draws) {
        this.id = id;
        this.username = username;
        this.createdAt = createdAt != null ? createdAt : new Date();
        this.totalGames = totalGames;
        this.wins = wins;
        this.losses = losses;
        this.draws = draws;
    }

    /**
     * Converts MongoDB Document to Player instance.
     */
    public static Player fromDocument(Document doc) {
        if (doc == null) return null;
        Player player = new Player();
        player.setId(doc.getObjectId("_id"));
        player.setUsername(doc.getString("username"));
        player.setCreatedAt(doc.getDate("createdAt"));
        player.setTotalGames(doc.getInteger("totalGames", 0));
        player.setWins(doc.getInteger("wins", 0));
        player.setLosses(doc.getInteger("losses", 0));
        player.setDraws(doc.getInteger("draws", 0));
        return player;
    }

    /**
     * Converts Player instance to MongoDB Document.
     */
    public Document toDocument() {
        Document doc = new Document();
        if (id != null) {
            doc.append("_id", id);
        }
        doc.append("username", username)
           .append("createdAt", createdAt != null ? createdAt : new Date())
           .append("totalGames", totalGames)
           .append("wins", wins)
           .append("losses", losses)
           .append("draws", draws);
        return doc;
    }

    public double getWinRate() {
        if (totalGames == 0) return 0.0;
        return ((double) wins / totalGames) * 100.0;
    }

    public String getWinRateFormatted() {
        return String.format("%.1f%%", getWinRate());
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getTotalGames() {
        return totalGames;
    }

    public void setTotalGames(int totalGames) {
        this.totalGames = totalGames;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getDraws() {
        return draws;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }
}
