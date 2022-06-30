from typing import Optional
from neo4j import Transaction


def get_execution_id(tx: Transaction) -> Optional[str]:
    result = tx.run("MATCH (ex:Execution) RETURN ex.executionId")
    record = result.single()
    if record is not None:
        return record.get("executionId")
    else:
        return None

def set_ptnow(tx: Transaction, value: int) -> None:
    tx.run("MATCH (ex:Execution) SET ex.PTnow = $value", value=value)

def ensure_timestamp(tx: Transaction, timestamp: int) -> None:
    count = tx.run(
        "MATCH (t:Time) where t.timestamp = $timestamp RETURN count(t)",
        timestamp=timestamp
        ).single().get("count(t)", 0)

    if count == 0:
        tx.run(
            "CREATE (t:Time) SET t.timestamp = $timestamp",
            timestamp=timestamp
        )
        _update_next_relation(tx, timestamp)


def _update_next_relation(tx: Transaction, timestamp: int) -> None:
    prev_result = tx.run(
        "MATCH (t:Time) WHERE t.timestamp < $insertedTimestamp "
        "RETURN t.timestamp ORDER BY t.timestamp DESC LIMIT 1",
        insertedTimestamp=timestamp
        )
    next_result = tx.run(
        "MATCH (t:Time) WHERE t.timestamp > $insertedTimestamp "
        "RETURN t.timestamp ORDER BY t.timestamp LIMIT 1",
        insertedTimestamp=timestamp
    )

    prev, next = None, None
    if prev_result.peek() is not None:
        prev = prev_result.single().get("t.timestamp", 0)
        tx.run(
            "MATCH (prev:Time), (new:Time) " +
            "WHERE prev.timestamp = $existingTimestamp AND new.timestamp = $insertedTimestamp " +
            "CREATE (prev)-[:NEXT]->(new)",
            existingTimestamp=prev,
            insertedTimestamp=timestamp
        )
    if next_result.peek() is not None:
        next = next_result.single().get("t.timestamp", 0)
        tx.run(
            "MATCH (new:Time), (next:Time) " +
            "WHERE next.timestamp = $existingTimestamp AND new.timestamp = $insertedTimestamp " +
            "CREATE (new)-[:NEXT]->(next)",
            existingTimestamp=prev,
            insertedTimestamp=timestamp
        )

    if prev is not None and next is not None:
        tx.run(
            "MATCH (prev:Time)-[n:NEXT]->(next:Time) " +
            "WHERE prev.timestamp = $prev AND next.timestamp = $next " +
            "DELETE n",
            prev=prev, next=next
        );