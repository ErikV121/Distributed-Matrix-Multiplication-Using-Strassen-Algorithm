package V2.client.secondary;

import V2.MatrixPair;
import V2.MatrixSerializer;
import V2.StrassenAlgorithmUtil;
import V2.message.Message;
import V2.util.Opcode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SecondaryClientConnection {

    private static final Logger LOGGER = LogManager.getLogger();

    private SocketChannel socketChannel;

public void connect(String ipAddress, int port) {
    try {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(ipAddress, port));

        while (!socketChannel.finishConnect()) {
            Thread.onSpinWait();
        }

        LOGGER.info("Connected to server at {}:{}", ipAddress, port);

        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) Opcode.SECONDARY.getCode());
        buffer.flip();
        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }
        socketChannel.configureBlocking(true);

        handleMessages();

    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}
    public void handleMessages() {
        ByteBuffer header = ByteBuffer.allocate(9);

        while (socketChannel.isOpen()) {
            try {
                header.clear();

                while (header.hasRemaining()) {
                    int read = socketChannel.read(header);
                    if (read == -1) {
                        LOGGER.info("Server closed the connection.");
                        return;
                    }
                }
                header.flip();

                byte opcodeVal = header.get();
                int workId = header.getInt();
                int payloadLen = header.getInt();

                ByteBuffer payloadBuffer = ByteBuffer.allocate(payloadLen);
                while (payloadBuffer.hasRemaining()) {
                    int read = socketChannel.read(payloadBuffer);
                    if (read == -1) return;
                }
                payloadBuffer.flip();

                LOGGER.info("Received Work ID: {}", workId);
                MatrixPair pair = MatrixSerializer.deserializePair(payloadBuffer.array());
                long[][] result = StrassenAlgorithmUtil.strassenMultiply(pair.matrixA(), pair.matrixB());

                byte[] resultBytes = MatrixSerializer.serializeMatrix(result);
//                Very expensive to print big matrices
//                StrassenAlgorithmUtil.printMatrix(result);

                Message response = new Message(Opcode.RESULT, workId, resultBytes);
                byte[] responseData = response.toBytes();

                ByteBuffer responseBuffer = ByteBuffer.wrap(responseData);
                while (responseBuffer.hasRemaining()) {
                    socketChannel.write(responseBuffer);
                }
                LOGGER.info("Sent Result for Work ID: {}", workId);

            } catch (IOException e) {
                LOGGER.error("Worker Error: ", e);
                return;
            }
        }
    }
}
