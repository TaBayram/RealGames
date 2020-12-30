package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
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
    public TextField textField_RoomName;

    public void initialize() {
        HideOtherMainsExceptThis(anchorPane_MainMenu);

    }

    Client client = new Client();



    public void buttonPlayClick(ActionEvent actionEvent) {
        HideOtherMainsExceptThis(anchorPane_Play);
        
    }

    public void buttonCreateRoomClick(ActionEvent actionEvent) {

        Main.StartServer();
        HideOtherMainsExceptThis(anchorPane_Room);
    }

    public void buttonBackClick(ActionEvent actionEvent) {
        HideOtherMainsExceptThis(anchorPane_Play);

        try{
            client.StopFindingServers();
            client.StopReceivingInet();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }

    public void buttonSearchRoomClick(ActionEvent actionEvent) throws InterruptedException {
        HideOtherMainsExceptThis(anchorPane_FindRooms);

        client.StartFindingServers();
        client.StartReceivingInet(this);


        TimerTask task = new TimerTask() {
            @Override public void run() {
                Platform.runLater(() -> {
                    vBox_RoomList.getChildren().removeAll();
                    vBox_RoomList.getChildren().clear();
                   /* for (InetAddress inetAddress:list) {
                        Button button = new Button(""+inetAddress.getHostAddress());
                        vBox_RoomList.getChildren().add(button);
                    }*/
                    for(int i = 0; i < list.size(); i ++){
                        ServerListCreateServerBox(list.get(i),namelist.get(i));
                    }
                    list.clear();
                    namelist.clear();
                });

            }
        };
        Timer timer = new Timer();
        timer.schedule(task,2000L,8000L);


    }

    private void ServerListCreateServerBox(InetAddress inetAddress,String roomName){
        String ipAddress = inetAddress.getHostAddress();

        var width = vBox_RoomList.getWidth();
        Pane pane = new Pane();
        pane.setPrefSize(width/.90,60);
        pane.getStyleClass().add("serverpane");

        Label labelRoomName = new Label(roomName);
        Label labelIpAddress = new Label(ipAddress);
        labelRoomName.getStyleClass().add("minortext");
        labelIpAddress.getStyleClass().add("minortext");

        Button button = new Button("Join Room");
        button.getStyleClass().add("minorbutton");
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
            JoinARoomButtonClick(e,inetAddress,roomName);
        }});

        button.setLayoutX(pane.getPrefWidth()-button.getPrefHeight()-10);
        button.setLayoutY(10);
        labelIpAddress.setLayoutX(10);
        labelIpAddress.setLayoutY(40);
        labelRoomName.setLayoutX(5);
        labelRoomName.setLayoutY(5);


        pane.getChildren().addAll(button,labelIpAddress,labelRoomName);
        vBox_RoomList.getChildren().add(pane);

    }

    private void JoinARoomButtonClick(ActionEvent actionEvent,InetAddress inetAddress,String roomName){
        Main.StartClient(inetAddress);
        HideOtherMainsExceptThis(anchorPane_Room);
        textField_RoomName.setEditable(false);
        textField_RoomName.setText(roomName);

    }







    ObservableList<InetAddress> list = FXCollections.observableArrayList();
    public ObservableList<String> namelist = FXCollections.observableArrayList();
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


    public void textFieldRoomNameChange(ActionEvent actionEvent) {
        var textField = (TextField)actionEvent.getSource();
        Server.ServerName = textField.getText();
    }
}
