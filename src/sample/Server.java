package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.ServerSocket;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    Controller controller;
    Server(Controller controller){
        this.controller = controller;

    }


    public static String ServerName = "Room";

    public static ObservableList<String> players = FXCollections.observableArrayList();
    final static List<ConnectedSocketsThread> clients = new ArrayList<>();

    MainServer mainServerThread = new MainServer();
    private boolean mainServerThreadCanRun = false;

    public void StartMainServer(){
        mainServerThreadCanRun = true;
        if(!mainServerThread.isAlive()){
            mainServerThread = new MainServer();
            mainServerThread.start();
        }
        else{

        }


    }

    public void StopMainServer() {
        mainServerThreadCanRun = false;

    }

    public class MainServer extends Thread{
        @Override
        public void run() {
            try{
                Thread discoveryThread = new Thread(DiscoveryThread.getInstance());
                discoveryThread.start();

                ServerSocket serverSocket = new ServerSocket(6666);
                Socket socket = new Socket();
                DataInputStream dataInputStream;
                ObjectOutputStream objectOutputStream;

                while (true) {
                    socket = serverSocket.accept();

                    dataInputStream = new DataInputStream((socket.getInputStream()));
                    String firstMessage = (String) dataInputStream.readUTF();
                    System.out.println(firstMessage);
                    String clientName = firstMessage.substring(firstMessage.indexOf(" "));

                    String players = "Players: ";
                    for (ConnectedSocketsThread connectedSocketsThread: clients) {
                        objectOutputStream = new ObjectOutputStream(connectedSocketsThread.socket.getOutputStream());
                        objectOutputStream.writeObject(clientName);
                        objectOutputStream.flush();
                        players += connectedSocketsThread.clientName + " ";

                    }

                    ConnectedSocketsThread connectedSocketsThread = new ConnectedSocketsThread(clientName, socket);
                    connectedSocketsThread.start();
                    clients.add(connectedSocketsThread);

                    controller.AddPlayerToList(clientName);



                    objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    objectOutputStream.writeObject("HiClient");
                    objectOutputStream.flush();

                    objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    objectOutputStream.writeObject(players);
                    objectOutputStream.flush();


                }
            }
            catch(Exception e){
                System.out.println("Server - " + e);
            }
        }
    }





   /* public static Task StartServer(){
        return new Task() {
            @Override
            protected Object call() throws Exception {
                try{
                    Thread discoveryThread = new Thread(DiscoveryThread.getInstance());
                    discoveryThread.start();

                    ServerSocket serverSocket = new ServerSocket(6666);
                    Socket socket = new Socket();
                    DataInputStream dataInputStream;

                    while (true) {
                        socket = serverSocket.accept();
                        clients.add(new ConnectedSocketsThread(socket));
                        dataInputStream = new DataInputStream((socket.getInputStream()));
                        String str = (String) dataInputStream.readUTF();
                        System.out.println(str);


                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                        objectOutputStream.writeObject("Hi Client");
                        objectOutputStream.flush();

                    }
                }
                catch(Exception e){
                    System.out.println("Server - " + e);
                }
                return null;
            }
        };
    }*/

    public class ConnectedSocketsThread extends Thread{
        public String clientName;
        public Socket socket;
        DataInputStream dataInputStream;

        ConnectedSocketsThread(String clientName, Socket socket){
            this.clientName = clientName;
            this.socket = socket;
        }

        @Override
        public void run() {
            while(socket.isConnected()) {
                try{
                    dataInputStream = new DataInputStream((socket.getInputStream()));
                    String str = (String) dataInputStream.readUTF();
                    System.out.println(str);
                }
                catch(Exception exception){

                }

            }
            //ANNOUNCE THE DC
        }
    }


    public static class DiscoveryThread implements Runnable {

        DatagramSocket socket;

        @Override
        public void run() {
            try {
                //Keep a socket open to listen to all the UDP traffic that is destined for this port
                socket = new DatagramSocket(6666, InetAddress.getByName("0.0.0.0"));
                socket.setBroadcast(true);

                while (true) {
                    System.out.println(getClass().getName() + "###Ready to receive broadcast packets!");

                    //Receive a packet
                    byte[] receiveBuf = new byte[15000];
                    DatagramPacket packet = new DatagramPacket(receiveBuf, receiveBuf.length);
                    socket.receive(packet);

                    //Packet received
                    System.out.println(getClass().getName() + "###Discovery packet received from: " + packet.getAddress().getHostAddress());
                    System.out.println(getClass().getName() + "###Packet received; data: " + new String(packet.getData()));

                    //See if the packet holds the right command (message)
                    String message = new String(packet.getData()).trim();
                    if (message.equals("DISCOVER_FUIFSERVER_REQUEST")) {
                        byte[] sendData = ("DISCOVER_FUIFSERVER_RESPONSE "+ServerName).getBytes();

                        //Send a response
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                        socket.send(sendPacket);

                        System.out.println(getClass().getName() + "###Sent packet to: " + sendPacket.getAddress().getHostAddress());
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(DiscoveryThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public static DiscoveryThread getInstance() {
            return DiscoveryThreadHolder.INSTANCE;
        }

        private static class DiscoveryThreadHolder {

            private static final DiscoveryThread INSTANCE = new DiscoveryThread();
        }

    }

}
