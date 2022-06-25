package api;


import java.io.Closeable;

/**
 * @author Daniel Pérez - University of Málaga
 * Class to encapsulate a connection with the Data Lake.
 */
public class DTDLConnection implements Closeable {

    public DTDLConnection(String host, int port) {
        // TODO
    }

    public DTDataLake getDataLake() {
        return new DTDataLake();
    }

    public void close() {
        // TODO
    }

}
