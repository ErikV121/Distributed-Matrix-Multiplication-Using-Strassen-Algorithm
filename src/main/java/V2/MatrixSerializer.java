package V2;

import java.io.*;
import java.nio.ByteBuffer;

public class MatrixSerializer {

    public static byte[] serializePair(MatrixPair matrixPair) {
        int rows = matrixPair.matrixA().length;
        int cols = matrixPair.matrixA()[0].length;

        int size = 8 + (rows * cols * 8 * 2);

        ByteBuffer buffer = ByteBuffer.allocate(size);

        buffer.putInt(rows);
        buffer.putInt(cols);

        for (long[] row : matrixPair.matrixA() ) {
            for (long val : row) {
                buffer.putLong(val);
            }
        }
        for (long[] col : matrixPair.matrixB() ) {
            for (long val : col) {
                buffer.putLong(val);
            }
        }

        return buffer.array();
    }


    public static byte[] serializeMatrix(long[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        int size = 8 + (rows * cols * 8);

        ByteBuffer buffer = ByteBuffer.allocate(size);

        buffer.putInt(rows);
        buffer.putInt(cols);

        for (long[] row : matrix ) {
            for (long val : row) {
                buffer.putLong(val);
            }
        }

        return buffer.array();
    }


    public static MatrixPair deserializePair(byte[] pairPayload) {
        ByteBuffer buffer = ByteBuffer.wrap(pairPayload);

        int rows = buffer.getInt();
        int cols = buffer.getInt();

        long[][] matrixA = new long[rows][cols];
        long[][] matrixB = new long[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                matrixA[r][c] = buffer.getLong();
            }
        }
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                matrixB[r][c] = buffer.getLong();
            }
        }

        return new MatrixPair(matrixA, matrixB);
    }



    public static long[][] deserializeMatrix(byte[] matrixPayload) {
        ByteBuffer buffer = ByteBuffer.wrap(matrixPayload);

        int rows = buffer.getInt();
        int cols = buffer.getInt();

        long[][] matrix = new long[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                matrix[r][c] = buffer.getLong();
            }
        }

        return matrix;
    }
}
