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
import java.util.concurrent.ConcurrentHashMap;
import V2.message.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerRouter {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Set<SocketChannel> secondaryClients = new HashSet<>();
    private final ArrayList<long[][]> matrices = new ArrayList<>();
    private boolean run = true;
    private Opcode strategyType;

    private final Map<SocketChannel, Integer> workerState = new ConcurrentHashMap<>();

//  "volatile" makes essentially global variables shared across different threads
    private volatile WorkQueue workQueue;
    private volatile Selector workerSelector;


    public ServerRouter() {
    }

    public void startServer1(final int port) {
        LOGGER.info("Server Router will be listening on port: {}", port);


        try (ServerSocket serverSocket = new ServerSocket(port)) {
                while (run) {
                    Socket clientSocket = serverSocket.accept();
                    clientSocket.setKeepAlive(true);
                    clientSocket.setTcpNoDelay(true);

                    LOGGER.info("client connected: {}KeepAlive: {} TcpNoDelay: {}",
                            clientSocket.getInetAddress().getHostName(), clientSocket.getKeepAlive(), clientSocket.getTcpNoDelay());

//                    TODO currently only supports 1 P client at a time
                        handleInputStream(clientSocket, EnumSet.of(
                                ProcedureFlag.CHECK_CLIENT_TYPE,
                                ProcedureFlag.RECEIVE_STRATEGY_TYPE,
                                ProcedureFlag.PRINT_CLIENT_INFO,
                                ProcedureFlag.RECEIVE_MATRICES,
                                ProcedureFlag.TEST_SERVER_TO_CLIENT));
                }
        } catch (IOException e) {
            LOGGER.error("ServerRouter socket error: {}", e.getMessage());
        }
    }


public void receiveMatrices(Socket socket, DataInputStream dataReader, DataOutputStream dataWriter, ArrayList<long[][]> matrices) throws IOException {
    int matrixCount = dataReader.readInt();

    for (int i = 0; i < matrixCount; i++) {
        int rows = dataReader.readInt();
        int cols = dataReader.readInt();
        long[][] matrix = new long[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                matrix[r][c] = dataReader.readLong();
            }
        }
        matrices.add(matrix);
    }
    LOGGER.info("Matrices received from Primary Client: {}", matrixCount);

    this.workQueue = new WorkQueue(matrices);


    Selector sel = this.workerSelector;

    if (sel != null) {
        sel.wakeup();
    }

    LOGGER.info("Work Queue initialized with {} matrices. \n\nWaiting for workers...", matrices.size());


    try {
        // waiting for final result via CompletableFuture
        long[][] finalResult = this.workQueue.getFinalResultFuture().get();

        System.out.println("ServerRouter: calculation done sending result to primary");

        dataWriter.writeInt(99);
        dataWriter.writeInt(finalResult.length);
        dataWriter.writeInt(finalResult[0].length);

        for (long[] row : finalResult) {
            for (long val : row) {
                dataWriter.writeLong(val);
            }
        }
        dataWriter.flush();
        System.out.println("ServerRouter: Sent final result to Primary Client.");

    } catch (Exception e) {
        System.out.println("Error waiting for results: " + e.getMessage());
    }
}


    public void startServer2(int port) {
        System.out.println("Starting NIO server on port " + port);

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
             Selector selector = Selector.open();) {

            this.workerSelector = selector;


            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                selector.select();



                if (workQueue != null && workQueue.hasWork()) {
                    for (SocketChannel worker : secondaryClients) {
                        Integer currentJob = workerState.get(worker);
                        if (currentJob == null || currentJob == -1) {
                            try {
                                assignJob(worker);
                            } catch (IOException e) {
                                LOGGER.error("Failed to assign job: {}", e.getMessage());
                            }
                        }
                    }
                }

                for (SelectionKey key : selector.selectedKeys()) {
                    if (key.isAcceptable()) {
                        if (key.channel() instanceof ServerSocketChannel channel) {
                            SocketChannel client = channel.accept();
                            Socket socket = client.socket();
                            String clientInfo = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
                            LOGGER.info("Client connected: {}", clientInfo);

                            // Switch to blocking to read the first byte for client type
                            client.configureBlocking(true);
                            checkClientType(client);

                            // Switch non blocking for Selector
                            client.configureBlocking(false);
                            client.register(selector, SelectionKey.OP_READ, clientInfo);
                        }
                    } else if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
//                        changed from 4096 to 12288 to try and fit larger matrices
//                        testing 100kb
                        ByteBuffer buffer = ByteBuffer.allocate(10240000);

                        int bytesRead = -1;
                        try {
                            bytesRead = client.read(buffer);
                        } catch (IOException e) {
                            bytesRead = -1;
                        }

                        if (bytesRead == -1) {
                            LOGGER.info("Client disconnected: {}", key.attachment());

                            Integer failedJobId = workerState.remove(client);
                            if (failedJobId != null && failedJobId != -1) {
                                System.err.println("WARNING: Worker died while processing Job ID " + failedJobId);
                            }

                            secondaryClients.remove(client);
                            client.close();
                            continue;
                        }

                        buffer.flip();
                        handleWorkerResult(client, buffer);
                    }
                }
                selector.selectedKeys().clear();
            }

        } catch (IOException e) {
            LOGGER.error("Error starting NIO server: {}", e.getMessage());
        } finally {
            for (SocketChannel client : secondaryClients) {
                try {
                    client.close();
                } catch (IOException e) {
                    LOGGER.error("Error closing client socket: {}", e.getMessage());
                }
            }
        }
    }


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
                receiveMatrices(clientSocket, dataReader, dataWriter, matrices);
            }
            if (procedureFlags.contains(ProcedureFlag.PRINT_MATRICES)) {
                printMatrices(matrices);
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error in ServerRouter: " + e.getMessage());
        }
    }


    // socket is for future plans to allow multiple p clients
    public void checkClientType(Socket socket, DataInputStream dataReader) throws IOException {
        byte clientMessage = dataReader.readByte();
        if (clientMessage == 4) {
            System.out.println("Primary client connected\n");
        } else if (clientMessage == 5) {
            System.out.println("Secondary client connected\n");
        } else {
            throw new IOException("ERROR Unknown client type: " + clientMessage);
        }
    }

    // For NIO socket clients
    public void checkClientType(SocketChannel socketChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        socketChannel.read(buffer);
        buffer.flip();
        byte clientMessage = buffer.get();

        if (clientMessage == 4) {
            System.out.println("Primary client connected\n");
//        } else if (clientMessage == 2) {
//            System.out.println("Secondary client connected\n");
//            secondaryClients.add(socketChannel);
//            System.out.println("Secondary client added to list: " + socketChannel.getLocalAddress().toString());
//
//        }

            // Inside checkClientType (NIO version)
        } else if (clientMessage == 5) {
            System.out.println("Secondary client connected");
            secondaryClients.add(socketChannel);
        }
            else {
            System.out.println("ERROR Unknown client type: " + clientMessage);
        }
    }

    public static void printMatrices(List<long[][]> matrices) {
        for (int i = 0; i < matrices.size(); i++) {
            System.out.println("Matrix " + (i + 1) + ":");
            StrassenAlgorithmUtil.printMatrix(matrices.get(i));
        }
    }

    public void receiveStrategyType(DataInputStream dataReader) throws IOException, InterruptedException {
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

    public void assignJob(SocketChannel worker) throws IOException {
        if (workQueue == null || !workQueue.hasWork()) {
            return;
        }

        MatrixPair job = workQueue.poll();
        int workId = Math.abs(UUID.randomUUID().hashCode());

        workerState.put(worker, workId);

        byte[] payload = MatrixSerializer.serializePair(job);

        Message msg = new Message(Opcode.JOB_LEADER, workId, payload);
        byte[] data = msg.toBytes();

        ByteBuffer buffer = ByteBuffer.wrap(data);
        while (buffer.hasRemaining()) {
            worker.write(buffer);
        }
        System.out.println("Assigned Job " + workId + " to " + worker.getRemoteAddress());
    }

    public void handleWorkerResult(SocketChannel client, ByteBuffer buffer) throws IOException {
        // Parse Header 9 bytes: 1 Opcode + 4 int ID + 4 int Length
        if (buffer.remaining() < 9){
            return;
        }

        byte opcodeByte = buffer.get();
        Opcode op = Opcode.fromCode(opcodeByte);

        if (op == null) {
            LOGGER.info("Unknown Opcode received: {}", opcodeByte);
            return;
        }


        int workId = buffer.getInt();
        int payloadLen = buffer.getInt();

        byte[] payload = new byte[payloadLen];
        if (buffer.remaining() >= payloadLen) {
            buffer.get(payload);
        } else {
            System.err.println("Error: Buffer too small for matrix!");
            return;
        }
        if (op == Opcode.RESULT) {
            long[][] resultMatrix = MatrixSerializer.deserializeMatrix(payload);

            if (workQueue != null) {
                workQueue.addResult(workId, resultMatrix);
            }

            LOGGER.info("Worker {} finished Job {}", client.getRemoteAddress(), workId);

            workerState.put(client, -1);
            assignJob(client);
        }
    }
}

