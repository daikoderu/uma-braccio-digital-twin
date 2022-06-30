from neo4j import Record


class Command:

    def __init__(self, rec: Record, twin_id: str, execution_id: str):
        i = rec.get("i")
        self.id = i.get("commandId")
        self.twin_id = twin_id
        self.execution_id = execution_id
        self.name = i.get("name")
        self.arguments = i.get("arguments")
        self.when_processed = None

    def get_command_line(self) -> str:
        return f"{self.name} {self.arguments}"