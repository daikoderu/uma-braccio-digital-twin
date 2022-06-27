package api;


import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

import java.io.Closeable;

/**
 * @author Daniel Pérez - University of Málaga
 * Class to encapsulate a connection with the Data Lake.
 */
public class DTDLConnection implements Closeable {

    private final Driver driver;

    public DTDLConnection(String host, int port) {
        driver = GraphDatabase.driver("bolt://" + host + ":" + port);
    }

    public DTDataLake getDataLake() {
        return new DTDataLake(driver);
    }

    public void close() {
        driver.close();
    }

}
