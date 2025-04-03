package V2;

import V2.util.StrassenAlgorithmUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Executors;

public class ServerRouter {
    private ArrayList<Socket> secondaryClientSockets = new ArrayList<>();
    private ArrayList<int[][]> matrices = new ArrayList<>();

    private boolean run = true;

    public ServerRouter(int port) {
        start(port);
    }

    public void start(final int port) {
        System.out.println("Server Router will be begin listening on port: " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                while (run) {
                    Socket clientSocket = serverSocket.accept();

                    // creates a new v thread for each client
                    executor.submit(() -> {
                        handleInputStream(clientSocket);


                    });
                }
            }
        } catch (IOException e) {
            System.out.println("Server socket error: " + e.getMessage());
        }
    }

    public void receiveMatrices(Socket socket, DataInputStream dataReader, ArrayList<int[][]> matrices) throws IOException {
        int matrixCount = dataReader.readInt();

        for (int i = 0; i < matrixCount; i++) {
            int rows = dataReader.readInt();
            int cols = dataReader.readInt();
            int[][] matrix = new int[rows][cols];

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    matrix[r][c] = dataReader.readInt();
                }
            }
            matrices.add(matrix);
        }
        System.out.println("Received " + matrixCount + " matrices from client: " + socket.getInetAddress().getHostAddress());

    }

    public void handleInputStream(Socket clientSocket) {
        try (DataInputStream dataReader = new DataInputStream(clientSocket.getInputStream())) {

            String clientAddress = clientSocket.getInetAddress().getHostAddress();
            String clientPort = String.valueOf(clientSocket.getPort());

            System.out.println("client: " + clientAddress + ":" + clientPort + " connected\n");

            //checks first and only the first line of the input stream for client type
            checkClientType(clientSocket, dataReader);
            receiveMatrices(clientSocket, dataReader, matrices);

            for (int i = 0; i < matrices.size(); i++) {
                System.out.println("Matrix " + (i + 1) + ":");
                StrassenAlgorithmUtil.printMatrix(matrices.get(i));
            }

//          TODO returnInfo() //returns information to Primary Client regarding available secondary clients and more...


        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    //    TODO prevent multiple primary clients from connecting
    public void checkClientType(Socket clientSocket, DataInputStream dataReader) throws IOException {
        String clientMessage = dataReader.readUTF();
        if (clientMessage.equals("PRIMARY")) {
            System.out.println("Primary client connected\n");

        } else if (clientMessage.equals("SECONDARY")) {
            System.out.println("Secondary client connected\n");
            secondaryClientSockets.add(clientSocket);
            System.out.println("Secondary client added to list: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());

        } else {
            System.out.println("Unknown client type: " + clientMessage);
        }
    }

}
