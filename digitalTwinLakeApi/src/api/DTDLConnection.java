package api;

import redis.clients.jedis.JedisPool;

import java.io.Closeable;

/**
 * @author Daniel Pérez - University of Málaga
 * Class to encapsulate a connection with the Data Lake.
 */
public class DTDLConnection implements Closeable {

    private final JedisPool pool;

    public DTDLConnection(String host, int port) {
        pool = new JedisPool(host, port);
    }

    public DTDataLake getDataLake() {
        return new DTDataLake(pool);
    }

    public void close() {
        pool.close();
    }

}
