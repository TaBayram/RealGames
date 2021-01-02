package sample;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.NoSuchElementException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

       /* var networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while(true) {
            try {

                var n = networkInterfaces.nextElement();
                System.out.println("___");
                var b = n.getInetAddresses();
                var f = n.getInterfaceAddresses();
                for (InterfaceAddress add:f) {
                    System.out.println("##");
                    System.out.println(add.getAddress());
                    System.out.println(add.getBroadcast());


                }
                System.out.println("##");
                while(true) {
                    try {
                        System.out.println("+++");
                        var c = b.nextElement();

                        System.out.println(c.getHostAddress()+" - " +c.getHostName());
                        System.out.println(c.getCanonicalHostName()+" - " +c.getAddress());
                    }
                    catch(NoSuchElementException e){
                        break;
                    }

                }
            }
            catch(NoSuchElementException e){
                break;
            }

        }*/

       /* InetAddress ip = InetAddress.getLocalHost();
        var hostname = ip.getHostName();
        System.out.println("Your current IP address : " + ip);
        System.out.println("Your current Hostname : " + hostname);*/


        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(300);
        primaryStage.setMaxHeight(900);
        primaryStage.setMaxWidth(900);
        primaryStage.setTitle("ConMath");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();



        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                controller.StopEverything();


            }
        });






    }



    public static void main(String[] args) {
        launch(args);
    }
}
