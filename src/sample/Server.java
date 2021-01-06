package sample;

import java.io.*;
import java.net.ServerSocket;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Server {
    Controller controller;

    MainServer mainServerThread = new MainServer();
    DiscoveryThread discoveryThread = new DiscoveryThread();

    public static String ServerName = "Room";
    public static InetAddress serverAddress;

    final static Vector<ConnectedSocketsThread> clients = new Vector<>();
    final static Vector<PingPong> pingPongs = new Vector<>();

    private ConcurrentMath concurrentMath = new ConcurrentMath();
    Random random = new Random();


    Server(Controller controller){
        this.controller = controller;
    }






    public void StartMainServer(){
        clients.clear();
        concurrentMath = new ConcurrentMath();
        mainServerThread.canRun = true;
        if(!mainServerThread.isAlive()){
            mainServerThread = new MainServer();
            mainServerThread.start();
        }
        else{
            return;
        }
    }

    public void StopMainServer() {
        mainServerThread.canRun = false;
        if(mainServerThread == null || !mainServerThread.isAlive()) return;
        try{
            for(ConnectedSocketsThread socketsThread: clients){
                socketsThread.Disconnect(false);
            }

            pingPongs.clear();
        }
        catch(Exception exception){
            exception.printStackTrace();
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
        boolean canRun = true;
        ServerSocket serverSocket;
        ObjectInputStream objectInputStream;
        ObjectOutputStream objectOutputStream;
        Thread discoveryThread;

        @Override
        public void run() {
            try{
                StartDiscoveryThread();

                serverSocket = new ServerSocket(6666);
                serverAddress = serverSocket.getInetAddress();
                controller.StartClient(serverAddress);
                Socket socket = new Socket();

                while (canRun) {
                    try{
                        socket = serverSocket.accept();
                    }
                    catch(SocketException socketException){
                        //System.out.println("###Server Closed");
                        StopDiscoveryThread();
                        break;
                    }



                    objectInputStream = new ObjectInputStream(socket.getInputStream());
                    var packet = objectInputStream.readObject();


                    if(packet.getClass() == DataPackages.PinPong.class){
                        var packetPingPong = (DataPackages.PinPong)(packet);

                        if(packetPingPong.FirstPing){

                            ConnectedSocketsThread connectedSocketsThread = null;
                            for (ConnectedSocketsThread socketsThread:clients) {
                                if(socketsThread.socket.getInetAddress().getHostAddress().equals(socket.getInetAddress().getHostAddress())){
                                    connectedSocketsThread = socketsThread;
                                    break;
                                }
                            }

                            //System.out.println("First Ping");
                            PingPong pingPong = new PingPong(connectedSocketsThread,socket);
                            pingPong.start();
                            pingPongs.add(pingPong);
                        }

                    }

                    else if(packet.getClass() == DataPackages.Player.class){
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
                                //System.out.println("###Error in ID: " + exception);
                            }


                            var clientName = packetPlayer.getName();
                            //System.out.println("### Player has joined! " +clientName);

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
                //System.out.println("###Error " + e);
            }
        }

        public void SendLeavingPlayerPacket(ConnectedSocketsThread LeavingSocketThread){
            for (ConnectedSocketsThread connectedSocketsThread: clients) {
                if(LeavingSocketThread != connectedSocketsThread){
                    try {
                        //System.out.println("Leaver " +LeavingSocketThread.player.getName() +" Sending to " + connectedSocketsThread.player.getName());
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
                            Disconnect(true);
                        }


                    }
                    else if(packet.getClass() == DataPackages.MathQuestion.class){
                        var packetMathQuestion = (DataPackages.MathQuestion) (packet);

                        if(packetMathQuestion.isSendingAnswer()){
                            //System.out.println("##>Answer from: " +player.getName() + " - "+ packetMathQuestion.getAnswer());

                            player.setScore(player.getScore() + (int)Math.round(packetMathQuestion.getPoint()));

                            player.setSendingScore(true);
                            ObjectFlushAll(player);
                            player.setSendingScore(false);
                            player.setHasSentAnswer(true);


                            boolean hasAllSent = true;
                            for (ConnectedSocketsThread socketsThread:clients) {
                                if(!socketsThread.player.isHasSentAnswer()){
                                    hasAllSent = false;
                                }
                            }

                            if(hasAllSent){
                                for (ConnectedSocketsThread socketsThread:clients) {
                                    socketsThread.player.setHasSentAnswer(false);
                                }
                                DataPackages.GameCommand gameCommand = new DataPackages().new GameCommand();
                                gameCommand.setHasEveryoneSentAnswer(true);
                                ObjectFlushAll(gameCommand);
                            }


                        }

                    }


                    //GAME
                    else if(packet.getClass() == DataPackages.GameCommand.class){
                        var packetGameCommand = (DataPackages.GameCommand)(packet);

                        if(packetGameCommand.isEntering()){
                            ObjectFlushAll(packetGameCommand);
                            StopDiscoveryThread();
                        }
                        else if(packetGameCommand.isExiting()){
                            ObjectFlushAll(packetGameCommand);
                        }
                        else if(packetGameCommand.isStarting()){
                            nextQuestion();
                        }
                        else if(packetGameCommand.isNextLevel()){
                            nextQuestion();
                        }
                        else if(packetGameCommand.isEnding()){
                            ObjectFlushOthers(packetGameCommand);
                        }

                    }


                }
                catch(Exception exception){
                    //System.out.println("##>Error: "+exception.getMessage());
                }

            }
            Disconnect(true);
        }

        public synchronized void Disconnect(boolean announce){
            try {
                if(socket.isClosed()) return;
                canRun = false;
                player.setLeaving(true);

                if(announce) mainServerThread.SendLeavingPlayerPacket(this);

                ObjectFlushSelf(player);

                if (objectInputStream != null)
                    objectInputStream.close();
                if (objectOutputStream != null)
                    objectOutputStream.close();

                socket.close();

                for (PingPong pingPong:pingPongs) {
                    if(pingPong.connectedSocketsThread == this){
                        pingPong.Disconnect();
                    }
                }

            }
            catch (Exception e){
                //System.out.println("##>Error "+ e.getMessage());
            }
        }

        private void nextQuestion(){
            concurrentMath.level ++;
            concurrentMath.CreateQuestion();
            DataPackages.MathQuestion mathQuestion = new DataPackages().new MathQuestion();
            mathQuestion.setAnswer(concurrentMath.answer);
            mathQuestion.setQuestion(concurrentMath.question);
            mathQuestion.setPoint(concurrentMath.levelScore);
            mathQuestion.setLevel(concurrentMath.level);
            mathQuestion.setSendingQuestion(true);

            ObjectFlushAll(mathQuestion);
        }

        private void ObjectFlushSelf(Object object){
            try{
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(object);
                objectOutputStream.flush();
            }
            catch (Exception exception){
                //System.out.println(">>>Error Flush: "+exception.getMessage());
            }


        }

        private void ObjectFlushOthers(Object object){

            try{
                for(ConnectedSocketsThread socketsThread: clients){
                    if(!socketsThread.socket.isClosed()){
                        if(socketsThread != this){
                            objectOutputStream = new ObjectOutputStream(socketsThread.socket.getOutputStream());
                            objectOutputStream.writeObject(object);
                            objectOutputStream.flush();

                        }
                    }
                }

            }
            catch (Exception exception){
                //System.out.println(">>>Error Flush: "+exception.getMessage());
            }


        }

        private void ObjectFlushAll(Object object){

            try{
                for(ConnectedSocketsThread socketsThread: clients){
                    if(!socketsThread.socket.isClosed()){
                        objectOutputStream = new ObjectOutputStream(socketsThread.socket.getOutputStream());
                        objectOutputStream.writeObject(object);
                        objectOutputStream.flush();
                    }
                }
            }
            catch (Exception exception){
                //System.out.println(">>>Error Flush: "+exception.getMessage());
            }


        }

    }

    /*------------------- PING PONG -------------------*/

    class PingPong extends Thread{

        boolean canRun = true;
        ConnectedSocketsThread connectedSocketsThread;
        Socket socket;
        ObjectOutputStream objectOutputStream;
        ObjectInputStream objectInputStream;
        int noPingTimeout = 0;

        PingPong(ConnectedSocketsThread connectedSocketsThread,Socket socket){
            this.connectedSocketsThread = connectedSocketsThread;
            this.socket = socket;

        }

        @Override
        public void run() {
            try {
                socket.setSoTimeout(4000);

                var pinPong = new DataPackages().new PinPong(false);
                ObjectFlushClient(pinPong);

                while(canRun){
                    try{
                        objectInputStream = new ObjectInputStream(socket.getInputStream());
                    }
                    catch (Exception exception){
                        //System.out.println("###Ping Timeout Disconnect");
                        if(socket.isClosed()) {
                            Disconnect();
                            return;
                        }

                        noPingTimeout ++;

                        pinPong = new DataPackages().new PinPong(false);
                        ObjectFlushClient(pinPong);

                        if(noPingTimeout == 3){
                            //System.out.println("#Disconnect");
                            Disconnect();
                            break;
                        }

                        continue;
                    }

                    noPingTimeout = 0;

                    var packet = objectInputStream.readObject();

                    if(packet.getClass() == DataPackages.PinPong.class){
                        var packetPingPong = (DataPackages.PinPong)(packet);

                        if(packetPingPong.Ping){
                            //System.out.println("#Ping");
                            Thread.sleep(2000);
                            pinPong = new DataPackages().new PinPong(false);
                            ObjectFlushClient(pinPong);

                        }
                    }


                }


            } catch (Exception e) {
                //System.out.println("###Pong" +e);
            }
        }

        private void ObjectFlushClient(Object object){
            try{
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(object);
                objectOutputStream.flush();
            }
            catch (Exception exception){
                //System.out.println("###Error Pong Flush "+exception.getMessage());
            }


        }

        public synchronized void Disconnect(){
            try {
                if(this.socket.isClosed()) return;
                canRun = false;
                socket.close();
                connectedSocketsThread.Disconnect(true);
                pingPongs.remove(this);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }





    }


    /* --------------------- DISCOVER ME --------------------- */

    public void StartDiscoveryThread(){
        discoveryThread.canRun = true;
        if(!discoveryThread.isAlive()){
            discoveryThread = new DiscoveryThread();
            discoveryThread.start();
        }
        else{
            return;
        }
    }

    public void StopDiscoveryThread(){
        if(DiscoveryThread.datagramSocket != null)
            DiscoveryThread.datagramSocket.close();
    }


    public static class DiscoveryThread extends Thread {
        public boolean canRun = true;
        public static DatagramSocket datagramSocket;

        @Override
        public void run() {
            try {
                //Keep a socket open to listen to all the UDP traffic that is destined for this port
                datagramSocket = new DatagramSocket(6666, InetAddress.getByName("0.0.0.0"));
                datagramSocket.setBroadcast(true);

                while (canRun) {
                    //System.out.println(getClass().getName() + "###Ready to receive broadcast packets!");

                    //Receive a packet
                    byte[] receiveBuf = new byte[15000];
                    DatagramPacket packet = new DatagramPacket(receiveBuf, receiveBuf.length);
                    try {
                        datagramSocket.receive(packet);
                    }
                    catch(Exception exception){
                        //System.out.println(getClass().getName() + "###Discovery Stop:" );
                        break;
                    }


                    //Packet received
                    //System.out.println(getClass().getName() + "###Discovery packet received from: " + packet.getAddress().getHostAddress());
                    //System.out.println(getClass().getName() + "###Packet received; data: " + new String(packet.getData()));

                    //See if the packet holds the right command (message)
                    String message = new String(packet.getData()).trim();
                    if (message.equals("DISCOVER_FUIFSERVER_REQUEST")) {
                        byte[] sendData = ("DISCOVER_FUIFSERVER_RESPONSE "+ServerName).getBytes();

                        //Send a response
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                        datagramSocket.send(sendPacket);

                        //System.out.println(getClass().getName() + "###Sent packet to: " + sendPacket.getAddress().getHostAddress());
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(DiscoveryThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


    }

}
