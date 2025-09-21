// V2/client/ClientConnection.java
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

    public void connect(String ipAddress, int port, ConnectionHandler handler) {
        try (Socket socket = new Socket(ipAddress, port);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            System.out.println("Client Socket created successfully");

            // handshake: announce role (PRIMARY / SECONDARY)
            out.writeUTF(clientType);
            out.flush();

            // delegate to protocol implementation
            handler.handle(out, in);

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
