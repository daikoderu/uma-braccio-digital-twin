import time

from ptdriver.transactions import next_command, process_input_object
from ptdriver.ptcontext import PTContext
from ptdriver.config import *

    
def send_commands(context: PTContext):
    """Send a command to the Data Lake if it is not busy executing
    another command.
    """
    if context.command is None:
        with context.datalake.session() as session:
            # We can receive a new command as we are not busy
            result = session.read_transaction(
                lambda tx:
                    next_command(tx, context.twin_id, context.execution_id)
            )
            if result is not None:
                node_id, command = result

                # Forward the command to the physical twin
                context.robot.write(f"COM {command.get_command_line()}")
                context.command = command

                # Move to the processed commands list

                session.write_transaction(
                    lambda tx:
                        process_input_object(tx, node_id, context.timestamp)
                )
                command.when_processed = context.timestamp


def input_handler(context: PTContext):
    while not context.quit:
        send_commands(context)
        time.sleep(SLEEP_TIME_MS / 1000)