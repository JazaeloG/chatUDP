package chatUDP;

import java.io.IOException;
import java.net.*;

public class UDPClient {
    private DatagramSocket socket;
    private String username;
    private InetAddress serverAddress;
    private int serverPort;
    private ChatClientUI ui;

    public UDPClient(String serverIP, int serverPort, String username) throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        this.serverAddress = InetAddress.getByName(serverIP);
        this.serverPort = serverPort;
        this.username = username;
        this.ui = new ChatClientUI(this);

        // Enviar mensaje de conexión
        sendMessage("CONNECT");
    }

    public void sendMessage(String message) {
        try {
            System.out.println("Enviando mensaje: " + message);
            String send = message +","+username;
            byte[] buffer = send.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, serverPort);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveMessages() {
        new Thread(() -> {
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                    String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                    ui.displayMessage(receivedMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void disconnect() {
        // Enviar mensaje de desconexión
        sendMessage("DISCONNECT");
    }

    public static void main(String[] args) throws SocketException, UnknownHostException {
        UDPClient client = new UDPClient("192.168.137.226", 12345, "Jazael") ;
        client.receiveMessages();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            client.disconnect();
        }));
    }

}