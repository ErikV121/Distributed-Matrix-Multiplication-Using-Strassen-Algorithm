package V2.client.primary;

import V2.StrassenAlgorithmUtil;
import V2.client.ConnectionHandler;
import V2.util.JobScheduler;
import V2.util.Opcode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;

public class PrimaryProtocol implements ConnectionHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ArrayList<long[][]> matrices;
    private int matrixSize;
    private int matrixCount;

    public PrimaryProtocol(ArrayList<long[][]> matrices) {
        this.matrices = matrices;
    }

    public PrimaryProtocol(ArrayList<long[][]> matrices, int matrixSize, int matrixCount) {
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
        LOGGER.info("Selected strategy: {}", strategy.getCode());


        dataWriter.writeInt(matrices.size());

        for (int i = 0; i < matrices.size(); i++) {
            long[][] matrix = matrices.get(i);
            int rows = matrix.length;
            int cols = (rows > 0) ? matrix[0].length : 0;

            dataWriter.writeInt(rows);
            dataWriter.writeInt(cols);

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    dataWriter.writeLong(matrix[r][c]);
                }
            }
        }
        dataWriter.flush();
    }

    public void receiveServerInfo(DataInputStream dataReader) {
        try {
            while (true) {
                int serverMessage = dataReader.readInt();

                if (serverMessage == 99) {
                    LOGGER.info("Receiving final matrix from server...");
                    int rows = dataReader.readInt();
                    int cols = dataReader.readInt();
                    long[][] finalMatrix = new long[rows][cols];

                    for(int r = 0; r < rows; r++) {
                        for(int c = 0; c < cols; c++) {
                            finalMatrix[r][c] = dataReader.readLong();
                        }
                    }

                    LOGGER.info("Final matrix received from server.");
                    StrassenAlgorithmUtil.printMatrix(finalMatrix);

                    return;
                } else {
                    System.out.println("Server says: " + serverMessage);

                }
            }
        } catch (IOException e) {
            LOGGER.error("Server closed the connection or finished.");
        }
    }
}
