package chatUDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 *
 * @author omar_
 */
public class UDPClient {
    private String username;
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;
    private ChatClientUI ui;

    public UDPClient(String username,String serverIP, int serverPort) throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        this.username = username;
        this.serverAddress = InetAddress.getByName(serverIP);
        this.serverPort = serverPort;
        this.ui = new ChatClientUI(this);

        // Enviar mensaje de conexión
        sendMessage("Se conecto " + username);
    }

    public void sendMessage(String message) {
        try {
            System.out.println("Enviando mensaje: " + message);
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, serverPort);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendPrivateMessage(String message, String recipient) {
        sendMessage("PRIVATE " + recipient + " " + message);
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
        sendMessage("DISCONNECT");
    }

    public static void main(String[] args) throws SocketException, UnknownHostException {
        UDPClient client = new UDPClient("jazael", "20.96.185.240", 12345);
        client.receiveMessages();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            client.disconnect();
        }));
    }
}
