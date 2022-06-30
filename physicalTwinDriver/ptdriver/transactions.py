from typing import Optional
from xmlrpc.client import boolean
from neo4j import Transaction


def create_robot(tx: Transaction, twin_id: str, execution_id: str) -> None:
    tx.run(
        "CREATE (r:BraccioRobot) SET r.twinId = $twinId, r.executionId = $executionId, "
        "r.isPhysical = true",
        twinId=twin_id, executionId=execution_id
    )

def save_output_snapshot(
    tx: Transaction,
    twin_id: str,
    execution_id: str, 
    attrs: dict,
    timestamp: int):

    node_id = tx.run(
        "CREATE (o:OutputSnapshot $attributes) RETURN id(o)",
        attributes=attrs
        ).single().get("id(o)", 0)

    tx.run(
        "MATCH (r:BraccioRobot), (o:OutputSnapshot) "
        "WHERE r.twinId = $twinId AND r.executionId = $executionId "
        "AND r.isPhysical AND id(o) = $id "
        "CREATE (r)-[:IS_IN_STATE]->(o)",
        twinId=twin_id,
        executionId=execution_id,
        id=node_id
    )

    ensure_timestamp(tx, timestamp)
    tx.run(
        "MATCH (o:OutputSnapshot), (t:Time) "
        "WHERE id(o) = $id AND t.timestamp = $timestamp "
        "CREATE (o)-[:AT_TIME]->(t)",
        id=node_id,
        timestamp=timestamp,
    )

def get_execution_id(tx: Transaction) -> Optional[str]:
    result = tx.run("MATCH (ex:Execution) RETURN ex.executionId")
    record = result.single()
    if record is not None:
        return record.get("ex.executionId")
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