package com.tictactoe.model;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a completed game record stored in MongoDB.
 */
public class GameRecord {
    private ObjectId id;
    private String playerO;
    private String playerX;
    private String winner;     // "O", "X", or "DRAW"
    private String winnerName; // Username of winner or "Draw"
    private List<Move> moves;
    private int totalMoves;
    private Date startedAt;
    private Date endedAt;
    private long durationSeconds;

    public GameRecord() {
        this.moves = new ArrayList<>();
        this.startedAt = new Date();
        this.endedAt = new Date();
    }

    public GameRecord(String playerO, String playerX, String winner, String winnerName,
                      List<Move> moves, Date startedAt, Date endedAt) {
        this.playerO = playerO;
        this.playerX = playerX;
        this.winner = winner;
        this.winnerName = winnerName;
        this.moves = moves != null ? moves : new ArrayList<>();
        this.totalMoves = this.moves.size();
        this.startedAt = startedAt != null ? startedAt : new Date();
        this.endedAt = endedAt != null ? endedAt : new Date();
        this.durationSeconds = (this.endedAt.getTime() - this.startedAt.getTime()) / 1000;
    }

    public static GameRecord fromDocument(Document doc) {
        if (doc == null) return null;
        GameRecord record = new GameRecord();
        record.setId(doc.getObjectId("_id"));
        record.setPlayerO(doc.getString("playerO"));
        record.setPlayerX(doc.getString("playerX"));
        record.setWinner(doc.getString("winner"));
        record.setWinnerName(doc.getString("winnerName"));
        record.setTotalMoves(doc.getInteger("totalMoves", 0));
        record.setStartedAt(doc.getDate("startedAt"));
        record.setEndedAt(doc.getDate("endedAt"));

        Object durationObj = doc.get("durationSeconds");
        if (durationObj instanceof Number) {
            record.setDurationSeconds(((Number) durationObj).longValue());
        }

        List<Document> moveDocs = doc.getList("moves", Document.class);
        List<Move> moveList = new ArrayList<>();
        if (moveDocs != null) {
            for (Document mDoc : moveDocs) {
                moveList.add(Move.fromDocument(mDoc));
            }
        }
        record.setMoves(moveList);
        return record;
    }

    public Document toDocument() {
        Document doc = new Document();
        if (id != null) {
            doc.append("_id", id);
        }
        List<Document> moveDocs = new ArrayList<>();
        if (moves != null) {
            for (Move m : moves) {
                moveDocs.add(m.toDocument());
            }
        }

        doc.append("playerO", playerO)
           .append("playerX", playerX)
           .append("winner", winner)
           .append("winnerName", winnerName)
           .append("moves", moveDocs)
           .append("totalMoves", totalMoves)
           .append("startedAt", startedAt != null ? startedAt : new Date())
           .append("endedAt", endedAt != null ? endedAt : new Date())
           .append("durationSeconds", durationSeconds);
        return doc;
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getPlayerO() {
        return playerO;
    }

    public void setPlayerO(String playerO) {
        this.playerO = playerO;
    }

    public String getPlayerX() {
        return playerX;
    }

    public void setPlayerX(String playerX) {
        this.playerX = playerX;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }

    public List<Move> getMoves() {
        return moves;
    }

    public void setMoves(List<Move> moves) {
        this.moves = moves;
        if (moves != null) {
            this.totalMoves = moves.size();
        }
    }

    public int getTotalMoves() {
        return totalMoves;
    }

    public void setTotalMoves(int totalMoves) {
        this.totalMoves = totalMoves;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Date getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Date endedAt) {
        this.endedAt = endedAt;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }
}
