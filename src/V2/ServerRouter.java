package V2;

import V2.util.Opcode;
import V2.util.ProcedureFlag;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.Executors;

public class ServerRouter {
    private final Set<SocketChannel> secondaryClients = new HashSet<>();
    private final ArrayList<int[][]> matrices = new ArrayList<>();
    private boolean run = true;
    private Opcode strategyType;

    public ServerRouter() {
    }

    public void startServer1(final int port) {
        System.out.println("Server Router will be begin listening on port: " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                while (run) {
                    Socket clientSocket = serverSocket.accept();
                    clientSocket.setKeepAlive(true);
                    clientSocket.setTcpNoDelay(true);

                    System.out.println("socket alive: " + clientSocket.getKeepAlive());
                    System.out.println("socket no delay: " + clientSocket.getTcpNoDelay());

                    // creates a new v thread for each P client
//                    TODO currently only supports 1 P client at a time
                    executor.submit(() -> {
                        handleInputStream(clientSocket, EnumSet.of(
                                ProcedureFlag.CHECK_CLIENT_TYPE,
                                ProcedureFlag.RECEIVE_STRATEGY_TYPE,
                                ProcedureFlag.PRINT_CLIENT_INFO,
                                ProcedureFlag.RECEIVE_MATRICES,
                                ProcedureFlag.PRINT_MATRICES,
                                ProcedureFlag.TEST_SERVER_TO_CLIENT));
                    });
                }
            }
        } catch (IOException e) {
            System.out.println("ServerRouter socket error: " + e.getMessage());
        }
    }


    public void startServer2(int port) {

        System.out.println("Starting NIO server on port " + port);
//        Set<SocketChannel> clients = new HashSet<>();

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

                            // Check client type using NIO
                            checkClientType(client);

                            // Register for future read operations
                            client.register(selector, SelectionKey.OP_READ, clientInfo);
                        }
                    }
                }
                selector.selectedKeys().clear();
            }

        } catch (IOException e) {
            System.out.println("Error starting NIO server: " + e.getMessage());
        } finally {
            for (SocketChannel client : secondaryClients) {
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

//    so the order of the if statements matter here
    public void handleInputStream(Socket clientSocket, EnumSet<ProcedureFlag> procedureFlags) {
        try (DataInputStream dataReader = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream dataWriter = new DataOutputStream(clientSocket.getOutputStream());) {

            if (procedureFlags.contains(ProcedureFlag.CHECK_CLIENT_TYPE)) {
                checkClientType(clientSocket, dataReader);
            }
            if (procedureFlags.contains(ProcedureFlag.RECEIVE_STRATEGY_TYPE)) {
                receiveStrategyType(dataReader);
            }
            if (procedureFlags.contains(ProcedureFlag.PRINT_CLIENT_INFO)) {
                String clientAddress = clientSocket.getInetAddress().getHostAddress();
                String clientPort = String.valueOf(clientSocket.getPort());
                System.out.println("client: " + clientAddress + ":" + clientPort + " connected\n");
            }
            if (procedureFlags.contains(ProcedureFlag.RECEIVE_MATRICES)) {
                receiveMatrices(clientSocket, dataReader, matrices);
            }
            if (procedureFlags.contains(ProcedureFlag.PRINT_MATRICES)) {
                printMatrices(matrices);
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error in ServerRouter: " + e.getMessage());
        }
    }


    // For blocking I/O p clients
    // socket is for future plans to allow multiple p clients
    public void checkClientType(Socket socket, DataInputStream dataReader) throws IOException {
        byte clientMessage = dataReader.readByte();
        if (clientMessage == 4) {
            System.out.println("Primary client connected\n");
        } else if (clientMessage == 5) {
            System.out.println("Secondary client connected\n");
        } else {
            System.out.println("ERROR Unknown client type: " + clientMessage);
        }
    }

    // For NIO socket clients
    public void checkClientType(SocketChannel socketChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        socketChannel.read(buffer);
        buffer.flip();
        byte clientMessage = buffer.get();

        if (clientMessage == 1) {
            System.out.println("Primary client connected\n");
        } else if (clientMessage == 2) {
            System.out.println("Secondary client connected\n");
            secondaryClients.add(socketChannel);
            System.out.println("Secondary client added to list: " + socketChannel.getLocalAddress().toString());

        } else {
            System.out.println("ERROR Unknown client type: " + clientMessage);
        }
    }

    private static void printMatrices(List<int[][]> matrices) {
        for (int i = 0; i < matrices.size(); i++) {
            System.out.println("Matrix " + (i + 1) + ":");
            StrassenAlgorithmUtil.printMatrix(matrices.get(i));
        }
    }

    private void receiveStrategyType(DataInputStream dataReader) throws IOException, InterruptedException {
        int strategy = dataReader.readInt();
        switch (strategy) {
            case 1 -> {
                strategyType = Opcode.SINGLE_WORKER;
                System.out.println("Strategy selected: SINGLE WORKER");
            }
            case 2 -> {
                strategyType = Opcode.JOB_LEADER;
                System.out.println("Strategy selected: JOB LEADER");
            }
            case 3 -> {
                strategyType = Opcode.IDK_WHAT_TO_CALL_THIS;
                System.out.println("Strategy selected: IDK WHAT TO CALL THIS");
            }
            default -> {
                System.out.println("ERROR: Unknown strategy type: " + strategy);
                throw new IllegalArgumentException("Unknown strategy type: " + strategy);
            }
        }
    }
}

