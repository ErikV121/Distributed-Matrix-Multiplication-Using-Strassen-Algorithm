package V2.client;

import V2.util.StrassenAlgorithmUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PrimaryProtocol implements ConnectionHandler {

    private final ArrayList<int[][]> matrices;

    public PrimaryProtocol(ArrayList<int[][]> matrices) {
        this.matrices = matrices;
    }

    @Override
    public void handle(DataOutputStream out, DataInputStream in) throws IOException {
        PrimaryClient client = new PrimaryClient(matrices);
//        System.out.println("HANDLER  METHOD CALLED");
//        StrassenAlgorithmUtil.printMatrix(client.matrices.get(0));
        client.sendMatrices(out, in);
        System.out.println("RECIEVE METHOD NEXT");
        client.receiveServerInfo(in);
    }
}
