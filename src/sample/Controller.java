package sample;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

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

    public void buttonSearchRoomClick(ActionEvent actionEvent) {
        HideOtherMainsExceptThis(anchorPane_FindRooms);
        Main.StartClient();
    }

    public void buttonBackClick(ActionEvent actionEvent) {
        HideOtherMainsExceptThis(anchorPane_Play);
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
