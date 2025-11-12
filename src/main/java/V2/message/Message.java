package V2.message;

import V2.util.Opcode;

public class Message {


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
    private byte[] matrixPayload;

    public Message(Opcode strategyType, byte[] matrixPayload) {
        this.strategyType = strategyType;
        this.matrixPayload = matrixPayload;
    }


//    TODO add methods for serialization and deserialization
//     a simple toBytes and fromBytes would do for now

    public Opcode getStrategyType() {
        return strategyType;
    }

    public void setStrategyType(Opcode strategyType) {
        this.strategyType = strategyType;
    }

    public byte[] getMatrixPayload() {
        return matrixPayload;
    }

    public void setMatrixPayload(byte[] matrixPayload) {
        this.matrixPayload = matrixPayload;
    }
}
