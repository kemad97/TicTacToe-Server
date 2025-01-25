package tictactoe.server.request_handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.SQLNonTransientConnectionException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import tictactoe.server.dao.DAO;
import tictactoe.server.dao.User;

public class RequestHandler extends Thread {

    public static boolean working;

    private static Vector<RequestHandler> users;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private User user;

    static {
        users = new Vector<>();
        working = false;
    }

    public RequestHandler(Socket socket, ServerSocket serverSocket) {
        try {
            this.socket = socket;
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            user = new User();

            start();
        } catch (IOException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        //listen if server is douwn
        new Thread(() -> {
            while (true) {
                if (serverSocket.isClosed()) {
                    cloaseUserIOStreams();
                    break;
                }
            }
        }).start();

    }

    public void cloaseUserIOStreams() {
        try {
            dis.close();
            dos.close();
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        while (socket.isConnected()) {
            try {
                handleRequests(dis.readUTF());
            } catch (IOException ex) {
                if (this.user != null) {
                    System.out.println(this.user.getUsername() + " disconnected.");
                    users.remove(this);
                    sendAvailablePlayersToAll();
                }
                break;
            }
        }
    }

    private void handleRequests(String msg) throws IOException {
        JSONObject jsonObject = new JSONObject(msg);

        String header = jsonObject.getString("header");

        System.out.println(msg);

        switch (header) {
            case "register":
                try {
                    registerNewUser(jsonObject);
                } catch (IOException ex) {
                    //can't connect to client
                    System.out.println("can't connect to client");
                }

                break;
            case "login":
                try {
                    loginUser(jsonObject);
                } catch (IOException ex) {
                    //can't connect to client
                    System.out.println("can't connect to client");
                }
                break;

            case "get_available_players":
                sendAvailablePlayersToAll();
                break;

            case "request_start_match":
                handleMatchRequest(jsonObject);
                break;
            case "match_response":
                startMatchResult(jsonObject);
                break;

            case "move":
                sendMoveToTheOtherPlayer(jsonObject);
                break;

            case "get_user_profile":
                sendUserProfile(jsonObject);
                break;

            case "end_player_game":
                finalizePlayerMatch();
                break;
    
            case "update_score":
                String winnerName = jsonObject.getString("winner");
                updateWinnerScore(winnerName);
                break;
            case "exit_mathc":
                notifyOtherPlayerToExitGame(jsonObject);
                break;
            case "update_matches_NO":
                updateMatches_No(jsonObject);
                break;

            case "ask_to_be_not_available":
                notifyUserWithNotAvailableStateChanged();
                break;
            case "ask_to_be_available":
                notifyUserWithAvailableStateChanged();
                break;

            default:
                Map<String, String> map = new HashMap<>();
                map.put("header", "error");
                map.put("message", "invaled header");

                JSONObject response = new JSONObject(map);

                System.out.println("ERROR: invaled Request");
                this.dos.writeUTF(response.toString());
        }

    }

    private void updateWinnerScore(String winnerName) {

        System.out.println("server recieved request");

        int updatedScore = this.user.getScore() + 10;
        this.user.setScore(updatedScore);
        try {
            DAO.getInstance().updateScore(user);
            DAO.getInstance().updateWinMatches(winnerName);
            System.out.println("communicate with database");
        } catch (SQLException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void registerNewUser(JSONObject jsonObject) throws IOException {
        //waiting username and hashed_password
        user.setUsername(jsonObject.getString("username"));
        user.setHashedPassword(jsonObject.getString("password"));

        try {
            DAO.getInstance().saveUser(user);

            users.add(this);

            //send success header to user for success regesteraion
            Map<String, String> map = new HashMap<>();
            map.put("header", "success");
            map.put("username", user.getUsername());
            map.put("score", user.getScore() + "");

            JSONObject response = new JSONObject(map);

            this.dos.writeUTF(response.toString());

        } catch (SQLNonTransientConnectionException ex) {
            Map<String, String> map = new HashMap<>();
            map.put("header", "error");
            map.put("message", "Internal server error.");
            JSONObject response = new JSONObject(map);

            this.dos.writeUTF(response.toString());

            System.out.println("Database is down");
        } catch (SQLException ex) {
            //redundant username
            //send to user to change the username
            Map<String, String> map = new HashMap<>();
            map.put("header", "register_error");
            map.put("message", "use another username");

            JSONObject response = new JSONObject(map);

            this.dos.writeUTF(response.toString());
            ex.printStackTrace();
        }

    }

    private void loginUser(JSONObject jsonObject) throws IOException {
        try {
            this.user = DAO.getInstance().getUserByUsername(jsonObject.getString("username"));
        } catch (SQLNonTransientConnectionException ex) {
            Map<String, String> map = new HashMap<>();
            map.put("header", "error");
            map.put("message", "Internal server error.");
            JSONObject response = new JSONObject(map);

            this.dos.writeUTF(response.toString());

            System.out.println("Database is down");
            return;
        } catch (SQLException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (user == null) {
            //user dose not exist
            //send to user "this username is incorect"
            Map<String, String> map = new HashMap<>();
            map.put("header", "login_error");
            map.put("message", "Username is incorrect.");

            JSONObject response = new JSONObject(map);

            this.dos.writeUTF(response.toString());
            return;
        }

        //chck if the user is logged in
        if (isLoggedin(user.getUsername())) {
            //send success header to user for success login
            Map<String, String> map = new HashMap<>();
            map.put("header", "error");
            map.put("message", "this user is logged in.");

            JSONObject response = new JSONObject(map);

            this.dos.writeUTF(response.toString());
            return;
        }

        if (user.getHashedPassword().equals(jsonObject.getString("password"))) {

            users.add(this);

            //send success header to user for success login
            Map<String, String> map = new HashMap<>();
            map.put("header", "success");
            map.put("username", user.getUsername());
            map.put("score", user.getScore() + "");

            JSONObject response = new JSONObject(map);

            this.dos.writeUTF(response.toString());

        } else {
            //password incorrect
            //send to user "this username is incorect"
            Map<String, String> map = new HashMap<>();
            map.put("header", "login_error");
            map.put("message", "Password is incorrect.");

            JSONObject response = new JSONObject(map);

            this.dos.writeUTF(response.toString());
        }
    }

    private boolean isLoggedin(String username) {
        for (RequestHandler u : users) {
            if (u.user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    private static Vector<RequestHandler> getAvailablePlayers() {

        Vector<RequestHandler> availablePlayers = new Vector<>();

        for (RequestHandler userHandler : users) {

            if (userHandler.user != null && userHandler.user.getStatus() == User.AVAILABLE) {

                availablePlayers.add(userHandler);

            }

        }

        return availablePlayers;

    }

    public static void notifyAllUsersServerDowen() throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("header", "server_down");

        for (RequestHandler playerHandler : users) {
            playerHandler.dos.writeUTF(jsonObject.toString());
        }
    }

    private static void sendAvailablePlayersToAll() {
        JSONObject response = new JSONObject();
        Vector<RequestHandler> availablePlayers = getAvailablePlayers();
        List<Map<String, String>> playerList = new ArrayList<>();

        for (RequestHandler player : availablePlayers) {
            Map<String, String> playerData = new HashMap<>();
            playerData.put("username", player.user.getUsername());
            playerData.put("score", player.user.getScore().toString());
            playerList.add(playerData);
        }

        response.put("header", "available_players");
        response.put("players", playerList);

        for (RequestHandler player : availablePlayers) {
            try {
                player.dos.writeUTF(response.toString());
            } catch (IOException ex) {
                System.out.println("can't connect to " + player.user.getUsername());
            }
        }

    }

    private void handleMatchRequest(JSONObject jsonObject) throws IOException {
        String player2_Username = jsonObject.getString("targetPlayer");
        RequestHandler player2Handler = getPlayerHandler(player2_Username);

        if (player2Handler == null) {
            // Player 2 is not online
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("header", "match_error");
            errorResponse.put("message", "The requested player is not online.");
            this.dos.writeUTF(errorResponse.toString());
            return;
        }

        // Forward the request to Player 2
        JSONObject matchRequest = new JSONObject();
        matchRequest.put("header", "match_request");
        matchRequest.put("fromPlayer", this.user.getUsername());
        player2Handler.dos.writeUTF(matchRequest.toString());

        User player1 = this.user;
        User player2 = player2Handler.user;

        player1.setStatus(User.NOT_AVAILABLE);
        player2.setStatus(User.NOT_AVAILABLE);
        sendAvailablePlayersToAll();
    }

    private RequestHandler getPlayerHandler(String username) {
        for (RequestHandler userHandler : users) {
            if (userHandler.user != null && userHandler.user.getUsername().equals(username)) {
                return userHandler; // Return the handler if the username matches
            }
        }
        return null; // Return null if the player is not found
    }

    private void handleMatchResponse(JSONObject jsonObject) throws IOException {
        String fromPlayer = jsonObject.getString("fromPlayer"); // Player 1 who initiated the request
        String isAccepted = jsonObject.getString("isAccepted"); // Whether Player 2 accepted the match

        // Get Player 1's handler
        RequestHandler fromPlayerHandler = getPlayerHandler(fromPlayer);

        if (fromPlayerHandler != null) {
            // Create response JSON
            JSONObject response = new JSONObject();
            response.put("header", "match_response");
            response.put("fromPlayer", this.user.getUsername()); // Player 2 responding
            response.put("isAccepted", isAccepted);

            // Notify Player 1 of the response
            fromPlayerHandler.dos.writeUTF(response.toString());

            // If accepted, start the game logic
            if (isAccepted.equals("accepted")) {
                System.out.println("in accepted");

                startMatch(fromPlayerHandler, this); // Pass both handlers for Player 1 and Player 2
            } else if (isAccepted.equals("declined")) {
                System.out.println("in declined");
                fromPlayerHandler.user.setStatus(User.AVAILABLE);
                this.user.setStatus(User.AVAILABLE);
                sendAvailablePlayersToAll();

            }

        }
    }

    private void startMatch(RequestHandler player1Handler, RequestHandler player2Handler) {
        try {
            JSONObject startGameMessage = new JSONObject();
            startGameMessage.put("header", "start_game");
            startGameMessage.put("opponent", player2Handler.user.getUsername()); // Player 2's username for Player 1
            startGameMessage.put("yourTurn", true); // Player 1 starts the game
            // URL xSymbolPath = RequestHandler.class.getResource("/media/images/X.png");
            // startGameMessage.put("symbol", xSymbolPath);
            player1Handler.dos.writeUTF(startGameMessage.toString());

            startGameMessage.put("opponent", player1Handler.user.getUsername()); // Player 1's username for Player 2
            startGameMessage.put("yourTurn", false); // Player 2 waits for Player 1's move
            // URL oSymbolPath = RequestHandler.class.getResource("/media/images/O.png");
            // startGameMessage.put("symbol", oSymbolPath);
            player2Handler.dos.writeUTF(startGameMessage.toString());

            player1Handler.user.setStatus(User.IN_GAME);
            player2Handler.user.setStatus(User.IN_GAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startMatchResult(JSONObject jsonObject) throws IOException {
        RequestHandler player2 = getPlayerHandler(jsonObject.getString("opponent"));
        if (jsonObject.getString("response").equals("accepted")) {
            //this.user.setStatus(User.IN_GAME);        
            startMatch(this, player2);
            sendAvailablePlayersToAll();
        } else if (jsonObject.getString("response").equals("declined")) {
            JSONObject startGameMessage = new JSONObject();
            startGameMessage.put("header", "request_decline");
            startGameMessage.put("opponent", this.user.getUsername());

            player2.dos.writeUTF(startGameMessage.toString()); //send declined msg

            System.out.println("in declined");
            player2.user.setStatus(User.AVAILABLE);
            this.user.setStatus(User.AVAILABLE);
            sendAvailablePlayersToAll();
        }
    }

    public static Vector<RequestHandler> getUsers() {
        return users;
    }

    public User getUser() {
        return this.user;
    }

    public void sendMoveToTheOtherPlayer(JSONObject json) {

        json.put("header", "move_res");
        try {
            getPlayerHandler(json.getString("opponent")).dos.writeUTF(json.toString());
        } catch (IOException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println(json);
    }

    private void sendUserProfile(JSONObject jsonObject) throws IOException {

        String username = jsonObject.getString("username");

        try {

            User user = DAO.getInstance().getUserData(username);

            if (user != null) {
                Map<String, String> map = new HashMap<>();
                map.put("header", "user_profile");
                map.put("name", user.getUsername());
                map.put("score", String.valueOf(user.getScore()));
                map.put("matches_no", String.valueOf(user.getMatches_no()));
                map.put("won_matches", String.valueOf(user.getWon_matches()));
                System.out.println("user data: " + map);

                JSONObject response = new JSONObject(map);
                dos.writeUTF(response.toString());
                dos.flush();
            } else {

                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("header", "user_profile");
                errorMap.put("message", "User not found");
                JSONObject errorResponse = new JSONObject(errorMap);
                dos.writeUTF(errorResponse.toString());
                dos.flush();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void finalizePlayerMatch() throws IOException {
        this.user.setStatus(User.AVAILABLE);
        this.dos.writeUTF(new JSONObject().put("header", "end_of_game").toString());

    }

    private void notifyOtherPlayerToExitGame(JSONObject jsonObject) throws IOException {
        System.out.println(jsonObject);
        JSONObject respone = new JSONObject().put("header", "opponent_exit_match");
        getPlayerHandler(jsonObject.getString("opponent")).dos.writeUTF(respone.toString());
    }
    
    private void updateMatches_No(JSONObject jsonObject) {
        String username = jsonObject.getString("username");
        try {
            DAO.getInstance().updateMatches_No(username);
            System.out.println("The update was successfully completed for: " + username);
        } catch (SQLException e) {
            System.err.println("We can't update matches_NO for: " + username);
            e.printStackTrace();
        }
    }

    private void notifyUserWithNotAvailableStateChanged() throws IOException {
        this.user.setStatus(User.NOT_AVAILABLE);
        this.dos.writeUTF(new JSONObject().put("header", "your_state_not_available").toString());
    }

    private void notifyUserWithAvailableStateChanged() throws IOException {
        this.user.setStatus(User.AVAILABLE);
        this.dos.writeUTF(new JSONObject().put("header", "your_state_available").toString());
    }

}
