package V2.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@FunctionalInterface
public interface ConnectionHandler {
    void handle(DataOutputStream out, DataInputStream in) throws IOException;


}
