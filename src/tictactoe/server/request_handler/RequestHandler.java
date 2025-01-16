package tictactoe.server.request_handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
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
                } catch (SQLException ex) {
                    Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
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

    private void registerNewUser(JSONObject jsonObject) throws IOException {
        //waiting username and hashed_password
        user.setUsername(jsonObject.getString("username"));
        user.setHashedPassword(jsonObject.getString("password"));

        try {
            DAO.saveUser(user);

            users.add(this);

            //send success header to user for success regesteraion
            Map<String, String> map = new HashMap<>();
            map.put("header", "success");
            map.put("username", user.getUsername());
            map.put("score", user.getScore() + "");

            JSONObject response = new JSONObject(map);

            this.dos.writeUTF(response.toString());

        } catch (SQLException ex) {
            //redundant username
            //send to user to change the username
            Map<String, String> map = new HashMap<>();
            map.put("header", "register_error");
            map.put("message", "use another username");

            JSONObject response = new JSONObject(map);

            this.dos.writeUTF(response.toString());
        }

    }

    private void loginUser(JSONObject jsonObject) throws SQLException, IOException {
        this.user = DAO.getUserByUsername(jsonObject.getString("username"));

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

}
