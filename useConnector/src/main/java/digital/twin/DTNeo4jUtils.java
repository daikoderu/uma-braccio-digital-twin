package digital.twin;

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

            createNextRelation(tx);
        }
    }

    private static void createNextRelation(Transaction tx) {
        tx.run("MATCH (:Time)-[nxt:NEXT]->(:Time) DELETE nxt");
        tx.run("MATCH (t:Time) " +
                "WITH t ORDER BY t.timestamp DESC " +
                "WITH collect(t) as timestamps " +
                "FOREACH (i in range(0, size(timestamps) - 2) | " +
                "FOREACH (ts1 in [timestamps[i]] | " +
                "FOREACH (ts2 in [timestamps[i+1]] | " +
                "CREATE (ts2)-[:NEXT]->(ts1))))");
    }

}
