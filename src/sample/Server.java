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
    public static InetAddress serverAddress;

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
        try{
            for(ConnectedSocketsThread socketsThread: clients){
                socketsThread.Disconnect();
            }
        }
        catch(Exception exception){

        }

    }

    public class MainServer extends Thread{


        @Override
        public void run() {
            try{
                Thread discoveryThread = new Thread(DiscoveryThread.getInstance());
                discoveryThread.start();

                ServerSocket serverSocket = new ServerSocket(6666);
                serverAddress = serverSocket.getInetAddress();
                controller.StartClient(serverAddress);
                Socket socket = new Socket();
                ObjectInputStream objectInputStream;
                ObjectOutputStream objectOutputStream;

                while (true) {
                    socket = serverSocket.accept();


                    objectInputStream = new ObjectInputStream(socket.getInputStream());
                    var packet = objectInputStream.readObject();

                    if(packet.getClass() == DataPackages.Player.class){
                        var packetPlayer = (DataPackages.Player)(packet);

                        if(packetPlayer.isJoining()){
                            packetPlayer.setJoining(false);
                            var clientName = packetPlayer.getName();
                            System.out.println("### Player has joined! " +clientName);

                            DataPackages.PlayerList playerList = new DataPackages().new PlayerList();
                            for (ConnectedSocketsThread connectedSocketsThread: clients) {

                                //SEND THE PLAYER JOINING TO EVERYONE IN THE ROOM
                                objectOutputStream = new ObjectOutputStream(connectedSocketsThread.socket.getOutputStream());
                                objectOutputStream.writeObject(packetPlayer);
                                objectOutputStream.flush();


                                playerList.getNames().add(connectedSocketsThread.player.getName());

                            }

                            //SEND THE JOINING PLAYER THE PLAYERS IN THE ROOM
                            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                            objectOutputStream.writeObject(playerList);
                            objectOutputStream.flush();

                            //START A THREAD FOR JOINING PLAYER
                            ConnectedSocketsThread connectedSocketsThread = new ConnectedSocketsThread(packetPlayer, socket);
                            connectedSocketsThread.start();

                            clients.add(connectedSocketsThread);

                        }
                    }






                }
            }
            catch(Exception e){
                System.out.println("###Error " + e);
            }
        }
    }





    public class ConnectedSocketsThread extends Thread{
        public DataPackages.Player player = new DataPackages().new Player();
        public Socket socket;
        DataInputStream dataInputStream;

        ConnectedSocketsThread(DataPackages.Player player, Socket socket){
            this.player = player;
            this.socket = socket;
        }

        public void Disconnect(){
            try {
                player.setLeaving(true);
                var objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(player);
                objectOutputStream.flush();
            }
            catch (Exception e){
                System.out.println("##>Error "+ e.getMessage());
            }
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
