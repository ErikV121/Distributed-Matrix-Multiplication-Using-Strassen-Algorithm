package V2.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientConnection {
    private final String clientType;

    public ClientConnection(String clientType) {
        this.clientType = clientType;
    }

    public void connect(String ipAddress, int port, ClientDataHandler dataHandler) {
        try (Socket socket = new Socket(ipAddress, port);
             DataOutputStream dataWriter = new DataOutputStream(socket.getOutputStream());
             DataInputStream dataReader = new DataInputStream(socket.getInputStream())) {

            System.out.println("Client Socket created successfully");

            dataWriter.writeUTF(clientType);

            dataHandler.handleData(dataWriter, dataReader);

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

    }
}
