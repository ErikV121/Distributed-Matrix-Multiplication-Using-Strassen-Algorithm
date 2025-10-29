package V2.client;

import V2.util.Opcode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientConnection {
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

            System.out.println("Client Socket created successfully");

            out.writeByte(clientType);
            out.flush();

            handler.handle(out, in);

        } catch (IOException e) {
            System.out.println("Connection Error: " + e.getMessage());
            close();
        }
    }

    public void close() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            System.out.println("Connection closed.");
        } catch (IOException e) {
            System.out.println("Error while closing connection: " + e.getMessage());
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
