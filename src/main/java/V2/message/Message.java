package V2.message;

import V2.util.Opcode;

import java.nio.ByteBuffer;

public class Message {
//    reminder that:
//    ints are 4 bytes / (32 bits / 8 = 4 bytes)
//    bytes are 1 byte/ 8 bits
//    longs are 8 bytes / (64 bits / 8 = 8 bytes)


// TODO example of what i expect the server router to send to secondary client
/**
 Opcode: JOB_LEADER

 Work ID: 123
 Payload length: 64 bytes

 Matrix A (2×2): [[1,2],[3,4]]

 Matrix B (2×2): [[5,6],[7,8]]

 Expected result: [[19,22],[43,50]]

 Total size: 69 bytes
 **/

// TODO example of what secondary clients will send to server router

/**
 Opcode: RESULT

 Payload length: 40 bytes

 Work ID: 123

 Result matrix (2×2): [[19,22],[43,50]]

 Total size: 45 bytes
 **/

    private Opcode strategyType;
    private int workId;
    private byte[] matrixPayload;

    public Message(Opcode strategyType, int workId, byte[] matrixPayload) {
        this.strategyType = strategyType;
        this.workId = workId;
        this.matrixPayload = matrixPayload;
    }

    public byte[] toBytes() {
    // structure
    // 1 byte: opcode
    // 4 bytes: workID
    // 4 bytes: payload length num
    // ? bytes: payload

    int payloadLength = matrixPayload.length;
    int totalSize = 1 + 4 + 4 + payloadLength;

    ByteBuffer buffer = ByteBuffer.allocate(totalSize);

        buffer.put((byte) strategyType.getCode());
        buffer.putInt(workId);
        buffer.putInt(payloadLength);
        buffer.put(matrixPayload);

        return buffer.array();
}

    public static Message fromBytes(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);

        int opcodeInt = buffer.get();
        Opcode op = Opcode.fromCode((byte) opcodeInt);

        int wID = buffer.getInt();
        int length = buffer.getInt();

        byte[] payload = new byte[length];
        buffer.get(payload);

        return new Message(op, wID, payload);
    }

    public Opcode getStrategyType() {
        return strategyType;
    }

    public void setStrategyType(Opcode strategyType) {
        this.strategyType = strategyType;
    }

    public int getWorkId() {
        return workId;
    }

    public void setWorkId(int workId) {
        this.workId = workId;
    }

    public byte[] getMatrixPayload() {
        return matrixPayload;
    }

    public void setMatrixPayload(byte[] matrixPayload) {
        this.matrixPayload = matrixPayload;
    }
}
