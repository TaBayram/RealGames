package sample;

import java.io.*;
import java.net.ServerSocket;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

        try {
            if (mainServerThread.objectInputStream != null)
                mainServerThread.objectInputStream.close();
            if (mainServerThread.objectOutputStream != null)
                mainServerThread.objectOutputStream.close();
            mainServerThread.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public class MainServer extends Thread{
        ServerSocket serverSocket;
        ObjectInputStream objectInputStream;
        ObjectOutputStream objectOutputStream;

        @Override
        public void run() {
            try{
                Thread discoveryThread = new Thread(DiscoveryThread.getInstance());
                discoveryThread.start();

                serverSocket = new ServerSocket(6666);
                serverAddress = serverSocket.getInetAddress();
                controller.StartClient(serverAddress);
                Socket socket = new Socket();

                while (mainServerThreadCanRun) {
                    try{
                        socket = serverSocket.accept();
                    }
                    catch(SocketException socketException){
                        System.out.println("###Server Closed");
                        DiscoveryThread.datagramSocket.close();
                        break;
                    }



                    objectInputStream = new ObjectInputStream(socket.getInputStream());
                    var packet = objectInputStream.readObject();

                    if(packet.getClass() == DataPackages.Player.class){
                        var packetPlayer = (DataPackages.Player)(packet);

                        if(packetPlayer.isJoining()){

                            //GIVE THE JOINING PLAYER UNIQUE ID
                            int ID;
                            while(true) {
                                ID = 100000 + random.nextInt(99999);
                                boolean isUnique = true;
                                for (ConnectedSocketsThread connectedSocketsThread : clients) {
                                    if (ID == connectedSocketsThread.player.getID()) {
                                        isUnique = false;
                                        break;
                                    }
                                }
                                if(isUnique) break;
                            }
                            packetPlayer.setID(ID);

                            try{
                                packetPlayer.setJoining(false);
                                packetPlayer.setChecking(true);
                                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                                objectOutputStream.writeObject(packetPlayer);
                                objectOutputStream.flush();
                                packetPlayer.setChecking(false);
                                packetPlayer.setJoining(true);
                            }
                            catch (Exception exception){
                                System.out.println("###Error in ID: " + exception);
                            }


                            var clientName = packetPlayer.getName();
                            System.out.println("### Player has joined! " +clientName);

                            DataPackages.PlayerList playerList = new DataPackages().new PlayerList();
                            for (ConnectedSocketsThread connectedSocketsThread: clients) {

                                //SEND THE PLAYER JOINING TO EVERYONE IN THE ROOM
                                objectOutputStream = new ObjectOutputStream(connectedSocketsThread.socket.getOutputStream());
                                objectOutputStream.writeObject(packetPlayer);
                                objectOutputStream.flush();


                                playerList.getPlayers().add(connectedSocketsThread.player);

                            }

                            //SEND THE JOINING PLAYER THE PLAYERS IN THE ROOM
                            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                            objectOutputStream.writeObject(playerList);
                            objectOutputStream.flush();

                            //START A THREAD FOR JOINING PLAYER
                            packetPlayer.setJoining(false);
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

        public void SendLeavingPlayerPacket(ConnectedSocketsThread LeavingSocketThread){
            for (ConnectedSocketsThread connectedSocketsThread: clients) {
                if(LeavingSocketThread != connectedSocketsThread){
                    try {
                        System.out.println("Leaver " +LeavingSocketThread.player.getName() +" Sending to " + connectedSocketsThread.player.getName());
                        objectOutputStream = new ObjectOutputStream(connectedSocketsThread.socket.getOutputStream());
                        objectOutputStream.writeObject(LeavingSocketThread.player);
                        objectOutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }

            }

            clients.remove(LeavingSocketThread);

        }
    }



    Random random = new Random();

    public class ConnectedSocketsThread extends Thread{
        private boolean canRun = true;

        public DataPackages.Player player = new DataPackages().new Player();
        public Socket socket;

        ObjectInputStream objectInputStream;
        ObjectOutputStream objectOutputStream;

        ConnectedSocketsThread(DataPackages.Player player, Socket socket){
            this.player = player;
            this.socket = socket;


        }

        @Override
        public void run() {
            while(!socket.isClosed() && canRun) {
                try{
                    objectInputStream = new ObjectInputStream((socket.getInputStream()));
                    var packet = objectInputStream.readObject();

                    if(packet.getClass() == DataPackages.Player.class) {
                        var packetPlayer = (DataPackages.Player) (packet);

                        if(packetPlayer.isLeaving()){
                            Disconnect();
                        }


                    }
                    else if(packet.getClass() == DataPackages.MathQuestion.class){
                        var packetMathQuestion = (DataPackages.MathQuestion) (packet);

                        if(packetMathQuestion.isSendingAnswer()){
                            System.out.println("##>Answer from: " +player.getName() + " - "+ packetMathQuestion.getAnswer());
                        }

                    }
                }
                catch(Exception exception){
                    System.out.println("##>Error: "+exception.getMessage());
                }

            }
            Disconnect();
        }

        public void Disconnect(){
            try {
                if(socket.isClosed()) return;
                canRun = false;

                player.setLeaving(true);
                mainServerThread.SendLeavingPlayerPacket(this);

                var objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(player);
                objectOutputStream.flush();


                if (objectInputStream != null)
                    objectInputStream.close();
                if (objectOutputStream != null)
                    objectOutputStream.close();
                socket.close();
            }
            catch (Exception e){
                System.out.println("##>Error "+ e.getMessage());
            }
        }
    }


    public static class DiscoveryThread implements Runnable {
        public static DatagramSocket datagramSocket;

        @Override
        public void run() {
            try {
                //Keep a socket open to listen to all the UDP traffic that is destined for this port
                datagramSocket = new DatagramSocket(6666, InetAddress.getByName("0.0.0.0"));
                datagramSocket.setBroadcast(true);

                while (true) {
                    System.out.println(getClass().getName() + "###Ready to receive broadcast packets!");

                    //Receive a packet
                    byte[] receiveBuf = new byte[15000];
                    DatagramPacket packet = new DatagramPacket(receiveBuf, receiveBuf.length);
                    try {
                        datagramSocket.receive(packet);
                    }
                    catch(Exception exception){
                        break;
                    }


                    //Packet received
                    System.out.println(getClass().getName() + "###Discovery packet received from: " + packet.getAddress().getHostAddress());
                    System.out.println(getClass().getName() + "###Packet received; data: " + new String(packet.getData()));

                    //See if the packet holds the right command (message)
                    String message = new String(packet.getData()).trim();
                    if (message.equals("DISCOVER_FUIFSERVER_REQUEST")) {
                        byte[] sendData = ("DISCOVER_FUIFSERVER_RESPONSE "+ServerName).getBytes();

                        //Send a response
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                        datagramSocket.send(sendPacket);

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
