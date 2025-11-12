package V2.client.secondary;


public class SecondaryClient {
    private final SecondaryClientConnection connection;

    public SecondaryClient() {
        this.connection = new SecondaryClientConnection();
    }

    public void start(String ip, int port) {
        connection.connect(ip, port);

    }
}