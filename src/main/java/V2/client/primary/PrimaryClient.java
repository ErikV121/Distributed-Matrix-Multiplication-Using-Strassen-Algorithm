package V2.client.primary;

import V2.client.ClientConnection;
import V2.util.Opcode;
import java.util.ArrayList;

public class PrimaryClient {
    public final ArrayList<long[][]> matrices;
    private final ClientConnection connection;
    private final int matrixSize;
    private final int matrixCount;

    public PrimaryClient(ArrayList<long[][]> matrices, int size, int amount) {
        this.matrices = matrices;
        this.matrixSize = size;
        this.matrixCount = amount;
        this.connection = new ClientConnection(Opcode.PRIMARY);
    }

    public void start(String ip, int port) {
        PrimaryProtocol p1 = new PrimaryProtocol(matrices, matrixSize, matrixCount);
        connection.connect(ip, port, p1);

    }

}