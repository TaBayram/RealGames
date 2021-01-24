package sample;

import javafx.application.Application;
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
import javafx.stage.Stage;

import java.net.InetAddress;
import java.util.*;

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
    public Button button_RStartGame;
    public Button button_RCancelRoom;
    public Label label_Question;
    public Label label_GMScore;
    public VBox vBox_GMPlayer;
    public Label label_Timer;
    public Button button_GMLeaveGame;


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
        TextInputDialog dialog = new TextInputDialog("YAT");
        dialog.setTitle("Name");
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
            textField_RRoomName.setEditable(true);
            textField_RRoomName.setText("Room");
            button_RCancelRoom.setText("Cancel Room");
            button_RStartGame.setDisable(false);

        }

    }

    Timer timer_FindServer;

    public void buttonSearchRoomClick(ActionEvent actionEvent) {
        HideOtherMainsExceptThis(anchorPane_FindRooms);
        StartSearchingServers();

        TimerTask task = new TimerTask() {
            @Override public void run() {
                Platform.runLater(() -> {
                    vBox_FRRoomList.getChildren().removeAll();
                    vBox_FRRoomList.getChildren().clear();

                    for(int i = 0; i < inetAddresses.size(); i ++){
                        ServerListCreateServerBox(inetAddresses.get(i),namelist.get(i));
                    }
                    inetAddresses.clear();
                    namelist.clear();
                });

            }
        };

        timer_FindServer = new Timer();
        timer_FindServer.schedule(task,2000L,8000L);


    }

    public void buttonPBackClick(ActionEvent actionEvent) {
        HideOtherMainsExceptThis(anchorPane_MainMenu);
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

        button.setLayoutX(90);
        button.setLayoutY(10);
        labelIpAddress.setLayoutX(10);
        labelIpAddress.setLayoutY(40);
        labelRoomName.setLayoutX(5);
        labelRoomName.setLayoutY(5);


        pane.getChildren().addAll(button,labelIpAddress,labelRoomName);
        vBox_FRRoomList.getChildren().add(pane);

    }

    private void JoinARoomButtonClick(ActionEvent actionEvent,InetAddress inetAddress,String roomName){
        TextInputDialog dialog = new TextInputDialog("YAT");
        dialog.setTitle("Name");
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


            StartClient(inetAddress);


            HideOtherMainsExceptThis(anchorPane_Room);

            textField_RRoomName.setEditable(false);
            textField_RRoomName.setText(roomName);
            button_RCancelRoom.setText("Leave Room");
            button_RStartGame.setDisable(true);

        }
        else return;



    }

    public void buttonFindRoomBackClick(ActionEvent actionEvent) {
        HideOtherMainsExceptThis(anchorPane_Play);
        StopSearchingServers();
    }


    //ROOM SCREEN
    Vector<RoomPlayerBox> roomPlayerBoxes = new Vector<>();


    public void buttonStartGameClick(ActionEvent actionEvent) {
        if(isServerOwner){
            client.mainClientThread.EnterGame();
            client.mainClientThread.StartGame();
        }

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


    //GAME MATH SCREEN
    Vector<DataPackages.Player> players = new Vector<>();
    Vector<GamePlayerScoreBox> gamePlayerScoreBoxes = new Vector<>();



    Timer timer_GameCountdown;
    Timer timer_GameLevelTime;

    boolean gMHasSentAnswer = false;
    boolean gMCanAnswer = false;
    boolean gmHasGottenNextQuestion = false;
    boolean gMCountdownFinished = false;

    int questionAnswer = 0;
    int questionCountdown = 8;
    double gameLevelTime = 10;

    Date starTime = new Date(System.currentTimeMillis());
    DataPackages.MathQuestion mathQuestion = new DataPackages().new MathQuestion();

    public void buttonSendAnswer(ActionEvent actionEvent) {

        try{
            String gmAnswerText = textField_GMAnswer.getText();
            double answer = Double.parseDouble(gmAnswerText);

            if(answer == mathQuestion.getAnswer() && !gMHasSentAnswer && gMCanAnswer){
                gMHasSentAnswer = true;

                label_Question.setText("CORRECT");
                long seconds = (new Date(System.currentTimeMillis()).getTime()-starTime.getTime())/1000;
                int score =  (int)Math.round(((5.0/(5.0 + seconds) )*mathQuestion.getPoint()));

                label_GMScore.setText("Solved in " +seconds + " seconds. \t +" + score + " Points!");


                Client.playerMe.setScore(Client.playerMe.getScore() + score);

                mathQuestion.setPoint(score);

                SendAnswer(mathQuestion);
            }
        }
        catch (Exception e){
            //System.out.println("¤¤¤Parse Error: " + e.getMessage());
        }


    }

    public void ShowGameScreenAndStartTheClock(){
        Platform.runLater(() -> {
            HideOtherMainsExceptThis(anchorPane_GameMath);
            for (DataPackages.Player player : players) {
                GamePlayerScoreBox gamePlayerScoreBox = new GamePlayerScoreBox(vBox_GMPlayer, player);
                gamePlayerScoreBoxes.add(gamePlayerScoreBox);
            }
        });



    }

    public void ChangeScore(DataPackages.Player player, int score){
        Platform.runLater(() -> {
        for (GamePlayerScoreBox gamePlayerScoreBox : gamePlayerScoreBoxes) {
            if(gamePlayerScoreBox.player.getID() == player.getID()){
                gamePlayerScoreBox.SetScore(score);
            }
        }

        });
    }


    public void PrepareForNextLevel(){
        if(gmHasGottenNextQuestion) return;
        //System.out.println("¤¤¤Next Level Preparation");

        Platform.runLater(() -> {

            questionCountdown = 8;
            gameLevelTime = 10 + mathQuestion.getLevel()*1.50;
            gmHasGottenNextQuestion = true;
            gMCountdownFinished = false;
            questionAnswer = (int)mathQuestion.getAnswer()+0;
            TimerTask questionTask = new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        if (questionCountdown <= 0 && !gMCountdownFinished) {
                            gMCountdownFinished = true;
                            TimerTask gameLevelTimeTimerTask = new TimerTask() {
                                @Override
                                public void run() {
                                    Platform.runLater(() -> {
                                        if (gameLevelTime <= 0) {
                                            EndCurrentLevel();
                                        } else {
                                            int miliseconds = (int)Math.round((gameLevelTime%1.00) * 10);
                                            label_Timer.setText((int)Math.round(gameLevelTime-.500) + ":" + miliseconds);
                                            gameLevelTime -= 0.500;
                                        }

                                    });

                                }
                            };

                            timer_GameLevelTime = new Timer();
                            timer_GameLevelTime.schedule(gameLevelTimeTimerTask, 0, 500);

                            gMHasSentAnswer = false;
                            starTime = new Date(System.currentTimeMillis());
                            label_Question.setText(mathQuestion.getQuestion());

                            timer_GameCountdown.cancel();
                        } else if (questionCountdown <= 5) {
                            if(questionCountdown == 2){
                                textField_GMAnswer.setEditable(true);
                                textField_GMAnswer.setText("");
                                gMCanAnswer = true;
                            }
                            label_Question.setText("Level " + mathQuestion.getLevel() + " in "+questionCountdown + "...");

                        }
                        else{

                        }
                        questionCountdown--;

                    });

                }
            };

            timer_GameCountdown = new Timer();
            timer_GameCountdown.schedule(questionTask, 0, 1000);


        });
    }

    public void EndCurrentLevel(){
        if(!gmHasGottenNextQuestion) return;
        gMCanAnswer = false;
        gmHasGottenNextQuestion = false;

        Platform.runLater(()->{
            if(gmHasGottenNextQuestion) return;
           //System.out.println(mathQuestion.getLevel() + " Ending");


            label_Question.setText("Answer: "+ questionAnswer );
            textField_GMAnswer.setEditable(false);
            label_Timer.setText("00:00");
            timer_GameLevelTime.cancel();


            if(isServerOwner) {
                if(mathQuestion.getLevel() == 10){
                    StopGame();
                    EndGameShowWinner();
                }
                else
                NextLevel();
            }






        });

    }

    public void EndGameShowWinner(){
        FinishGame();
        Platform.runLater(()-> {
        var winnerPlayer = players.get(0);
        for (DataPackages.Player player:players) {
            if (player.getScore() > winnerPlayer.getScore()){
                winnerPlayer = player;
            }

        }

            label_Question.setText("WINNER: " + winnerPlayer.getName() + "\n\t Score: " + winnerPlayer.getScore());
        });

    }

    public void buttonGMLeaveGameClick(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Leave Game");
        if(isServerOwner) alert.setContentText("If you leave the game will end for everyone! Are you sure?");
        else
            alert.setContentText("Are you sure leaving the game?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            if(isServerOwner){
                StopServer();
            }
            else{
                StopClient();
                HideOtherMainsExceptThis(anchorPane_Play);
            }

        } else {

        }

    }






    ObservableList<InetAddress> inetAddresses = FXCollections.observableArrayList();
    public ObservableList<String> namelist = FXCollections.observableArrayList();
    public void ReceiveData(InetAddress inetAddress){
        //System.out.println("Received InetAddress");½
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

    public void ShowPlayBecauseYouGotKicked() {
        Platform.runLater(() -> {
            HideOtherMainsExceptThis(anchorPane_Play);
        });
    }

    public void buttonQuitApp(ActionEvent actionEvent) {
        Stage stage = (Stage) anchorPane_MainMenu.getScene().getWindow();
        stage.close();
    }


    private class GamePlayerScoreBox{


        DataPackages.Player player;
        int previousPoint;

        Pane paneMain = new Pane();
        Label labelName = new Label();
        Label labelTotalScore = new Label();
        Label labelPreviousLevelPoint = new Label();
        VBox parent = new VBox();

        GamePlayerScoreBox(VBox parent, DataPackages.Player player){
            this.player = player;
            this.parent = parent;

            var width = parent.getWidth();

            paneMain.setPrefSize(width/.90,60);
            paneMain.getStyleClass().add("gameplayerpane");

            labelName = new Label(player.getName());
            labelName.getStyleClass().add("gameplayertext");

            labelName.setLayoutX(5);
            labelName.setLayoutY(5);

            labelTotalScore = new Label("Total "+player.getScore());
            labelTotalScore.getStyleClass().add("gameplayertext");

            labelTotalScore.setLayoutX(5);
            labelTotalScore.setLayoutY(35);

            labelPreviousLevelPoint = new Label("Previous Point "+ player.getScore() );
            labelPreviousLevelPoint.getStyleClass().add("gameplayertext");

            labelPreviousLevelPoint.setLayoutX(40);
            labelPreviousLevelPoint.setLayoutY(20);

            previousPoint = player.getScore() +0;

            paneMain.getChildren().addAll(labelName,labelPreviousLevelPoint,labelTotalScore);

            parent.getChildren().add(paneMain);
        }

        public void SetScore(int score){
            player.setScore(score);
            previousPoint = score - previousPoint;
            labelTotalScore.setText("Total "+player.getScore());
            labelPreviousLevelPoint.setText("Previous Point "+ previousPoint );
            previousPoint = player.getScore() +0;

        }

        public void Remove(){
            parent.getChildren().remove(paneMain);
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

    public void StartSearchingServers(){
        client.StartFindingServers();
        client.StartReceivingInet();
    }

    public void StopSearchingServers(){
        if(timer_FindServer != null) timer_FindServer.cancel();
        try{
            client.StopFindingServers();
            client.StopReceivingInet();
        }catch (Exception e){
            //System.out.println(e.getMessage());
        }

    }

    public void StartServer(){
        vBox_RoomPlayerList.getChildren().clear();
        vBox_GMPlayer.getChildren().clear();
        listView_RLog.getItems().clear();
        players.clear();



        server.StartMainServer();
    }

    public void StopServer(){
        server.StopMainServer();
    }

    public void StartClient(InetAddress inetAddress){
        StopSearchingServers();
        client.StartMainClient(inetAddress);
    }

    public void StopClient(){
        client.StopMainClient();
    }

    public void SendAnswer(DataPackages.MathQuestion mathQuestion){
        client.mainClientThread.SendAnswer(mathQuestion);
    }

    public void StartGame(){
        client.mainClientThread.StartGame();

    }

    public void StopGame(){
        client.mainClientThread.StopGame();


    }

    public void NextLevel(){
        client.mainClientThread.NextLevel();

    }

    public void FinishGame(){
        if(this.timer_GameCountdown != null)
            this.timer_GameCountdown.cancel();
        if(this.timer_GameLevelTime != null)
            this.timer_GameLevelTime.cancel();;

    }

    public void StopEverything(){
        StopClient();
        StopServer();
        StopSearchingServers();

        if(this.timer_GameCountdown != null)
            this.timer_GameCountdown.cancel();
        if(this.timer_FindServer != null)
            this.timer_FindServer.cancel();
        if(this.timer_GameLevelTime != null)
            this.timer_GameLevelTime.cancel();;

    }



}
