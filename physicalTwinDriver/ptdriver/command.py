from ast import arguments
from ptdriver.utils import decode_dict


class Command:

    def __init__(self, hash: dict):
        decoded_hash = decode_dict(hash)
        self.id = decoded_hash["commandId"]
        self.twin_id = decoded_hash["twinId"]
        self.execution_id = decoded_hash["executionId"]
        self.name = decoded_hash["name"]
        self.arguments = decoded_hash["arguments"]
        self.when_processed = None

    def get_command_line(self) -> str:
        return f"{self.name} {self.arguments}"