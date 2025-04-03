package V2.util;

import java.util.ArrayList;
import java.util.Random;

public class StrassenAlgorithmUtil {
    public static int[][] strassenMultiply(int[][] A, int[][] B) {

        int n = A.length;
        int[][] result;
        // Base case for recursion: 1x1 matrix
        if (n == 1) {
            result = new int[1][1];
            result[0][0] = A[0][0] * B[0][0];
            return result;
        }

        // Split matrices into quadrants
        int newSize = n / 2;
        int[][] a11 = new int[newSize][newSize];
        int[][] a12 = new int[newSize][newSize];
        int[][] a21 = new int[newSize][newSize];
        int[][] a22 = new int[newSize][newSize];

        int[][] b11 = new int[newSize][newSize];
        int[][] b12 = new int[newSize][newSize];
        int[][] b21 = new int[newSize][newSize];
        int[][] b22 = new int[newSize][newSize];

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
        int[][] p1 = strassenMultiply(addMatrices(a11, a22), addMatrices(b11, b22));
        int[][] p2 = strassenMultiply(addMatrices(a21, a22), b11);
        int[][] p3 = strassenMultiply(a11, subtractMatrices(b12, b22));
        int[][] p4 = strassenMultiply(a22, subtractMatrices(b21, b11));
        int[][] p5 = strassenMultiply(addMatrices(a11, a12), b22);
        int[][] p6 = strassenMultiply(subtractMatrices(a21, a11), addMatrices(b11, b12));
        int[][] p7 = strassenMultiply(subtractMatrices(a12, a22), addMatrices(b21, b22));

        // Compute the final quadrants of the result matrix
        int[][] c11 = addMatrices(subtractMatrices(addMatrices(p1, p4), p5), p7);
        int[][] c12 = addMatrices(p3, p5);
        int[][] c21 = addMatrices(p2, p4);
        int[][] c22 = addMatrices(subtractMatrices(addMatrices(p1, p3), p2), p6);

        // Combine quadrants into a single result matrix
        int[][] C = new int[n][n];
        combineMatrices(C, c11, 0, 0);
        combineMatrices(C, c12, 0, newSize);
        combineMatrices(C, c21, newSize, 0);
        combineMatrices(C, c22, newSize, newSize);

        return C;
    }

    private static void splitMatrix(int[][] parent, int[][] child, int iB, int jB) {
        for (int i = 0; i < child.length; i++)
            for (int j = 0; j < child.length; j++)
                child[i][j] = parent[i + iB][j + jB];
    }

    private static void combineMatrices(int[][] parent, int[][] child, int iB, int jB) {
        for (int i = 0; i < child.length; i++)
            for (int j = 0; j < child.length; j++)
                parent[i + iB][j + jB] = child[i][j];
    }

    private static int[][] addMatrices(int[][] A, int[][] B) {
        int n = A.length;
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                result[i][j] = A[i][j] + B[i][j];
        return result;
    }

    private static int[][] subtractMatrices(int[][] A, int[][] B) {
        int n = A.length;
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                result[i][j] = A[i][j] - B[i][j];
        return result;
    }

    public static void generateRandomMatrices(ArrayList<int[][]> matrices, int numberOfMatrices, int size) {
        Random random = new Random();
        for (int l = 0; l < numberOfMatrices; l++) {
            int[][] matrix = new int[size][size];
            for (int i = 0; i < numberOfMatrices; i++) {
                for (int j = 0; j < size; j++) {
                    for (int k = 0; k < size; k++) {
                        matrix[j][k] = random.nextInt(10);
                    }
                }
            }
            matrices.add(matrix);
        }
    }

    public static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int value : row) {
                System.out.print(value + " ");
            }
            System.out.println();
        }
    }
}
