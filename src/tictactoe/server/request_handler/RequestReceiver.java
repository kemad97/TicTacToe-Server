package tictactoe.server.request_handler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RequestReceiver {

    private static ServerSocket serverSocket;

    static {
        serverSocket = null;
    }

    public static void changeServerStatus() {
        try {
            if (serverSocket == null || serverSocket.isClosed()) {
                serverSocket = new ServerSocket(8080);
                new RequestReceiver();
            } else {
                serverSocket.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

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
        while (!serverSocket.isClosed()) {
            try {

                new RequestHandler(serverSocket.accept(), serverSocket);

            } catch (IOException ex) {
                //lose connection
                System.out.println("server is down");
                break;
            }
        }
    }
}
