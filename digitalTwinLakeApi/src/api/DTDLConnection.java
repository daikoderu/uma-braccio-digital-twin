package api;

import redis.clients.jedis.JedisPool;

import java.io.Closeable;

public class DTDLConnection implements Closeable {

    private final JedisPool pool;

    public DTDLConnection(String host, int port) {
        pool = new JedisPool(host, port);
    }

    public DTDataLake getResource() {
        return new DTDataLake(pool);
    }

    public void close() {
        pool.close();
    }

}
