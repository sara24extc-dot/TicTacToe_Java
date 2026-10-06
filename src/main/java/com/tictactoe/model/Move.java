package com.tictactoe.model;

import org.bson.Document;

import java.util.Date;

/**
 * Represents a single move made in a game.
 */
public class Move {
    private int moveNumber;
    private String symbol; // "O" or "X"
    private int boxIndex;  // 0 to 8
    private Date timestamp;

    public Move() {
        this.timestamp = new Date();
    }

    public Move(int moveNumber, String symbol, int boxIndex) {
        this.moveNumber = moveNumber;
        this.symbol = symbol;
        this.boxIndex = boxIndex;
        this.timestamp = new Date();
    }

    public Move(int moveNumber, String symbol, int boxIndex, Date timestamp) {
        this.moveNumber = moveNumber;
        this.symbol = symbol;
        this.boxIndex = boxIndex;
        this.timestamp = timestamp != null ? timestamp : new Date();
    }

    public static Move fromDocument(Document doc) {
        if (doc == null) return null;
        Move move = new Move();
        move.setMoveNumber(doc.getInteger("moveNumber", 0));
        move.setSymbol(doc.getString("symbol"));
        move.setBoxIndex(doc.getInteger("boxIndex", 0));
        move.setTimestamp(doc.getDate("timestamp"));
        return move;
    }

    public Document toDocument() {
        return new Document("moveNumber", moveNumber)
                .append("symbol", symbol)
                .append("boxIndex", boxIndex)
                .append("timestamp", timestamp != null ? timestamp : new Date());
    }

    // Getters and Setters
    public int getMoveNumber() {
        return moveNumber;
    }

    public void setMoveNumber(int moveNumber) {
        this.moveNumber = moveNumber;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getBoxIndex() {
        return boxIndex;
    }

    public void setBoxIndex(int boxIndex) {
        this.boxIndex = boxIndex;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
