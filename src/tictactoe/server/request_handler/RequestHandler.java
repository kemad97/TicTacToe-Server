package tictactoe.server.request_handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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

    private static Vector<RequestHandler> players;
    private DataInputStream dis;
    private DataOutputStream dos;
    private User user;

    static {
        players = new Vector<>();
        working = false;
    }

    public RequestHandler(Socket client) {
        try {
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());

            user = new User();

            start();
        } catch (IOException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void run() {
        while (true) {
            try {
                handleRequests(dis.readUTF());
            } catch (IOException ex) {
                try {
                    dis.close();
                    dos.close();
                    System.out.println(this.user.getUsername() + " disconnected.");
                    players.remove(this);
                    break;
                } catch (IOException ex1) {
                    Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
    }

    private void handleRequests(String msg) throws IOException {
        JSONObject jsonObject = new JSONObject(msg);

        String header = jsonObject.getString("header");

        switch (header) {
            case "register":
                try {

                    System.out.println(msg);

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

            players.add(this);

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
