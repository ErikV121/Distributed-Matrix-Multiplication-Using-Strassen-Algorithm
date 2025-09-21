package V2.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SecondaryProtocol implements ConnectionHandler {

    @Override
    public void handle(DataOutputStream out, DataInputStream in) throws IOException {
//        here , it does its job meaning it receives then multiplies to sends back, then waits again for next
    }
}
