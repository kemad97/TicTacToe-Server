package tictactoe.server.main_screen;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import tictactoe.server.dao.DAO;
import tictactoe.server.dao.User;
import tictactoe.server.request_handler.RequestHandler;
import tictactoe.server.request_handler.RequestReceiver;

public class FXMLMainScreenController implements Initializable {

    @FXML
    private Button start_stop_btn;
    
    @FXML
    private  BarChart<String, Number> barChart;
    
    @FXML
    private  TextField textOfflinePlayer, textonlinePlayers, textActivePlayer;
    
    private  CategoryAxis categoryAxis;
    private  NumberAxis numberAxis;
   

    @Override
    public void initialize(URL url, ResourceBundle rb) {
   
        addBarChart();
        updateUserStatus();
        listenForUpdate();
    }

    @FXML
    private void changeServerStatus(ActionEvent event) {
        RequestReceiver.changeServerStatus();
        if (start_stop_btn.getText().equals("Start")) {
            start_stop_btn.setText("Stop");
        } else {
            start_stop_btn.setText("Start");
        }
    }
    
    
    private void addBarChart (){
        
        textonlinePlayers.setEditable(false);   
        textOfflinePlayer.setEditable(false);
        textActivePlayer.setEditable(false);

        XYChart.Series<String, Number> offlineUsers = new XYChart.Series<>();
        offlineUsers.setName("Offline Players");
        XYChart.Series<String, Number> onlineUsers = new XYChart.Series<>();
        onlineUsers.setName("Online Players");
        XYChart.Series<String, Number> activeUsers = new XYChart.Series<>();
        activeUsers.setName("Active Players");
        
        offlineUsers.getData().add(new XYChart.Data<>("offline", 0));
        onlineUsers.getData().add(new XYChart.Data<>("Online", 0));
        activeUsers.getData().add(new XYChart.Data<>("Active", 0));
        barChart.getData().addAll(offlineUsers, onlineUsers, activeUsers);
    }

    public void updateChartData(int offlineUsersNum, int onlineUsersNum, int activeUsersNum) {
        XYChart.Series<String, Number> offlineUsers = barChart.getData().get(0);
        XYChart.Series<String, Number> onlineUsers = barChart.getData().get(1);
        XYChart.Series<String, Number> activeUsers = barChart.getData().get(2);

        offlineUsers.getData().get(0).setYValue(offlineUsersNum);
        textOfflinePlayer.setText(String.valueOf(offlineUsersNum));
        onlineUsers.getData().get(0).setYValue(onlineUsersNum);
        textonlinePlayers.setText(String.valueOf(onlineUsersNum));
        activeUsers.getData().get(0).setYValue(activeUsersNum);
        textActivePlayer.setText(String.valueOf(activeUsersNum));
    }
    
    private void updateUserStatus() {
        try {
            
            DAO dao = DAO.getInstance();
            int totalUsers = dao.getTotalPlayers(); 

            int onlineUsers = 0;
            int inGameUsers = 0;
            
            for (RequestHandler handler : RequestHandler.getUsers()) {
                
                if (handler.getUser() != null) {
                    if (handler.getUser().getStatus() == User.AVAILABLE) {
                        onlineUsers++;
                    } else if (handler.getUser().getStatus() == User.IN_GAME) {
                        inGameUsers++;
                    }
                }
            }

            int offlineUsers = totalUsers - (onlineUsers + inGameUsers);

            updateChartData(offlineUsers, onlineUsers, inGameUsers);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void listenForUpdate() {
        
        Thread th = new Thread(() -> {
            while (true) {
                try {
                    
                    updateUserStatus();
                    
                    Thread.sleep(5000);
                    
                } catch (InterruptedException e) {
                    
                    e.printStackTrace();
                    
                    break; 
                }
            }
        });
        
        th.start(); 
    }


}
