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
import org.json.JSONObject;
import tictactoe.server.dao.DAO;
import tictactoe.server.dao.User;

public class RequestReceiver {

    private static ServerSocket serverSocket;
    private static Vector<RequestReceiver> clients;
    private static boolean working;

    private Socket client;
    private DataInputStream dis;
    private DataOutputStream dos;
    private User user;

    static {
        clients = new Vector<>();
        working = false;
        serverSocket = null;
    }

    public static void changeServerStatus() {
        try {
            if (!working) {
                serverSocket = new ServerSocket(8080);
                new RequestReceiver();
            } else {
                serverSocket.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        working = !working;
    }

    public static void closeServer() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                System.out.println("server already down");
            }
        }
    }

    public RequestReceiver() {
        new Thread(() -> {
            startServer();
        }).start();
    }

    private void startServer() {
        System.out.println("started");
        while (working) {
            try {
                client = serverSocket.accept();
                dis = new DataInputStream(client.getInputStream());
                dos = new DataOutputStream(client.getOutputStream());

                user = new User();
                handleRequests(dis.readUTF());

            } catch (IOException ex) {
                //lose connection
                System.out.println("server is down");
                break;
            }
        }
    }

    private void handleRequests(String msg) throws IOException {
        JSONObject jsonObject = new JSONObject(msg);

        String header = jsonObject.getString("header");

        switch (header) {
            case "register":
                try {
                    registerNewUser(jsonObject);
                } catch (IOException ex) {
                    //can't connect to client
                    System.out.println("can't connect to client");
                }

                break;
            default:
                Map<String, String> map = new HashMap<>();
                map.put("header", "error");
                map.put("message", "invaled header");

                JSONObject response = new JSONObject(map);

                System.out.println("ERROR: invaled header");
                this.dos.writeUTF(response.toString());
        }

    }

    private void registerNewUser(JSONObject jsonObject) throws IOException {
        //waiting username and hashed_password
        user.setUsername(jsonObject.getString("username"));
        user.setHashedPassword(jsonObject.getString("password"));

        try {
            DAO.saveUser(user);

            clients.add(this);

            //send success header to user for success regesteraion
            Map<String, String> map = new HashMap<>();
            map.put("header", "success");
            map.put("message", user.getUsername());

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
}
