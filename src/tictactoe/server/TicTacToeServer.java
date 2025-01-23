package tictactoe.server;

import java.sql.SQLNonTransientConnectionException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tictactoe.server.dao.DAO;
import tictactoe.server.request_handler.RequestReceiver;
import tictactoe.server.main_screen.FXMLMainScreenController;

public class TicTacToeServer extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("main_screen/FXMLMainScreen.fxml"));

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        RequestReceiver.closeServer();
        try {
            DAO.getInstance().close();
        } catch (SQLNonTransientConnectionException ex) {
            System.out.println("database is dwon!");
        }
        FXMLMainScreenController.isUpdatingGraph = false;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
