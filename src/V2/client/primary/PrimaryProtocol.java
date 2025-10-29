package V2.client.primary;

import V2.client.ConnectionHandler;
import V2.util.JobScheduler;
import V2.util.Opcode;

import java.io.*;
import java.util.ArrayList;

public class PrimaryProtocol implements ConnectionHandler {

    private final ArrayList<int[][]> matrices;
    private int matrixSize;
    private int matrixCount;

    public PrimaryProtocol(ArrayList<int[][]> matrices) {
        this.matrices = matrices;
    }

    public PrimaryProtocol(ArrayList<int[][]> matrices, int matrixSize, int matrixCount) {
        this.matrices = matrices;
        this.matrixSize = matrixSize;
        this.matrixCount = matrixCount;
    }

    @Override
    public void handle(DataOutputStream out, DataInputStream in) throws IOException {
        sendMatrices(out);
        receiveServerInfo(in);
    }

    public void sendMatrices(DataOutputStream dataWriter) throws IOException {
        Opcode strategy = JobScheduler.getStrategy(matrixSize, matrixCount);

        dataWriter.writeInt(strategy.getCode());
        System.out.println("check what was send Second since client type is first: " + strategy.getCode());


        dataWriter.writeInt(matrices.size());

        // For each matrix in the ArrayList
        for (int i = 0; i < matrices.size(); i++) {
            int[][] matrix = matrices.get(i);
            int rows = matrix.length;
            int cols = (rows > 0) ? matrix[0].length : 0;

            dataWriter.writeInt(rows);
            dataWriter.writeInt(cols);

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    dataWriter.writeInt(matrix[r][c]);
                }
            }
        }
        dataWriter.flush();
    }

    public void receiveServerInfo(DataInputStream dataReader) {
        try {
            while (true) {
                int serverMessage = dataReader.readInt();
                System.out.println("Server says: " + serverMessage);
            }
        } catch (IOException e) {
            System.out.println("Server closed the connection.");
        }

    }
}
