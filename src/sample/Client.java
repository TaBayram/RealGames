package sample;

import java.io.*;
import java.net.*;
import java.util.Enumeration;

public class Client {

    static Controller controller;

    MainClient mainClientThread = new MainClient(null);
    private boolean mainClientThreadCanRun = false;

    PingPong pingPong;



    public static String serverName = "";
    public static DataPackages.Player playerMe = new DataPackages().new Player();


    Client(Controller controller){
        this.controller = controller;

    }

    public void StartMainClient(InetAddress inetAddress){
        mainClientThreadCanRun = true;
        if(!mainClientThread.isAlive()){
            mainClientThread = new MainClient(inetAddress);
            mainClientThread.start();
        }
        else{

        }


    }

    public void StopMainClient() {
        if(mainClientThread == null || !mainClientThread.isAlive()) return;
        mainClientThreadCanRun = false;
        mainClientThread.LeaveRoom();


    }

    public class MainClient extends Thread{
        public InetAddress inetAddress;
        public Socket socket;
        public ObjectOutputStream objectOutputStream;
        public ObjectInputStream objectInputStream;
        public DataPackages.PlayerList playerList= new DataPackages().new PlayerList();

        MainClient(InetAddress inetAddress){
            this.inetAddress = inetAddress;
        }


        @Override
        public void run() {
            try {
                socket = new Socket(inetAddress, 6666);

                //SEND YOURSELF AS JOINING PLAYER IN THE ROOM
                playerMe.setJoining(true);
                ObjectFlush(playerMe);
                playerMe.setJoining(false);

                StartPingPong(inetAddress);


                while (mainClientThreadCanRun) {
                    objectInputStream = new ObjectInputStream(socket.getInputStream());
                    var packet = objectInputStream.readObject();

                    if(packet.getClass() == DataPackages.Player.class){
                        var packetPlayer = (DataPackages.Player)(packet);

                        //A PLAYER HAS JOINED THE ROOM
                        if(packetPlayer.isJoining()){
                            System.out.println(">>> Player has joined! " +packetPlayer.getName());
                            controller.AddPlayerToList(packetPlayer,true);

                            //Add Newcomer
                            controller.players.add(packetPlayer);

                        }
                        else if(packetPlayer.isLeaving()){

                            if(packetPlayer.getID() == playerMe.getID()){
                                System.out.println(">>> Getting Kicked");
                                controller.ShowPlayBecauseYouGotKicked();
                                socket.close();
                                break;
                            }
                            else{
                                controller.RemovePlayerFromList(packetPlayer);
                                System.out.println(">>> Player has left!" + packetPlayer.getName());

                            }
                        }
                        else if(packetPlayer.isChecking()){
                            playerMe.setID(packetPlayer.getID());
                            controller.AddPlayerToList(playerMe,true);

                            //Add Myself
                            controller.players.add(playerMe);

                        }

                        else if(packetPlayer.isSendingScore()){
                            controller.ChangeScore(packetPlayer,packetPlayer.getScore());

                        }

                    }
                    //YOU HAVE JOINED THE ROOM AND GETTING THE PLAYER LIST
                    else if(packet.getClass() == DataPackages.PlayerList.class){
                        var packetPlayer = (DataPackages.PlayerList)(packet);
                        for (DataPackages.Player player: packetPlayer.getPlayers()) {
                            controller.AddPlayerToList(player,false);

                            //Add Before Me
                            controller.players.add(player);
                        }
                        this.playerList = packetPlayer;
                    }

                    //GAME
                    else if(packet.getClass() == DataPackages.GameCommand.class){
                        var packetGameCommand = (DataPackages.GameCommand)(packet);

                        if(packetGameCommand.isEntering()){
                            controller.ShowGameScreenAndStartTheClock();

                        }
                        else if(packetGameCommand.isExiting()){
                            controller.ShowPlayBecauseYouGotKicked();
                        }
                        else if(packetGameCommand.isHasEveryoneSentAnswer()){
                            controller.EndCurrentLevel();
                        }
                        else if(packetGameCommand.isEnding()){
                            controller.EndCurrentLevel();
                        }

                    }

                    //MATH
                    else if(packet.getClass() == DataPackages.MathQuestion.class){
                        var packetMathQuestion = (DataPackages.MathQuestion)(packet);

                        //RECEIVING QUESTION AND STARTING THE CLOCK
                        if(packetMathQuestion.isSendingQuestion()){
                            controller.mathQuestion = packetMathQuestion;
                            controller.PrepareForNextLevel();
                        }


                    }

                }

            } catch (Exception e) {
                System.out.println(">>>Error " +e);
            }
        }

        public void EnterGame(){
            DataPackages.GameCommand gameCommand = new DataPackages().new GameCommand();
            gameCommand.setEntering(true);
            ObjectFlush(gameCommand);
        }

        public void ExitGame(){
            DataPackages.GameCommand gameCommand = new DataPackages().new GameCommand();
            gameCommand.setExiting(true);
            ObjectFlush(gameCommand);
        }

        public void StartGame(){
            DataPackages.GameCommand gameCommand = new DataPackages().new GameCommand();
            gameCommand.setStarting(true);
            ObjectFlush(gameCommand);
        }

        public void NextLevel(){
            DataPackages.GameCommand gameCommand = new DataPackages().new GameCommand();
            gameCommand.setNextLevel(true);
            ObjectFlush(gameCommand);

        }


        public void SendAnswer(DataPackages.MathQuestion mathQuestion){
            try{

                mathQuestion.setSendingAnswer(true);
                ObjectFlush(mathQuestion);


            }
            catch (Exception exception){
                System.out.println(">>>Error Answer: "+exception.getMessage());
            }


        }

        public void LeaveRoom(){
            try{

                playerMe.setLeaving(true);
                ObjectFlush(playerMe);
                playerMe.setLeaving(false);

            }
            catch (Exception exception){
                System.out.println(">>>Error Leaving: "+exception.getMessage());
            }

        }

        private void ObjectFlush(Object object){

            try{
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(object);
                objectOutputStream.flush();
            }
            catch (Exception exception){
                System.out.println(">>>Error Flush: "+exception.getMessage());
            }


        }

    }




    /*------------------- PING PONG -------------------*/

    public void StartPingPong(InetAddress inetAddress){
        pingPong = new PingPong(inetAddress);
        pingPong.start();


    }

    public void StopPingPong(){

    }

    class PingPong extends  Thread{

        boolean canRun = true;
        Socket socket;
        ObjectOutputStream objectOutputStream;
        ObjectInputStream objectInputStream;
        InetAddress inetAddress;
        int noPongTimeout = 0;

        PingPong(InetAddress inetAddress){
           this.inetAddress = inetAddress;

        }

        @Override
        public void run() {


            try {

                socket = new Socket(inetAddress, 6666);
                socket.setSoTimeout(8000);

                DataPackages.PinPong pinPong = new DataPackages().new PinPong(true);
                pinPong.FirstPing = true;
                ObjectFlushServer(pinPong);


                while(canRun){

                    try{
                        objectInputStream = new ObjectInputStream(socket.getInputStream());
                    }
                    catch (Exception exception){
                        System.out.println(">>>Pong Timeout Disconnect");
                        noPongTimeout ++;

                        pinPong = new DataPackages().new PinPong(true);
                        ObjectFlushServer(pinPong);

                        if(noPongTimeout == 3){
                            System.out.println("Disconnect");
                            Disconnect();
                            break;
                        }

                        continue;
                    }

                    noPongTimeout = 0;

                    var packet = objectInputStream.readObject();

                    if(packet.getClass() == DataPackages.PinPong.class){
                        var packetPingPong = (DataPackages.PinPong)(packet);

                        if(packetPingPong.Pong){
                            System.out.println("Pong");
                            Thread.sleep(5000);
                            pinPong = new DataPackages().new PinPong(true);
                            ObjectFlushServer(pinPong);

                        }
                    }


                }


            } catch (Exception e) {
                System.out.println(">>>PING " +e);
            }
        }

        private void ObjectFlushServer(Object object){

            try{
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(object);
                objectOutputStream.flush();
            }
            catch (Exception exception){
                System.out.println(">>>Error Flush: "+exception.getMessage());
            }


        }

        public void Disconnect(){
            canRun = false;
            StopMainClient();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }


    }



    /* ------------------- FIND SERVER -------------------*/

    FindServers findServersThread = new FindServers();
    private boolean findServersThreadCanRun = false;

    public void StartFindingServers(){
        findServersThreadCanRun = true;
        if(!findServersThread.isAlive()){
            findServersThread = new FindServers();
            findServersThread.start();
        }
        else{

        }


    }

    public void StopFindingServers() throws InterruptedException {
        findServersThreadCanRun = false;

    }


    class FindServers extends Thread{
        public void run() {
            // Find the server using UDP broadcast
            while(findServersThreadCanRun) {
                try {
                    //Open a random port to send the package
                    var datagramSocket = new DatagramSocket();
                    datagramSocket.setBroadcast(true);

                    byte[] sendData = "DISCOVER_FUIFSERVER_REQUEST".getBytes();

                    //Try the 255.255.255.255 first
                    try {
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 8888);
                        datagramSocket.send(sendPacket);
                        System.out.println(Client.class.getName() + ">>> Request packet sent to: 255.255.255.255 (DEFAULT)");
                    } catch (Exception e) {
                    }

                    // Broadcast the message over all the network interfaces
                    Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
                    while (interfaces.hasMoreElements()) {
                        NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

                        if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                            continue; // Don't want to broadcast to the loopback interface
                        }

                        for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                            InetAddress broadcast = interfaceAddress.getBroadcast();
                            if (broadcast == null) {
                                continue;
                            }

                            // Send the broadcast package!
                            try {
                                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 6666);
                                datagramSocket.send(sendPacket);
                            } catch (Exception e) {
                            }

                            System.out.println(Client.class.getName() + ">>> Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
                        }
                    }

                    System.out.println(Client.class.getName() + ">>> Done looping over all network interfaces. Now waiting for a reply!");
                    long startTime = System.currentTimeMillis();
                    System.out.println(">>>Outside time" + (System.currentTimeMillis() - startTime));
                    while ((System.currentTimeMillis() - startTime) < 8000 && findServersThreadCanRun) {
                        System.out.println(">>>Inside time" + (System.currentTimeMillis() - startTime));

                        try {


                            //Wait for a response
                            byte[] receiveBuf = new byte[15000];
                            DatagramPacket receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
                            datagramSocket.setSoTimeout(2000);
                            datagramSocket.receive(receivePacket);

                            //We have a response
                            System.out.println(Client.class.getName() + ">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());

                            //Check if the message is correct
                            String[] fullMessage = new String(receivePacket.getData()).split(" ");
                            String message = fullMessage[0];
                            if (message.equals("DISCOVER_FUIFSERVER_RESPONSE")) {
                                //DO SOMETHING WITH THE SERVER'S IP (for example, store it in your controller)
                                System.out.println((">>>Server Address") + receivePacket.getAddress());
                                serverName = fullMessage[1];
                                Sender sender = new Sender(receivePacket.getAddress());
                                Thread thread = new Thread(sender);
                                thread.setDaemon(true);
                                thread.start();
                            }
                        }
                        catch (SocketTimeoutException socketTimeoutException ){
                            System.out.println(">>>Timeout No Response");
                        }
                    }


                    //Close the port!
                    datagramSocket.close();
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());

                }
            };

        }


    }

    private static boolean transfer = true;
    private static InetAddress packet;
    private Thread receiverThread = new Thread();
    private boolean receiverThreadCanRun = false;

    public void StartReceivingInet(Controller controller){
        receiverThreadCanRun = true;
        if(!receiverThread.isAlive()){
            Receiver receiver = new Receiver(controller);
            receiverThread = new Thread(receiver);
            receiverThread.start();
        }
        else{

        }


    }

    public void StopReceivingInet() throws InterruptedException {
        receiverThreadCanRun = false;
        Sender sender = new Sender(null);
        Thread thread = new Thread(sender);
        thread.setDaemon(true);
        thread.start();

    }


    public class Sender implements Runnable {
        private InetAddress data;

        // standard constructors
        public Sender(InetAddress inetAddress){
            this.data = inetAddress;

        }
        public void run() {
                send(data);
        }
    }

    public class Receiver implements Runnable {
        private InetAddress load;
        private Controller controller;

        public Receiver(Controller controller){
            this.controller = controller;
        }
        // standard constructors

        public void run() {
            while(receiverThreadCanRun) {
                load = receive();
                if(load == null) break;
                controller.ReceiveData(load);
                controller.namelist.add(serverName);
            }

        }
    }

    public synchronized void send(InetAddress packet) {
        while (!transfer) {
            try {
                wait();
            } catch (InterruptedException e)  {
                Thread.currentThread().interrupt();
                //Log.error("Thread interrupted", e);
            }
        }
        transfer = false;

        this.packet = packet;
        notifyAll();
    }

    public synchronized InetAddress receive() {
        while (transfer) {
            try {
                wait();
            } catch (InterruptedException e)  {
                Thread.currentThread().interrupt();
                //Log.error("Thread interrupted", e);
            }
        }
        transfer = true;

        notifyAll();
        return packet;
    }

}



