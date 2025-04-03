package V1;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class MyClient {
    Socket s;
    DataInputStream din;
    ObjectOutputStream oout;
    ObjectInputStream oin;
    DataOutputStream dout;
    String serverName;
    static long startTime;
    static long endTime;
    static int mAmount = 0;
    ArrayList<int[][]> list = new ArrayList<>();

    public MyClient(int size) {
        try {
            serverName = "192.168.0.27";
            s = new Socket(serverName, 10);
            System.out.println(s);
            din = new DataInputStream(s.getInputStream());
            dout = new DataOutputStream(s.getOutputStream());
            for(int i = 0; i< mAmount; i++)
            {
                generateMatrix(size);
                
            }
            ClientChat();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public MyClient(String server, int port, ArrayList<int[][]> givenList) {
        try {
            serverName = server;
            s = new Socket(serverName, port);
            System.out.println(s);
            list = givenList;
            startTime = System.currentTimeMillis();
            oout = new ObjectOutputStream(s.getOutputStream()); // Initialize ObjectOutputStream
            oout.writeObject(list);
            oout.flush();
            oin = new ObjectInputStream(s.getInputStream()); // Initialize ObjectInputStream
            ClientChat();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void ClientChat() throws IOException {

        if (serverName.equals("192.168.0.27")) {

            dout.writeUTF("192.168.0.27");
            dout.flush();
            String[] response = din.readUTF().split(",");
            new MyClient(response[0], Integer.parseInt(response[1]), this.list);
            dout.writeUTF("192.168.0.27");
            s.close();
        } else {
            // Example of conversing in an ArrayList from the server
            try {
                ArrayList<int[][]> receivedList = (ArrayList<int[][]>) oin.readObject();
                endTime = System.currentTimeMillis();
                long duration = (endTime - startTime)/1000;
                long speedup = duration/(mAmount-1);
                long efficiency = speedup/(mAmount-1);
                System.out.println("Time taken to receive the Matrix: " + duration 
                + " seconds. The speed up is: " + speedup + ". The efficiency is: " + efficiency);
                int[][] rList = receivedList.get(0);
                //     for (int[] row : rList) {
                //     for (int element : row) {
                //         System.out.print(element + " ");
                //     }
                //         System.out.println();
                //     }
                oout.writeObject(new ArrayList<int[][]>());
                s.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        boolean run = true;
        int mSize = 0;

        while (run) {
            try {
                System.out.println("How many matrices(2, 4, 8, 16, or 32)");
                mAmount = Integer.parseInt(scan.nextLine());
                System.out.println("How big should they be?");
                mSize = Integer.parseInt(scan.nextLine());
                if(mAmount == 2 || mAmount == 4 || mAmount == 8 || mAmount == 16 || mAmount == 32 ){
                    run = false;
                }
            } catch (Exception e) {
                System.out.println("invalid size or amount");
                run = true;
            }
        }

        MyClient client = new MyClient(mSize);
        scan.close();
    }
    
    public void generateMatrix(int size) {
        Random random = new Random();
        int[][] matrix = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = random.nextInt(10);
            }
        }
        list.add(matrix);
    }
}