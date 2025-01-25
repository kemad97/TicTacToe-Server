
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

## Installation and Setup

### Prerequisites:
- Java Development Kit (JDK) installed on your machine.
- Internet connection for online multiplayer mode.

### Steps:
1. Clone the repository or download the source code.
2. Open the project in your preferred Java IDE (e.g., IntelliJ IDEA, Eclipse).
3. Download the required JSON library from the following link:
   [Download Json.jar](https://repo1.maven.org/maven2/org/json/json/20250107/json-20250107.jar).
4. Add the `json-20250107.jar` to your project's classpath.
5. Build and run the server application to initialize the server.
6. Launch the client application for the Tic-Tac-Toe game.
7. Register or log in to access online multiplayer features.
8. Enjoy the game in single-player, local multiplayer, or online multiplayer modes.

