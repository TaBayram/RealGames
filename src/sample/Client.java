package sample;

import javafx.concurrent.Task;

import java.io.*;
import java.net.*;

public class Client {
    public static Task StartClient(){
        return new Task() {
            @Override
            protected Object call() throws Exception {
                try{
                    Socket socket=new Socket("192.168.193.97",6666);
                    DataOutputStream dataOutputStream=new DataOutputStream(socket.getOutputStream());
                    dataOutputStream.writeUTF("Hello Server");
                    dataOutputStream.flush();
                    dataOutputStream.close();
                    socket.close();
                }catch(Exception e){
                    System.out.println(e);
                }
                return null;
            }
        };

    }


}
