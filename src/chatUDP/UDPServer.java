package chatUDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class UDPServer {

    private DatagramSocket socket;
    private Map<String, InetAddress> clients = new HashMap<>();
    private Map<String, String> privateConnections = new HashMap<>();
    private static final String _IP = "192.168.0.102";

    public UDPServer(int port) throws SocketException, UnknownHostException {
        InetAddress ip = InetAddress.getByName(_IP);
        socket = new DatagramSocket(port, ip);
    }

    private void handleConnection(String username, InetAddress clientAddress, int clientPort) throws IOException {
        String clientKey = username + ":" + clientPort;
        clients.put(clientKey, clientAddress);

        System.out.println("Cliente conectado: " + clientKey);

        // Enviar notificación a todos los clientes
        broadcast("Cliente " + username + " se ha conectado");
    }

    private void handleDisconnection(String username, InetAddress clientAddress, int clientPort) throws IOException {
        String clientKey = username + ":" + clientPort;
        clients.remove(clientKey);

        System.out.println("Cliente desconectado: " + clientKey);

        // Enviar notificación a todos los clientes
        broadcast("Cliente " + username + " se ha desconectado");
    }

    public void receiveData() throws IOException {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);

        String received = new String(packet.getData(), 0, packet.getLength());
        // Procesar los datos recibidos...
    }

    public void sendData(String message, InetAddress address, int port) throws IOException {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        socket.send(packet);
    }


    public void processData(DatagramPacket packet) throws IOException {
        String username = "";
        String message = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Mensaje recibido de "+ username + ": " + message);
        InetAddress clientAddress = packet.getAddress();
        int clientPort = packet.getPort();

        String[] parts = message.split(" ", 2);
        String command = parts[0];


        if (command.equals("CONNECT")) {
            username = parts[1];
            handleConnection(username, clientAddress, clientPort);
        } else if (command.equals("DISCONNECT")) {
            username = parts[1];
            handleDisconnection(username, clientAddress, clientPort);
        } else if (command.equals("PRIVATE")) {
            String[] privateParts = parts[1].split(" ", 2);
            String recipient = privateParts[0];
            String privateMessage = privateParts[1];
            sendPrivateMessage(privateMessage, recipient);
        } else if (command.startsWith("@")) {  // Identificador para mensajes privados
            String recipient = command.substring(1);
            String privateMessage = parts[1];
            sendPrivateMessage(privateMessage, recipient);
        } else {
            // Retransmitir el mensaje a todos los clientes, incluyendo el nombre del remitente
            broadcast(username + ": " + message);
        }
    }


    public void broadcast(String message) throws IOException {
        for (String clientKey : clients.keySet()) {
            InetAddress clientAddress = clients.get(clientKey);

            int clientPort = Integer.parseInt(clientKey.split(":")[1]);
            sendData(message, clientAddress, clientPort);
        }
    }


    public void sendPrivateMessage(String message, String recipientUsername) throws IOException {
        String recipientKey = findClientKeyByUsername(recipientUsername);

        if (recipientKey != null) {
            InetAddress recipientAddress = clients.get(recipientKey);
            int recipientPort = Integer.parseInt(recipientKey.split(":")[1]);
            sendData("PRIVATE " + recipientUsername + ": " + message, recipientAddress, recipientPort);
        }
    }

    private String findClientKeyByUsername(String username) {
        for (String clientKey : clients.keySet()) {
            if (clientKey.startsWith(username)) {
                return clientKey;
            }
        }
        return null;
    }

    public void listen() {
        System.out.println("Servidor iniciado. Escuchando en el puerto: " + socket.getLocalPort());

        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                socket.receive(packet);

                // Procesar el paquete recibido
                processData(packet);
            } catch (IOException e) {
                System.err.println("Error en la conexión: " + e.getMessage());
                // Manejar adecuadamente la excepción
            }
        }
    }

    public void disconnectClient(String username) {
        for (String clientKey : clients.keySet()) {
            if (clientKey.startsWith(username)) {
                InetAddress clientAddress = clients.get(clientKey);
                int clientPort = Integer.parseInt(clientKey.split(":")[1]);
                try {
                    sendData("DISCONNECT", clientAddress, clientPort);
                    handleDisconnection(username, clientAddress, clientPort);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws UnknownHostException {
        int port = 12345;
        try {
            UDPServer server = new UDPServer(port);
            server.listen();
        } catch (SocketException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }
}