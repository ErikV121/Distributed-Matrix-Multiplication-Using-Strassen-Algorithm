
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.*;

public class MyServer {
    
    ServerSocket ss;
    Socket s; 
    ObjectInputStream ois; 
    ObjectOutputStream oos; 

    public MyServer() { 
        try {
            System.out.println("Server Started"); 
            ss = new ServerSocket(50); 
            s = ss.accept(); 
            System.out.println(s); 
            System.out.println("CLIENT CONNECTED");  
            ois = new ObjectInputStream(s.getInputStream());
            oos = new ObjectOutputStream(s.getOutputStream()); 
            ServerChat(); 
        } catch(Exception e) { 
            System.out.println(e);
        }
    }
    
    public static void main (String as[]) { 
        new MyServer(); 
    } 
    
    public void ServerChat() throws IOException { 
        ArrayList<int[][]> receivedList;
        ArrayList<int[][]> processedList;
        boolean info = true;
        do{
            try {

                receivedList = (ArrayList<int[][]>) ois.readObject();
                if(receivedList.size() == 0){
                    info = false;
                    break;
                }
                System.out.println("Received ArrayList");
                
                // Process the ArrayList (example: convert to uppercase)
                processedList = processArrayList(receivedList);
                
                // Send back the processed ArrayList
                oos.writeObject(processedList);
                oos.flush();
                System.out.println("Sent processed ArrayList");
            } catch (ClassNotFoundException e) {
                System.out.println("Class not found: " + e);
            }  
        }while(info);
        
    }
    
    // Recursive method to process the ArrayList using threads
    private ArrayList<int[][]> processArrayList(ArrayList<int[][]> list) {
        ArrayList<int[][]> newList = new ArrayList<>();
        if (list.size() <= 2) {
            newList.add(strassenMultiply(list.get(0), list.get(1))); // Base case: process directly if small enough
            return newList;
        } else {
            // Split the list into two halves
            int mid = list.size() / 2;
            ArrayList<int[][]> firstHalf = new ArrayList<>(list.subList(0, mid));
            ArrayList<int[][]> secondHalf = new ArrayList<>(list.subList(mid, list.size()));
            
            // Create a thread pool
            ExecutorService executor = Executors.newFixedThreadPool(2);
            Future<ArrayList<int[][]>> future1 = executor.submit(() -> processArrayList(firstHalf));
            Future<ArrayList<int[][]>> future2 = executor.submit(() -> processArrayList(secondHalf));
            
            try {

                newList.add(strassenMultiply(future1.get().get(0), future2.get().get(0)));
            } catch (InterruptedException | ExecutionException e) {
                System.out.println("Error in processing: " + e);
            } finally {
                executor.shutdown();
            }
            return newList;
        }
    }

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
}