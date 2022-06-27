package digital.twin;

import org.neo4j.driver.Result;
import org.neo4j.driver.Transaction;

import static org.neo4j.driver.Values.parameters;

public class DTNeo4jUtils {

    public static void ensureTimestamp(Transaction tx, int timestamp) {
        int count = tx.run("MATCH (t:Time) where t.timestamp=$timestamp RETURN count(t)",
                parameters("timestamp", timestamp))
                .single().get("count(t)", 0);
        if (count == 0) {
            tx.run("CREATE (t:Time {timestamp: $timestamp}) RETURN t",
                    parameters("timestamp", timestamp));

            updateNextRelation(tx, timestamp);
        }
    }

    public static int getDTTimestampInDataLake(Transaction tx) {
        Result result = tx.run("MATCH (ex:Execution) RETURN ex.DTnow");
        return result.single().get("ex.DTnow", 0);
    }

    public static void updateDTTimestampInDataLake(Transaction tx, int timestamp) {
        timestamp = Math.max(getDTTimestampInDataLake(tx), timestamp);
        tx.run("MATCH (ex:Execution) SET ex.DTnow = $timestamp",
                parameters("timestamp", timestamp));
    }

    private static void updateNextRelation(Transaction tx, int insertedTimestamp) {
        Result prevResult = tx.run("MATCH (t:Time) WHERE t.timestamp < $insertedTimestamp " +
                        "RETURN t.timestamp ORDER BY t.timestamp DESC LIMIT 1",
                parameters("insertedTimestamp", insertedTimestamp));
        Result nextResult = tx.run("MATCH (t:Time) WHERE t.timestamp > $insertedTimestamp " +
                        "RETURN t.timestamp ORDER BY t.timestamp LIMIT 1",
                parameters("insertedTimestamp", insertedTimestamp));

        Integer prev = null, next = null;
        if (prevResult.hasNext()) {
            prev = prevResult.next().get("t.timestamp", 0);
            tx.run("MATCH (prev:Time), (new:Time) " +
                    "WHERE prev.timestamp = $existingTimestamp AND new.timestamp = $insertedTimestamp " +
                    "CREATE (prev)-[:NEXT]->(new)",
                    parameters("existingTimestamp", prev,
                            "insertedTimestamp", insertedTimestamp));
        }
        if (nextResult.hasNext()) {
            next = nextResult.next().get("t.timestamp", 0);
            tx.run("MATCH (next:Time), (new:Time) " +
                    "WHERE next.timestamp = $existingTimestamp AND new.timestamp = $insertedTimestamp " +
                    "CREATE (new)-[:NEXT]->(next)",
                    parameters("existingTimestamp", next,
                            "insertedTimestamp", insertedTimestamp));
        }

        if (prev != null && next != null) {
            tx.run("MATCH (prev:Time)-[n:NEXT]->(next:Time) " +
                    "WHERE prev.timestamp = $prev AND next.timestamp = $next " +
                    "DELETE n",
                    parameters("prev", prev, "next", next));
        }
    }

}
