from redis import Redis
import time

from ptdriver.ptcontext import PTContext
from ptdriver.config import *
from ptdriver.command import Command


sleep_time_in_millis = 100


def next_command(dl: Redis):
    """Return the Redis hash key of the next command to process,
    or None if there are no new commands.
    """
    last_key = dl.zrange("PTCommand_UNPROCESSED", start=0, end=0)
    if last_key:
        return last_key[0]
    else:
        return None

    
def send_commands(context: PTContext):
    """Send a command to the Data Lake if it is not busy executing
    another command.
    """
    if context.command is None:
        # We can receive a new command as we are not busy
        command_key = next_command(context.datalake)
        if command_key:
            # New command received from the Data Lake
            command = Command(context.datalake.hgetall(command_key))
            if context.twin_id == command.twin_id \
                    and context.execution_id == command.execution_id:

                # Forward the command to the physical twin
                context.robot.write(f"COM {command.get_command_line()}")
                context.command = command

                # Move to the processed commands list
                context.datalake.zrem("PTCommand_UNPROCESSED", command_key)
                context.datalake.zadd("PTCommand_PROCESSED", {command_key: command.id})
                context.datalake.hset(command_key, key="whenProcessed", value=context.timestamp)
                command.when_processed = context.timestamp

            else:
                pass  # This command is not for this twin or this execution


def input_handler(context: PTContext):
    while not context.quit:
        send_commands(context)
        time.sleep(SLEEP_TIME_MS / 1000)