package tictactoe.server.main_screen;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import tictactoe.server.request_handler.RequestReceiver;

public class FXMLMainScreenController implements Initializable {

    @FXML
    private Button start_stop_btn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
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

}
