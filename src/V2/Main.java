package V2;

import V2.client.PrimaryClient;
import V2.client.SecondaryClient;
import V2.util.StrassenAlgorithmUtil;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    static ArrayList<int[][]> matrices = new ArrayList<>();

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
                        System.out.println("Create matrices first\n");
                        createRandomMatrixCase(scanner);

                        System.out.println("Starting Primary Client");

                        System.out.println("Enter IP address of Server Router:");
                        String ip = "localhost";//scanner.nextLine();

                        System.out.println("Enter port number of Server Router:");
                        int port = 123; //Integer.parseInt(scanner.nextLine());

                        PrimaryClient primaryClient = new PrimaryClient(matrices);
                        primaryClient.start(ip, port);
                        return;
                    }

                    case 2 -> {
                        System.out.println("Starting Secondary Client");

                        System.out.println("Enter IP address of Server Router:");
                        String ip = scanner.nextLine();

                        System.out.println("Enter port number of Server Router:");
                        int port = Integer.parseInt(scanner.nextLine());

                        SecondaryClient secondaryClient = new SecondaryClient();
                    }

                    case 3 -> {
                        System.out.println("Starting Server Router");

                        System.out.println("Enter post number Server Router should listen on:");
                        int port = 123; //Integer.parseInt(scanner.nextLine());
                        ServerRouter serverRouter = new ServerRouter(port);

                        return;
                    }

                    case 4 -> {
                        System.out.println("Exiting...");
                        return;
                    }

                    default -> System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public static void createRandomMatrixCase(Scanner scanner) throws InterruptedException {
        System.out.println("How many matrices do you want to create?");
        int matrixCount = Integer.parseInt(scanner.nextLine());

        System.out.println("how big do you want these matrices to be?");
        int matrixSize = Integer.parseInt(scanner.nextLine());

        System.out.println("Creating randomized matrices...");
        Thread.sleep(2000);

        StrassenAlgorithmUtil.generateRandomMatrices(matrices, matrixCount, matrixSize);
        System.out.println("Matrices created");

        for (int i = 0; i < matrices.size(); i++) {
            System.out.println("Matrix " + (i + 1) + ":");
            StrassenAlgorithmUtil.printMatrix(matrices.get(i));
        }
    }
}


