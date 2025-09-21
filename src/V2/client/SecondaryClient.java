package V2.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class SecondaryClient {
    private final ArrayList<int[][]> matrices = new ArrayList<>();
    private final ClientConnection connection;

    public SecondaryClient() {
        this.connection = new ClientConnection("SECONDARY");
    }

    public void start(String ip, int port) {
        connection.connect(ip, port, new SecondaryProtocol()); // writes "SECONDARY" first
    }



}


//        ideas of flow FOR NOW, which will seem simpler and has to be done in this order:

//        so server router is listening
//        FIRST secondary client needs to connect before the primary client connects
//        server router will save a list of secondary clients
//        primary client will send the matrices to the server router
//
//        here is where im not sure,
//        server router will use custom strassen algorithm to split the matrices and send them to the secondary clients
//        TODO this will depend because it would be CPU intensive because it might requier multithreading to even get the 7 quadrants for a matrix
//

//        first the secondary client with most amount of cores will get its share
//        then the rest are sent to the other secondary clients
//        and what ever is left will be send back to the first secondary client with the most cores assuming it will finish first
//