package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

public class Controller {


    public AnchorPane anchorPane_MainMenu;
    public Button button_Play;
    public Button button_Options;
    public Button button_Quit;
    public AnchorPane anchorPane_Play;
    public StackPane stackPane_Main;
    public VBox vBox_RoomList;
    public AnchorPane anchorPane_FindRooms;
    public AnchorPane anchorPane_Room;

    public void initialize() {
        HideOtherMainsExceptThis(anchorPane_MainMenu);

    }




    public void buttonPlayClick(ActionEvent actionEvent) {
        HideOtherMainsExceptThis(anchorPane_Play);
        
    }

    public void buttonCreateRoomClick(ActionEvent actionEvent) {
        Main.StartServer();
        Main.StartClient();
        HideOtherMainsExceptThis(anchorPane_Room);
    }

    public void buttonSearchRoomClick(ActionEvent actionEvent) throws InterruptedException {
        HideOtherMainsExceptThis(anchorPane_FindRooms);
        Main.StartClient();

        Client client = new Client();
        client.StartFindingServers();

        client.StartReceivingInet(this);


        TimerTask task = new TimerTask() {
            @Override public void run() {
                Platform.runLater(() -> {
                    vBox_RoomList.getChildren().removeAll();
                    vBox_RoomList.getChildren().clear();
                    for (InetAddress inetAddress:list) {
                        Button button = new Button(""+inetAddress.getHostAddress());
                        vBox_RoomList.getChildren().add(button);
                    }
                    list.clear();
                });

            }
        };
        Timer timer = new Timer();
        timer.schedule(task,2000L,8000L);


    }








    public void buttonBackClick(ActionEvent actionEvent) {
        HideOtherMainsExceptThis(anchorPane_Play);
    }

    ObservableList<InetAddress> list = FXCollections.observableArrayList();
    public void ReceiveData(InetAddress inetAddress){
        System.out.println("Received InetAddress");
        boolean isUnique = true;
        for (InetAddress address:list) {
           if(inetAddress == address){
               isUnique = false;
               break;
           }

        }
        if(isUnique)
            list.add(inetAddress);

    }





    private void HideOtherMainsExceptThis(Node pane){
        for (Node node: stackPane_Main.getChildren()) {
            if(pane == node){
                node.setDisable(false);
                node.setVisible(true);
                node.setOpacity(1);
            }
            else{
                node.setDisable(true);
                node.setVisible(false);
                node.setOpacity(0);
            }

        }

    }


}
