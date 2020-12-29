package sample;

import javafx.concurrent.Task;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    public static Task StartClient() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                var inetAddress = FindServer();
                try {
                    Socket socket = new Socket(inetAddress, 6666);
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    dataOutputStream.writeUTF("Hello Server");
                    dataOutputStream.flush();


                    while (true) {
                        var objectInputStream = new ObjectInputStream(socket.getInputStream());
                        String message = (String) objectInputStream.readObject();
                        System.out.println("Message: " + message);
                    }

                } catch (Exception e) {
                    System.out.println("Client - " +e);
                }
                return null;
            }
        };
    }
        public static InetAddress FindServer() {
            // Find the server using UDP broadcast
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

                //Wait for a response
                byte[] receiveBuf = new byte[15000];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
                datagramSocket.receive(receivePacket);

                //We have a response
                System.out.println(Client.class.getName() + ">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());

                //Check if the message is correct
                String message = new String(receivePacket.getData()).trim();
                if (message.equals("DISCOVER_FUIFSERVER_RESPONSE")) {
                    //DO SOMETHING WITH THE SERVER'S IP (for example, store it in your controller)
                    System.out.println(("Socket Side ") + receivePacket.getAddress());
                    return receivePacket.getAddress();
                }

                //Close the port!
                datagramSocket.close();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());

            }
            return  null;
        }

}
