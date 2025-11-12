package V2.client.secondary;

import V2.util.Opcode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SecondaryClientConnection {
    private SocketChannel socketChannel;

    public void connect(String ipAddress, int port) {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(ipAddress, port));

            while (!socketChannel.finishConnect()) {
                Thread.onSpinWait();
            }

            System.out.println("done connecting Secondary Client socket to " + ipAddress + ":" + port);

            ByteBuffer buffer = ByteBuffer.allocate(1);
            buffer.put((byte) Opcode.SECONDARY.getCode());
            buffer.flip();
            while (buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }

//            TODO implement how secondary client handles messages from server router
            handleMessages();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
