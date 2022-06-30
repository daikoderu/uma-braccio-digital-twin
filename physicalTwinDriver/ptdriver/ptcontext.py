from neo4j import Driver

from ptdriver.transactions import set_ptnow
from ptdriver.braccio import Braccio


class PTContext:

    def __init__(self, robot: Braccio, datalake: Driver, twin_id: str, execution_id: str):
        self.robot = robot
        self.datalake = datalake
        self.twin_id = twin_id
        self.execution_id = execution_id

        self.timestamp = 0
        with self.datalake.session() as session:
            session.write_transaction(lambda tx: set_ptnow(tx, 0))

        self.command = None

        self.quit = False

    def update_timestamp(self, new_value: int) -> int:
        self.timestamp = max(new_value, self.timestamp)
        with self.datalake.session() as session:
            session.write_transaction(lambda tx: set_ptnow(tx, self.timestamp))
        return self.timestamp