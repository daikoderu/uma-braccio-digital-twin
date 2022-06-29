package api;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.io.Closeable;

import static org.neo4j.driver.Values.parameters;

/**
 * @author Daniel Pérez - University of Málaga
 * API to access the Data Lake.
 */
@SuppressWarnings("unused")
public class DTDataLake implements Closeable {

    private final Session session;

    DTDataLake(Driver driver) {
        this.session = driver.session();
    }

    @Override
    public void close() {
        session.close();
    }

    /**
     * Performs a ping.
     * @return True if the ping was successful.
     */
    public boolean ping() {
        try {
            session.writeTransaction(tx -> {
                tx.run("CREATE (p:_____Ping) RETURN p");
                tx.run("MATCH (p:_____Ping) DELETE p");
                return null;
            });
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Gets the current time for the Physical Twin.
     * @return The value of the Physical Twin's clock.
     */
    public int getPTTime() {
        return session.readTransaction(tx -> {
            Result result = tx.run("MATCH (ex:Execution) RETURN ex.PTnow");
            if (result.hasNext()) {
                return result.next().get("ex.PTnow").asInt();
            } else {
                return 0;
            }
        });
    }

    /**
     * Gets the current time for the Digital Twin.
     * @return The value of the Digital Twin's clock.
     */
    public int getDTTime() {
        return session.readTransaction(tx -> {
            Result result = tx.run("MATCH (ex:Execution) RETURN ex.DTnow");
            if (result.hasNext()) {
                return result.next().get("ex.DTnow").asInt();
            } else {
                return 0;
            }
        });
    }

    /**
     * Advances the Digital Twin's time.
     * @param amount The number of milliseconds to advance.
     */
    public void advanceDTTime(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be non-negative");
        }
        session.writeTransaction(tx -> {
            Result result = tx.run("MATCH (ex:Execution) SET ex.DTnow = ex.DTnow + $amount",
                    parameters("amount", amount));
            return null;
        });
    }

    /**
     * Returns the ID of the current execution.
     * @return The ID of the current execution.
     */
    public String getCurrentExecutionId() {
        return session.readTransaction(tx -> {
            Result result = tx.run("MATCH (ex:Execution) RETURN ex.executionId");
            if (result.hasNext()) {
                return result.next().get("ex.executionId").asString();
            } else {
                return null;
            }
        });
    }

    /**
     * Returns the current value of the command counter.
     * @return The current value of the command counter.
     */
    public int getCommandCounter() {
        return session.readTransaction(tx -> {
            Result result = tx.run("MATCH (ex:Execution) RETURN ex.commandCounter");
            if (result.hasNext()) {
                return result.next().get("ex.commandCounter").asInt();
            } else {
                return 0;
            }
        });
    }

    /**
     * Generates and returns a DLTwin object to perform queries on a specific twin system
     * in the current executionId.
     * @param twinId The ID of the twin to query.
     * @return A DLTwin object to perform queries on the specified twin.
     */
    public DLTwin forTwin(String twinId) {
        return new DLTwin(this, session, twinId);
    }

}
