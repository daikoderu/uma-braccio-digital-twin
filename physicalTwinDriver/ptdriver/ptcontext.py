from redis import Redis

from ptdriver.braccio import Braccio


class PTContext:

    PTNOW = "PTnow"

    def __init__(self, robot: Braccio, datalake: Redis, twin_id: str, execution_id: str):
        self.robot = robot
        self.datalake = datalake
        self.twin_id = twin_id
        self.execution_id = execution_id

        self.timestamp = 0
        self.datalake.set(self.PTNOW, 0)

        self.command = None

        self.quit = False

    def update_timestamp(self, new_value: int) -> int:
        self.timestamp = max(new_value, self.timestamp)
        self.datalake.set(self.PTNOW, self.timestamp)
        return self.timestamp