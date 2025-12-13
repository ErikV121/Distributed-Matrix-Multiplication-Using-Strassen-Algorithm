package V2;

import java.util.ArrayList;
import java.util.Random;

public class StrassenAlgorithmUtil {
//  https://en.wikipedia.org/wiki/Strassen_algorithm?utm_source=chatgpt.com#Implementation_considerations
//    adds padding for odd sized matrices to make them even sized
    public static long[][] strassenMultiply(long[][] A, long[][] B) {
        int n = A.length;

        if ((n & (n - 1)) == 0) {
            return strassenRecursive(A, B);
        }

        int m = 1;
        while (m < n) {
            m = m * 2;
        }

        long[][] Ap = new long[m][m];
        long[][] Bp = new long[m][m];

        for (int i = 0; i < n; i++) {
            System.arraycopy(A[i], 0, Ap[i], 0, n);
            System.arraycopy(B[i], 0, Bp[i], 0, n);
        }

        long[][] Cp = strassenRecursive(Ap, Bp);

        long[][] C = new long[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(Cp[i], 0, C[i], 0, n);
        }

        return C;
    }


    private static long[][] strassenRecursive(long[][] A, long[][] B) {
        int n = A.length;
        long[][] result;

        // Base case for recursion: 1x1 matrix
        if (n == 1) {
            result = new long[1][1];
            result[0][0] = A[0][0] * B[0][0];
            return result;
        }

        // Split matrices into quadrants
        int newSize = n / 2;
        long[][] a11 = new long[newSize][newSize];
        long[][] a12 = new long[newSize][newSize];
        long[][] a21 = new long[newSize][newSize];
        long[][] a22 = new long[newSize][newSize];

        long[][] b11 = new long[newSize][newSize];
        long[][] b12 = new long[newSize][newSize];
        long[][] b21 = new long[newSize][newSize];
        long[][] b22 = new long[newSize][newSize];

        // Dividing the matrices into quadrants
        splitMatrix(A, a11, 0, 0);
        splitMatrix(A, a12, 0, newSize);
        splitMatrix(A, a21, newSize, 0);
        splitMatrix(A, a22, newSize, newSize);

        splitMatrix(B, b11, 0, 0);
        splitMatrix(B, b12, 0, newSize);
        splitMatrix(B, b21, newSize, 0);
        splitMatrix(B, b22, newSize, newSize);

        // Apply Strassen's formulae to compute intermediate matrices
        // Note: Calls strassenRecursive directly
        long[][] p1 = strassenRecursive(addMatrices(a11, a22), addMatrices(b11, b22));
        long[][] p2 = strassenRecursive(addMatrices(a21, a22), b11);
        long[][] p3 = strassenRecursive(a11, subtractMatrices(b12, b22));
        long[][] p4 = strassenRecursive(a22, subtractMatrices(b21, b11));
        long[][] p5 = strassenRecursive(addMatrices(a11, a12), b22);
        long[][] p6 = strassenRecursive(subtractMatrices(a21, a11), addMatrices(b11, b12));
        long[][] p7 = strassenRecursive(subtractMatrices(a12, a22), addMatrices(b21, b22));

        // Compute the final quadrants of the result matrix
        long[][] c11 = addMatrices(subtractMatrices(addMatrices(p1, p4), p5), p7);
        long[][] c12 = addMatrices(p3, p5);
        long[][] c21 = addMatrices(p2, p4);
        long[][] c22 = addMatrices(subtractMatrices(addMatrices(p1, p3), p2), p6);

        // Combine quadrants into a single result matrix
        long[][] C = new long[n][n];
        combineMatrices(C, c11, 0, 0);
        combineMatrices(C, c12, 0, newSize);
        combineMatrices(C, c21, newSize, 0);
        combineMatrices(C, c22, newSize, newSize);

        return C;
    }

    private static void splitMatrix(long[][] parent, long[][] child, int iB, int jB) {
        for (int i = 0; i < child.length; i++)
            for (int j = 0; j < child.length; j++)
                child[i][j] = parent[i + iB][j + jB];
    }

    private static void combineMatrices(long[][] parent, long[][] child, int iB, int jB) {
        for (int i = 0; i < child.length; i++)
            for (int j = 0; j < child.length; j++)
                parent[i + iB][j + jB] = child[i][j];
    }

    private static long[][] addMatrices(long[][] A, long[][] B) {
        int n = A.length;
        long[][] result = new long[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                result[i][j] = A[i][j] + B[i][j];
        return result;
    }

    private static long[][] subtractMatrices(long[][] A, long[][] B) {
        int n = A.length;
        long[][] result = new long[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                result[i][j] = A[i][j] - B[i][j];
        return result;
    }

    public static void generateRandomMatrices(ArrayList<long[][]> matrices, int numberOfMatrices, int size) {
        Random random = new Random();
        for (int l = 0; l < numberOfMatrices; l++) {
            long[][] matrix = new long[size][size];
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    matrix[j][k] = random.nextInt(1, 2);
                }
            }
            matrices.add(matrix);
        }
    }

    public static void printMatrix(long[][] matrix) {
        for (long[] row : matrix) {
            for (long value : row) {
                System.out.print(value + " ");
            }
            System.out.println();
        }
    }

    public static void printMatrixList(ArrayList<long[][]> matrices) {
        for (int i = 0; i < matrices.size(); i++) {
            System.out.println("Matrix " + (i + 1) + ":");
            printMatrix(matrices.get(i));
        }
    }
}