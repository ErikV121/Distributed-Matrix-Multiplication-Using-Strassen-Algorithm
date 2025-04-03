package V2.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface ClientDataHandler {
    void handleData(DataOutputStream dataWriter, DataInputStream dataReader) throws IOException;
}
