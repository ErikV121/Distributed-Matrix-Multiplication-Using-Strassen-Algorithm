package V2.client;

import V2.util.Opcode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientConnection {

    private static final Logger LOGGER = LogManager.getLogger();

    private final int clientType;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public ClientConnection(Opcode clientType) {
        this.clientType = clientType.getCode();
    }

    public void connect(String ipAddress, int port, ConnectionHandler handler) {
        try {
            socket = new Socket(ipAddress, port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            socket.setKeepAlive(true);
            socket.setSoTimeout(10000000);
            socket.setTcpNoDelay(true);

            LOGGER.info("Client Socket created successfully");

            out.writeByte(clientType);
            out.flush();

            handler.handle(out, in);

        } catch (IOException e) {
            LOGGER.error("Connection Error: ", e);
            close();
        }
    }

    public void close() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            LOGGER.info("Connection closed.");
        } catch (IOException e) {
            LOGGER.error("Error while closing connection: {}", e.getMessage());
        }
    }

    public int getClientType() {
        return clientType;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public DataOutputStream getOut() {
        return out;
    }

    public void setOut(DataOutputStream out) {
        this.out = out;
    }

    public DataInputStream getIn() {
        return in;
    }

    public void setIn(DataInputStream in) {
        this.in = in;
    }
}
