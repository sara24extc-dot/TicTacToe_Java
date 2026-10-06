# Desktop Tic-Tac-Toe Game (JavaFX + MongoDB)

A Java desktop port of the classic web Tic-Tac-Toe game with persistent player statistics, game history, and move replays powered by MongoDB.

---

## 🎮 Features

- **Classic Game Logic**:
  - 3x3 board where Player O always starts, alternating with Player X.
  - Complete win pattern detection (3 horizontal, 3 vertical, 2 diagonal).
  - Accurate draw detection at 9 moves.
  - "Reset Game" and "New Game" controls.
- **Faithful Web Aesthetic**:
  - Exact port of the original web project's visual style: `#548687` background, `#ffffc7` soft rounded boxes with `#b0413e` symbols, `#191913` dark buttons, and responsive layout.
- **MongoDB Persistence**:
  - `players` collection: Stores player accounts with atomic statistics updates (`totalGames`, `wins`, `losses`, `draws`) using MongoDB's `$inc` operator.
  - `games` collection: Stores full game metadata including player usernames, winner, duration, timestamps, and the complete move-by-move history.
  - Automatic index creation on `players.username` (unique) and `games.endedAt` (descending).
- **Leaderboard Screen**:
  - Displays top 10 players ranked by wins, along with total matches, losses, draws, and win rate percentage.
- **Game History & Interactive Replay**:
  - Lists the last 20 games with date, players, winner, move count, and duration.
  - **Move Replay**: Step through any completed game move-by-move (Forward, Backward, Auto-Play, Jump to Start/End).
- **Graceful Offline Mode**:
  - If MongoDB is unreachable, the game displays a clear offline indicator and allows full gameplay without crashing or interrupting the user.

---

## 🛠️ Tech Stack

- **Java**: 17+ (Tested on Java 17, 21, and 25)
- **GUI**: JavaFX 21 (`javafx-controls`, `javafx-graphics`, `javafx-base`)
- **Database Driver**: MongoDB Java Sync Driver (`org.mongodb:mongodb-driver-sync:5.1.0`)
- **Logging**: SLF4J (`slf4j-api`, `slf4j-simple`)
- **Testing**: JUnit 5 Jupiter (`junit-jupiter`)
- **Build System**: Apache Maven 3.9+ (`javafx-maven-plugin`)

---

## 📁 Project Structure

```text
tictactoe-java/
├── pom.xml
├── README.md
├── mvnw.cmd
├── .mvn/
│   └── jvm.config
├── src/
│   ├── main/
│   │   ├── java/com/tictactoe/
│   │   │   ├── Main.java                 # JavaFX Application entry point
│   │   │   ├── model/
│   │   │   │   ├── Player.java           # Player document model
│   │   │   │   ├── Move.java             # Individual move representation
│   │   │   │   └── GameRecord.java       # Game document model
│   │   │   ├── game/
│   │   │   │   └── GameEngine.java       # Pure game logic (UI & DB independent)
│   │   │   ├── db/
│   │   │   │   ├── MongoConnection.java  # Singleton MongoDB connection manager
│   │   │   │   ├── PlayerRepository.java # Player CRUD and atomic $inc updates
│   │   │   │   └── GameRepository.java   # Game history queries and inserts
│   │   │   └── ui/
│   │   │       ├── ScreenController.java # Navigation and scene switcher
│   │   │       ├── StartScreen.java      # Welcome and player name setup
│   │   │       ├── GameScreen.java       # 3x3 interactive board and outcomes
│   │   │       ├── LeaderboardScreen.java# Top 10 leaderboard table
│   │   │       ├── HistoryScreen.java    # Recent games table
│   │   │       └── ReplayDialog.java     # Step-by-step move replay modal
│   │   └── resources/
│   │       └── style.css                 # JavaFX stylesheet
│   └── test/
│       └── java/com/tictactoe/
│           ├── game/
│           │   └── GameEngineTest.java   # 17 Unit tests for all win/draw patterns
│           └── db/
│               └── MongoIntegrationTest.java # MongoDB repository tests
```

---

## 🚀 Getting Started

### 1. Prerequisites

- **Java Development Kit (JDK)**: JDK 17 or higher installed. Verify with:
  ```bash
  java -version
  ```
- **MongoDB**:
  - **Option A (Local)**: Install and start MongoDB Community Server locally listening on `mongodb://localhost:27017`.
  - **Option B (MongoDB Atlas)**: Set the `MONGO_URI` environment variable to your connection string:
    ```bash
    # Windows PowerShell
    $env:MONGO_URI="mongodb+srv://<username>:<password>@cluster0.mongodb.net/?retryWrites=true&w=majority"
    
    # Linux / macOS
    export MONGO_URI="mongodb+srv://<username>:<password>@cluster0.mongodb.net/?retryWrites=true&w=majority"
    ```

---

### 2. Running the Application

**Quickest Option (Double Click)**:
- Simply double-click [`run.bat`](file:///d:/Microproj_EXTC/Sara/run.bat) in the project folder to start the game directly!

**From Terminal**:
```bash
# Using standard Maven
mvn clean javafx:run

# Or using the included Windows wrapper script
.\mvnw.cmd clean javafx:run
```

---

### 3. Running Automated Tests

Run all unit and integration tests with:

```bash
# Using standard Maven
mvn clean test

# Or using the wrapper script
.\mvnw.cmd clean test
```

Test coverage includes:
- All 8 win patterns (3 rows, 3 columns, 2 diagonals)
- Draw detection after 9 moves
- Turn alternation (O always first, followed by X)
- Rejection of invalid moves (occupied squares, out-of-bounds, after game over)
- Board and game reset integrity
- Atomic `$inc` operations in MongoDB

---

## 📄 Sample MongoDB Documents

### `players` Collection Document

```json
{
  "_id": { "$oid": "664c3a1e2f8b5c0012a45b78" },
  "username": "Alice",
  "createdAt": { "$date": "2026-10-01T12:30:00.000Z" },
  "totalGames": 12,
  "wins": 8,
  "losses": 3,
  "draws": 1
}
```

### `games` Collection Document

```json
{
  "_id": { "$oid": "664c3a502f8b5c0012a45b79" },
  "playerO": "Alice",
  "playerX": "Bob",
  "winner": "O",
  "winnerName": "Alice",
  "moves": [
    {
      "moveNumber": 1,
      "symbol": "O",
      "boxIndex": 0,
      "timestamp": { "$date": "2026-10-01T12:32:01.100Z" }
    },
    {
      "moveNumber": 2,
      "symbol": "X",
      "boxIndex": 4,
      "timestamp": { "$date": "2026-10-01T12:32:03.450Z" }
    },
    {
      "moveNumber": 3,
      "symbol": "O",
      "boxIndex": 1,
      "timestamp": { "$date": "2026-10-01T12:32:05.200Z" }
    },
    {
      "moveNumber": 4,
      "symbol": "X",
      "boxIndex": 3,
      "timestamp": { "$date": "2026-10-01T12:32:07.800Z" }
    },
    {
      "moveNumber": 5,
      "symbol": "O",
      "boxIndex": 2,
      "timestamp": { "$date": "2026-10-01T12:32:09.150Z" }
    }
  ],
  "totalMoves": 5,
  "startedAt": { "$date": "2026-10-01T12:32:00.000Z" },
  "endedAt": { "$date": "2026-10-01T12:32:10.000Z" },
  "durationSeconds": 10
}
```
