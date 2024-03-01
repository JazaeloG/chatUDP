package chatUDP;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UDPServer {

    private DatagramSocket socket;
    private HashMap<String, InetAddress> clients = new HashMap<>();
    private ArrayList<String> users = new ArrayList<>();
    private ArrayList<String> ipPort = new ArrayList<>();
    private ArrayList<InetAddress> ipAddress = new ArrayList<>();
    private static final String _IP = "192.168.137.226"; //Cambie por su IP

    public UDPServer(int port) throws SocketException, UnknownHostException {
        InetAddress ip = InetAddress.getByName(_IP);
        socket = new DatagramSocket(port, ip);
    }

    private void handleConnection(InetAddress clientAddress, int clientPort, String username) throws IOException {
        String clientKey = clientAddress.getHostAddress() + ":" + clientPort;
        clients.put(clientKey, clientAddress);
        users.add(username);
        ipPort.add(clientKey);
        ipAddress.add(clientAddress);





        System.out.println("Cliente conectado: " + clientKey);

        // Enviar notificación a todos los clientes
        broadcast("Cliente " + clientKey + " se ha conectado");
    }

    private void handleDisconnection(InetAddress clientAddress, int clientPort) throws IOException {
        String clientKey = clientAddress.getHostAddress() + ":" + clientPort;
        int index = ipAddress.indexOf(clientAddress);
        clients.remove(clientKey);
        users.remove(index);
        ipPort.remove(index);
        ipAddress.remove(index);

        System.out.println("Cliente desconectado: " + clientKey);

        // Enviar notificación a todos los clientes
        broadcast("Cliente " + clientKey + " se ha desconectado");
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
        String recibe = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Mensaje recibido: " + recibe);
        InetAddress clientAddress = packet.getAddress();
        int clientPort = packet.getPort();

        String[] lstEntradaServidor = recibe.split(",");
        String message = String.valueOf(lstEntradaServidor[0]);

        String username = String.valueOf(lstEntradaServidor[1]);
        System.out.println("username de ");
        System.out.printf(username);
        System.out.println(message);

        String clientKey = clientAddress.getHostAddress() + ":" + clientPort;

        if (message.startsWith("CONNECT")) {
            handleConnection(clientAddress, clientPort, username);
        } else if (message.startsWith("DISCONNECT")) {
            handleDisconnection(clientAddress, clientPort);
        } else if(message.startsWith("PRIVADO")) {
            String recipientKey = message.split(" ")[1];
            String mensaje = message.split(recipientKey)[1];
            String send = username +": "+mensaje;
            sendPrivateMessage(send ,recipientKey);
        }else {
            // Retransmitir el mensaje a todos los clientes, incluyendo la IP del remitente
            broadcast(clientKey + ": " + message);
        }
    }

    public void broadcast(String message) throws IOException {
        for (String clientKey : clients.keySet()) {
            InetAddress clientAddress = clients.get(clientKey);

            int clientPort = Integer.parseInt(clientKey.split(":")[1]);
            sendData(message, clientAddress, clientPort);
        }
    }


    public void sendPrivateMessage(String message  , String  recipientKey) throws IOException {
        System.out.println(recipientKey);
        System.out.println(message);
        int index = users.indexOf(recipientKey);
        String privateIp = ipPort.get(index);
        System.out.println("nombre");
        System.out.println(recipientKey);
        InetAddress recipientAddress = ipAddress.get(index);

        System.out.println("ip");
        System.out.println(recipientAddress);
        if (recipientAddress != null) {
            int recipientPort = Integer.parseInt(privateIp.split(":")[1]);
            sendData(message, recipientAddress, recipientPort);
        }
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

    public static void main(String[] args) throws UnknownHostException {
        int port = 12345; // Puedes cambiar esto al puerto que desees
        try {
            UDPServer server = new UDPServer(port);
            server.listen();
        } catch (SocketException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }
}