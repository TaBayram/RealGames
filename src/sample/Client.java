package sample;

import javafx.concurrent.Task;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.concurrent.TimeoutException;

public class Client {

    public static String serverName = "";

    public static Task StartClient(InetAddress inetAddress) {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    Socket socket = new Socket(inetAddress, 6666);
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    dataOutputStream.writeUTF("Hello Server");
                    dataOutputStream.flush();


                    while (true) {
                        var objectInputStream = new ObjectInputStream(socket.getInputStream());
                        String message = ((String) objectInputStream.readObject()).trim();

                        if(message.equals("Hi Client")){
                            System.out.println(message);
                        }
                        else if(message.startsWith("Players: ")){
                            String[] players = message.split(" ");
                            for (String player: players) {
                                System.out.println(player);

                            }
                        }
                        else{
                            System.out.println(message);
                        }

                        Thread.sleep(1000);
                        dataOutputStream = new DataOutputStream(socket.getOutputStream());
                        dataOutputStream.writeUTF("Hello After Sleep");
                        dataOutputStream.flush();

                    }

                } catch (Exception e) {
                    System.out.println("Client - " +e);
                }
                return null;
            }
        };
    }



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
                    while ((System.currentTimeMillis() - startTime) < 5000) {
                        System.out.println(">>>Inside time" + (System.currentTimeMillis() - startTime));

                        try {


                            //Wait for a response
                            byte[] receiveBuf = new byte[15000];
                            DatagramPacket receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
                            datagramSocket.setSoTimeout(5000);
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
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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



