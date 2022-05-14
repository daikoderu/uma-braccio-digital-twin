from redis import Redis

from .braccio import Braccio


def handle_output_snapshot(snapshot: str, dl: Redis, status: dict):
    try:
        twin_id, executionId = status["twinId"], status["executionId"]
        timestamp, currentpos, targetpos, speeds = snapshot.split(':')
        currentpos_list = currentpos.split(',')
        targetpos_list = targetpos.split(',')
        speeds_list = speeds.split(',')
        status["timestamp"] = max(int(timestamp), status["timestamp"])
        hash = {
            "twinId": twin_id,
            "executionId": executionId,
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

        # Save to the Data Lake
        key = f"PTOutputSnapshot:{twin_id}:{executionId}:{timestamp}"
        dl.hset(key, mapping=hash)
        dl.zadd("PTOutputSnapshot_PROCESSED", {key: timestamp})
        print(f"Saved output object: {key}")
    except Exception as ex:
        print(f"Error saving output snapshot: {ex}")


def handle_command_result(result: str, dl: Redis, status: dict):
    try:
        twin_id, executionId = status["twinId"], status["executionId"]
        command = status["command"]
        if command is not None:
            command_id = command["commandId"]
            hash = {
                "twinId": twin_id,
                "executionId": executionId,
                "timestamp": status["timestamp"],
                "commandId": command_id,
                "commandName": command["name"],
                "commandArguments": command["arguments"],
                "commandTimestamp": command["whenProcessed"],
                "return": result
            }

            # Save to the Data Lake
            key = f"PTCommandResult:{twin_id}:{executionId}:{command_id}"
            dl.hset(key, mapping=hash)
            dl.zadd("PTCommandResult_PROCESSED", {key: command_id})
            print(f"Saved output object: {key}")

            # Unset current command
            status["command"] = None
        else:
            pass # print(f"Error saving command result: command not found")
    except Exception as ex:
        print(f"Error saving command result: {ex}")


def output_handler(robot: Braccio, dl: Redis, status: dict):
    try:
        while not status["quit"]:
            try:
                out = robot.read()
                if out:
                    if out.startswith("OUT "):
                        # This is an output snapshot
                        handle_output_snapshot(out[4:], dl, status)
                    elif out.startswith("RET "):
                        # This is a command result
                        handle_command_result(out[4:], dl, status)
            except:
                pass
    except EOFError:
        print("End of file found. Aborting.")
        status["quit"] = True