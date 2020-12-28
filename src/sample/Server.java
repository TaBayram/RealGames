package sample;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.io.*;
import java.net.ServerSocket;
import java.net.*;

public class Server {
    public static Task StartServer(){
        return new Task() {
            @Override
            protected Object call() throws Exception {
                try{
                    ServerSocket serverSocket = new ServerSocket(6666);
                    Socket socket = new Socket();
                    DataInputStream dis;
                    while (true) {
                        socket = serverSocket.accept();
                        dis = new DataInputStream((socket.getInputStream()));
                        String str = (String) dis.readUTF();
                        System.out.println(str);
                    }
                }
                catch(Exception e){
                    System.out.println(e);
                }
                return null;
            }
        };
    }

}
