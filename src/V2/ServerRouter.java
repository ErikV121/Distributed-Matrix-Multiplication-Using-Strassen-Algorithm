package V2;

import V2.util.StrassenAlgorithmUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;

public class ServerRouter {
    private ArrayList<Socket> secondaryClientSockets = new ArrayList<>();
    private ArrayList<int[][]> matrices = new ArrayList<>();

    private boolean run = true;

    public ServerRouter() {
    }

    public void startServer1(final int port) {
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
            System.out.println("ServerRouter socket error: " + e.getMessage());
        }
    }


    public void startServer2(int port) {
        //        TODO create a non blocking server  which can handle multiple clients
//         what going to happen is primary client will send matrices to server
//
//        TODO server2 will already start and have clients connected
//         using virtual threads im assuming
//         begin strassen algorithm
//         consider base case : 2 matrices,
//         now real 2 4x4,
        System.out.println("Starting NIO server on port " + port);
        Set<SocketChannel> clients = new HashSet<SocketChannel>();

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
             Selector selector = Selector.open();) {

            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                if (selector.select() == 0) {
                    continue;
                }

                for (SelectionKey key : selector.selectedKeys()) {
                    if (key.isAcceptable()) {
                        if (key.channel() instanceof ServerSocketChannel channel) {
                            SocketChannel client = channel.accept();
                            Socket socket = client.socket();
                            String clientInfo = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
                            System.out.println("Client connected: " + clientInfo);

                            client.configureBlocking(false);
                            client.register(selector, SelectionKey.OP_READ, clientInfo);
                            clients.add(client);
                        }
                    }
                }
                selector.selectedKeys().clear();
            }

        } catch (IOException e) {
            System.out.println("Error starting NIO server: " + e.getMessage());
        } finally {
            for (SocketChannel client : clients) {
                try {
                    client.close();
                } catch (IOException e) {
                    System.out.println("Error closing client socket: " + e.getMessage());
                }
            }
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
            System.out.println("ServerRouter Error: " + e.getMessage());
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
