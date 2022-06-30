from ptdriver.transactions import save_command_result
from ptdriver.ptcontext import PTContext
from ptdriver.transactions import save_output_object


def handle_output_snapshot(output: str, context: PTContext):
    try:
        # Parse snapshot
        timestamp, currentpos, targetpos, speeds = output.split(':')
        currentpos_list = currentpos.split(',')
        targetpos_list = targetpos.split(',')
        speeds_list = speeds.split(',')
        context.update_timestamp(int(timestamp))

        # Build the attribute dictionary
        hash = {
            "currentAngles": [],
            "targetAngles": [],
            "currentSpeeds": [],
            "moving": False
        }
        for i in range(6):
            hash["currentAngles"].append(int(currentpos_list[i]))
            hash["targetAngles"].append(int(targetpos_list[i]))
            hash["currentSpeeds"].append(float(speeds_list[i]))
            if float(speeds_list[i]) > 0:
                hash["moving"] = True
        
        # Save snapshot to the Data Lake
        with context.datalake.session() as session:
            session.write_transaction(
                lambda tx:
                    save_output_object(tx, "OutputSnapshot", "IS_IN_STATE",
                    context.twin_id, context.execution_id,
                    hash, context.timestamp)
            )

        key = f"PTOutputSnapshot:{context.twin_id}:{context.execution_id}:{context.timestamp}"
        print(f"Saved output object: {key}")

    except Exception as ex:
        print(f"Error saving output snapshot: {ex}")


def handle_command_result(output: str, context: PTContext):
    try:
        command = context.command
        if command is not None:
            # We assume the command stored in context.command is the command
            # that has just been executed

            # Save snapshot to the Data Lake
            with context.datalake.session() as session:
                session.write_transaction(
                    lambda tx:
                        save_command_result(tx,
                            context.twin_id, context.execution_id, command.id,
                            output, context.timestamp)
                )

            # Save command to the Data Lake
            key = f"PTCommandResult:{context.twin_id}:{context.execution_id}:{command.id}"
            print(f"Saved output object: {key}")
        else:
            print(f"Error saving command result: no command.")
    except Exception as ex:
        print(f"Error saving command result: {ex}")

    # Unset current command
    context.command = None


output_handlers = {
    "OUT": handle_output_snapshot,
    "RET": handle_command_result,
}


def output_handler(context: PTContext):
    try:
        # Flush any output from previous executions
        context.robot.flush()

        while not context.quit:
            try:
                out = context.robot.read()
                for prefix, handler in output_handlers.items():
                    if out.startswith(f"{prefix} "):
                        handler(out[len(prefix) + 1:], context)
                        break
                else:
                    print("Unknown output from physical twin!")
            except:
                pass
    except EOFError:
        print("End of file found. Aborting.")
        context.quit = True