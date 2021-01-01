package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.InetAddress;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class Controller {

    public StackPane stackPane_Main;

    public AnchorPane anchorPane_MainMenu;
    public Button button_MMPlay;
    public Button button_MMOptions;
    public Button button_MMQuit;


    public AnchorPane anchorPane_Play;

    public AnchorPane anchorPane_FindRooms;
    public VBox vBox_FRRoomList;


    public AnchorPane anchorPane_Room;
    public TextField textField_RRoomName;
    public VBox vBox_RoomPlayerList;
    public ListView listView_RLog;

    public AnchorPane anchorPane_GameMath;
    public TextField textField_GMAnswer;


    public void initialize() {
        HideOtherMainsExceptThis(anchorPane_MainMenu);
        client = new Client(this);
        server = new Server(this);

    }

    Client client;
    Server server;

    boolean isServerOwner = false;



    //MAIN MENU SCREEN

    public void buttonPlayClick(ActionEvent actionEvent) {
        HideOtherMainsExceptThis(anchorPane_Play);

    }


    //PLAY MENU SCREEN

    public void buttonCreateRoomClick(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog("Enter Name");
        dialog.setTitle("Text Input Dialog");
        dialog.setHeaderText(null);
        dialog.setContentText("Please enter your name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            if(result.get().trim() == ""){
                Client.playerMe.setName("Generic");
            }
            else{
                Client.playerMe.setName(result.get());
            }
            StartServer();
            isServerOwner = true;
            HideOtherMainsExceptThis(anchorPane_Room);
            AddPlayerToList(Client.playerMe,false);

        }

    }

    public void buttonSearchRoomClick(ActionEvent actionEvent) {
        HideOtherMainsExceptThis(anchorPane_FindRooms);

        client.StartFindingServers();
        client.StartReceivingInet(this);


        TimerTask task = new TimerTask() {
            @Override public void run() {
                Platform.runLater(() -> {
                    vBox_FRRoomList.getChildren().removeAll();
                    vBox_FRRoomList.getChildren().clear();
                   /* for (InetAddress inetAddress:list) {
                        Button button = new Button(""+inetAddress.getHostAddress());
                        vBox_RoomList.getChildren().add(button);
                    }*/
                    for(int i = 0; i < inetAddresses.size(); i ++){
                        ServerListCreateServerBox(inetAddresses.get(i),namelist.get(i));
                    }
                    inetAddresses.clear();
                    namelist.clear();
                });

            }
        };
        Timer timer = new Timer();
        timer.schedule(task,2000L,8000L);


    }

    //ROOM SCREEN
    Vector<RoomPlayerBox> roomPlayerBoxes = new Vector<>();


    public void buttonStartGameClick(ActionEvent actionEvent) {
        HideOtherMainsExceptThis(anchorPane_GameMath);
    }

    public void AddPlayerToList(DataPackages.Player player,boolean isAfter) {
        Platform.runLater(() -> {
            RoomPlayerBox roomPlayerBox = new RoomPlayerBox(vBox_RoomPlayerList, player);
            roomPlayerBoxes.add(roomPlayerBox);
            if(isAfter) listView_RLog.getItems().add(player.getName() +" has joined!");

        });
    }

    public void RemovePlayerFromList(DataPackages.Player player) {
        Platform.runLater(() -> {
            for(RoomPlayerBox roomPlayerBox: roomPlayerBoxes){
                if(roomPlayerBox.player.getID() == player.getID()){
                    roomPlayerBox.Remove();
                    listView_RLog.getItems().add(player.getName() +" has left!");
                    break;
                }
            }


        });
    }

    public void buttonRoomBackClick(ActionEvent actionEvent) {
        if(isServerOwner){
            StopServer();
            isServerOwner = false;
        }
        else{
            StopClient();
            HideOtherMainsExceptThis(anchorPane_Play);

        }
    }

    //FIND ROOM SCREEN

    public void textFieldRoomNameChange(ActionEvent actionEvent) {
        var textField = (TextField)actionEvent.getSource();
        Server.ServerName = textField.getText();
    }

    private void ServerListCreateServerBox(InetAddress inetAddress,String roomName){
        String ipAddress = inetAddress.getHostAddress();

        var width = vBox_FRRoomList.getWidth();
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
        vBox_FRRoomList.getChildren().add(pane);

    }

    private void JoinARoomButtonClick(ActionEvent actionEvent,InetAddress inetAddress,String roomName){
        TextInputDialog dialog = new TextInputDialog("Enter Name");
        dialog.setTitle("Text Input Dialog");
        dialog.setHeaderText(null);
        dialog.setContentText("Please enter your name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            if(result.get().trim() == ""){
                Client.playerMe.setName("Generic");
            }
            else{
                Client.playerMe.setName(result.get());
            }
            try {
                client.StopFindingServers();
                client.StopReceivingInet();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            StartClient(inetAddress);
            AddPlayerToList(client.playerMe,true);
            try{
                client.StopFindingServers();
                client.StopReceivingInet();
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
            HideOtherMainsExceptThis(anchorPane_Room);
            textField_RRoomName.setEditable(false);
            textField_RRoomName.setText(roomName);

        }
        else return;



    }

    public void buttonFindRoomBackClick(ActionEvent actionEvent) {
        HideOtherMainsExceptThis(anchorPane_Play);

        try{
            client.StopFindingServers();
            client.StopReceivingInet();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }

    //GAME MATH SCREEN

    public void buttonSendAnswer(ActionEvent actionEvent) {

        try{
            String gmAnswerText = textField_GMAnswer.getText();
            double answer = Double.parseDouble(gmAnswerText);
            SendAnswer(answer);
        }
        catch (Exception e){
            System.out.println("¤¤¤Parse Error: " + e.getMessage());
        }


    }



    public void ShowPlayBecauseYouGotKicked() {
        Platform.runLater(() -> {
           HideOtherMainsExceptThis(anchorPane_Play);
        });
    }






    ObservableList<InetAddress> inetAddresses = FXCollections.observableArrayList();
    public ObservableList<String> namelist = FXCollections.observableArrayList();
    public void ReceiveData(InetAddress inetAddress){
        System.out.println("Received InetAddress");
        boolean isUnique = true;
        for (InetAddress address: inetAddresses) {
           if(inetAddress == address){
               isUnique = false;
               break;
           }

        }
        if(isUnique)
            inetAddresses.add(inetAddress);

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






    private class RoomPlayerBox{


        DataPackages.Player player;
        Pane paneMain = new Pane();
        Label labelName = new Label();
        VBox parent = new VBox();

        RoomPlayerBox(VBox parent, DataPackages.Player player){
            this.player = player;
            this.parent = parent;

            var width = parent.getWidth();

            paneMain.setPrefSize(width/.90,60);
            paneMain.getStyleClass().add("serverpane");

            labelName = new Label(player.getName());
            labelName.getStyleClass().add("minortext");

            labelName.setLayoutX(5);
            labelName.setLayoutY(5);

            paneMain.getChildren().addAll(labelName);

            parent.getChildren().add(paneMain);
        }

        public void Remove(){
            parent.getChildren().remove(paneMain);
        }


    }

    public void StartServer(){
        server.StartMainServer();
    }

    public void StopServer(){
        server.StopMainServer();
    }

    public void StartClient(InetAddress inetAddress){
        client.StartMainClient(inetAddress);
    }

    public void StopClient(){
        client.StopMainClient();
    }

    public void SendAnswer(double answer){
        client.mainClientThread.SendAnswer(answer);
    }



}
