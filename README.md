
# Tic-Tac-Toe Server

The server application is responsible for handling connections, data streams, and managing user interactions in online mode. Key server features include:

- **Start/Stop Service Buttons:**
  - Easily start or stop the server.

- **Graphical Monitoring:**
  - Visual graphs displaying:
    - Number of active users.
    - Online and offline user statistics.

---

## Installation and Setup

### Prerequisites:
- Java Development Kit (JDK) installed on your machine.
- Download Json.jar.
- Create Data Base schema.
  ~~~ sql
  CREATE TABLE users (
    id INTEGER PRIMARY KEY,
    user_name VARCHAR(45) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    score INTEGER NOT NULL DEFAULT 0,
    avatar VARCHAR(200),
    matches_no INTEGER NOT NULL DEFAULT 0,
    won_matches INTEGER NOT NULL DEFAULT 0
);
  ~~~

### Steps:
1. Clone the repository or download the source code.
2. Open the project in your preferred Java IDE (e.g., IntelliJ IDEA, Eclipse).
3. Build and run the server application to initialize the server.
4. Launch the client application for the Tic-Tac-Toe game.
5. Register or log in to access online multiplayer features.
6. Enjoy the game in single-player, local multiplayer, or online multiplayer modes.

---

## How to Play
1. Select a game mode:
   - Single-Player
   - Local Multiplayer
   - Online Multiplayer
2. Follow the in-game instructions for your selected mode.
3. For online mode:
   - Choose an opponent from the list of available players.
   - Accept or decline game requests from other players.
4. Play the game and aim to win!
5. View game replays or enjoy bonus videos after winning.



