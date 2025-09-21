package V2.client;

import java.io.*;
import java.util.ArrayList;

public class PrimaryClient {
    public final ArrayList<int[][]> matrices;
    private final ClientConnection connection;

    public PrimaryClient(ArrayList<int[][]> matrices) {
        this.matrices = matrices;
        this.connection = new ClientConnection("PRIMARY");
    }


    public void start(String ip, int port) {
        connection.connect(ip, port, new PrimaryProtocol(matrices)); // writes "PRIMARY" first
    }

    public void sendMatrices(DataOutputStream dataWriter, DataInputStream dataReader) throws IOException {
        dataWriter.writeInt(matrices.size());

        // For each matrix in the ArrayList
        for (int i = 0; i < matrices.size(); i++) {
            int[][] matrix = matrices.get(i);
            int rows = matrix.length;
            int cols = (rows > 0) ? matrix[0].length : 0;

            dataWriter.writeInt(rows);
            dataWriter.writeInt(cols);

            // Send all elements of the matrix
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    dataWriter.writeInt(matrix[r][c]);
                }
            }
        }
        dataWriter.flush();
    }

    public void receiveServerInfo(DataInputStream dataReader)  {
//        TODO the issue here is it read until there is empty input, but we want it to read until the stream is closed
//        for (String input; !(input = dataReader.readUTF()).isEmpty(); ) {
//            System.out.println("Server: " + input);
//        }


            try {
                while (true) {
                    String input = dataReader.readUTF();
                    System.out.println("Server: " + input);

                    if (input.equalsIgnoreCase("Quit")) {
                        System.out.println("quitting stream");
                        break;
                    }
                }

            } catch (IOException e) {
                System.out.println("End of server messages.");
            }


    }
}