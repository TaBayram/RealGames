package sample;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.io.*;
import java.net.ServerSocket;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    public static Task StartServer(){
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
                        dataInputStream = new DataInputStream((socket.getInputStream()));
                        String str = (String) dataInputStream.readUTF();
                        System.out.println(str);


                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                        objectOutputStream.writeObject("Hi Client ");
                        objectOutputStream.flush();

                    }
                }
                catch(Exception e){
                    System.out.println("Server - " + e);
                }
                return null;
            }
        };
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
                    System.out.println(getClass().getName() + ">>>Ready to receive broadcast packets!");

                    //Receive a packet
                    byte[] receiveBuf = new byte[15000];
                    DatagramPacket packet = new DatagramPacket(receiveBuf, receiveBuf.length);
                    socket.receive(packet);

                    //Packet received
                    System.out.println(getClass().getName() + ">>>Discovery packet received from: " + packet.getAddress().getHostAddress());
                    System.out.println(getClass().getName() + ">>>Packet received; data: " + new String(packet.getData()));

                    //See if the packet holds the right command (message)
                    String message = new String(packet.getData()).trim();
                    if (message.equals("DISCOVER_FUIFSERVER_REQUEST")) {
                        byte[] sendData = "DISCOVER_FUIFSERVER_RESPONSE".getBytes();

                        //Send a response
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                        socket.send(sendPacket);

                        System.out.println(getClass().getName() + ">>>Sent packet to: " + sendPacket.getAddress().getHostAddress());
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
