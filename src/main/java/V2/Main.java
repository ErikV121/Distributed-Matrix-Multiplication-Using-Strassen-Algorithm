package V2;

import V2.client.primary.PrimaryClient;
import V2.client.secondary.SecondaryClient;

import java.util.ArrayList;
import java.util.Scanner;


public class Main {
    static ArrayList<long[][]> matrices = new ArrayList<>();
//    only support squared matrices for now
    static int matrixSize;
    static int matrixAmount;
    static long t0;

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("""
                        Menu:\s
                        1. Start Primary Client
                        2. Start Secondary Client
                        3. Start Server Router
                        4. Exit
                        """);

                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1 -> {
                        handlePrimaryClient(scanner);
                        return;
                    }
                    case 2 -> {
                        handleSecondaryClient(scanner);
                    }
                    case 3 -> {
                        handleServerRouter(scanner);
                        return;
                    }
                    case 4 -> {
                        System.out.println("Exiting...");
                        return;
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            }
        }
    }

    public static void handlePrimaryClient(Scanner scanner) {
        System.out.println("Create matrices first\n");
        createRandomMatrixCase(scanner);

        System.out.println("Starting Primary Client");

        System.out.println("Enter IP address of Server Router:");
//        String ip = scanner.nextLine();
//        TODO for testing
        String ip = "localhost";

        System.out.println("Enter port number of Server Router:");
//        int port = Integer.parseInt(scanner.nextLine());
        // TODO for testing
        int port = 3000;
        t0 = System.nanoTime();
        PrimaryClient primaryClient = new PrimaryClient(matrices, matrixSize, matrixAmount );
        primaryClient.start(ip, port);

        long t1 = System.nanoTime();
        System.out.println("milliseconds to compute = " + (t1 - t0) / 1_000_000.0);

    }

    public static void handleSecondaryClient(Scanner scanner) {
        System.out.println("Starting Secondary Client");

        System.out.println("Enter IP address of Server Router:");
//        String ip = scanner.nextLine();
        String ip = "localhost";

        System.out.println("Enter port number of Server Router:");
//        int port = Integer.parseInt(scanner.nextLine());
        int port = 3001;


        SecondaryClient secondaryClient = new SecondaryClient();
        secondaryClient.start(ip, port);
    }

    public static void handleServerRouter(Scanner scanner) {
        System.out.println("Starting Server Router");

        // int port = Integer.parseInt(scanner.nextLine());
        int port = 3000;

        ServerRouter serverRouter = new ServerRouter();

        // Start each accept loop on its own virtual thread
        Thread v1 = Thread.ofVirtual()
                .name("server1")
                .start(() -> serverRouter.startServer1(port));

        Thread v2 = Thread.ofVirtual()
                .name("server2")
                .start(() -> serverRouter.startServer2(port + 1));

        try {
            v1.join();
            v2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }



    public static void createRandomMatrixCase(Scanner scanner) {
        System.out.println("How many matrices do you want to create?");
        matrixAmount = Integer.parseInt(scanner.nextLine());

        System.out.println("how big do you want these matrices to be?");
        matrixSize = Integer.parseInt(scanner.nextLine());

        System.out.println("Creating randomized matrices...");


        StrassenAlgorithmUtil.generateRandomMatrices(matrices, matrixAmount, matrixSize);
        System.out.println("Matrices created");

        StrassenAlgorithmUtil.printMatrixList(matrices);
    }
}


