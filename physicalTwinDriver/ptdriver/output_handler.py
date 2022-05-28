from ptdriver.ptcontext import PTContext


def handle_output_snapshot(output: str, context: PTContext):
    try:
        # Parse snapshot
        timestamp, currentpos, targetpos, speeds = output.split(':')
        currentpos_list = currentpos.split(',')
        targetpos_list = targetpos.split(',')
        speeds_list = speeds.split(',')
        context.update_timestamp(int(timestamp))

        # Build the hash
        hash = {
            "twinId": context.twin_id,
            "executionId": context.execution_id,
            "timestamp": timestamp,
        }
        is_moving = False
        for i in range(6):
            hash[f"currentAngles_{i + 1}"] = currentpos_list[i]
            hash[f"targetAngles_{i + 1}"] = targetpos_list[i]
            hash[f"currentSpeeds_{i + 1}"] = speeds_list[i]
            if float(speeds_list[i]) > 0:
                is_moving = True
        hash["moving"] = 1 if is_moving else 0

        # Save snapshot to the Data Lake
        key = f"PTOutputSnapshot:{context.twin_id}:{context.execution_id}:{context.timestamp}"
        context.datalake.hset(key, mapping=hash)
        context.datalake.zadd("PTOutputSnapshot_PROCESSED", {key: timestamp})
        print(f"Saved output object: {key}")

        # Save a reference to this hash in the twin's "history"
        history_key = f"PTOutputSnapshot:{context.twin_id}:{context.execution_id}_HISTORY"
        context.datalake.zadd(history_key, {key: timestamp})

    except Exception as ex:
        print(f"Error saving output snapshot: {ex}")


def handle_command_result(output: str, context: PTContext):
    try:
        command = context.command
        if command is not None:
            # We assume the command stored in context.command is the command
            # that has just been executed
            hash = {
                "twinId": context.twin_id,
                "executionId": context.execution_id,
                "timestamp": context.timestamp,
                "commandId": command.id,
                "commandName": command.name,
                "commandArguments": command.arguments,
                "commandTimestamp": command.when_processed,
                "return": output
            }

            # Save command to the Data Lake
            key = f"PTCommandResult:{context.twin_id}:{context.execution_id}:{command.id}"
            context.datalake.hset(key, mapping=hash)
            context.datalake.zadd("PTCommandResult_PROCESSED", {key: command.id})
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