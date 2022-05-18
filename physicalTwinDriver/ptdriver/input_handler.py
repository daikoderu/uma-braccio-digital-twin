from redis import Redis
import time

from .braccio import Braccio
from .utils import decode_dict


sleep_time_in_millis = 100


def next_command(dl: Redis):
    last_key = dl.zrange("PTCommand_UNPROCESSED", start=0, end=0)
    if last_key:
        return last_key[0]
    else:
        return None

    
def send_commands(robot: Braccio, dl: Redis, status: dict):
    if not status["command"]:
        command_key = next_command(dl)
        if command_key:
            # New command received from the Data Lake
            command = decode_dict(dl.hgetall(command_key))
            if status["twinId"] == command["twinId"]:
                name, args, command_id = command["name"], command["arguments"], command["commandId"]
                robot.write(f"COM {name} {args}")
                status["command"] = command
                command["whenProcessed"] = status["timestamp"]

                # Move to the processed commands list
                dl.zrem("PTCommand_UNPROCESSED", command_key)
                dl.zadd("PTCommand_PROCESSED", {command_key: command_id})
                dl.hset(command_key, mapping=command)
            else:
                pass  # This command is not for this twin


def input_handler(robot: Braccio, dl: Redis, status: dict):
    while not status["quit"]:
        send_commands(robot, dl, status)
        time.sleep(sleep_time_in_millis / 1000)