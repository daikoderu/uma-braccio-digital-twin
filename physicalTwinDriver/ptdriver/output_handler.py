from redis import Redis

from .braccio import Braccio


def handle_output_snapshot(snapshot: str, dl: Redis, status: dict):
    twin_id, executionId = status["twinId"], status["executionId"]
    timestamp, currentpos, targetpos, speeds = snapshot.split(':')
    currentpos_list = currentpos.split(',')
    targetpos_list = targetpos.split(',')
    speeds_list = speeds.split(',')
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
    dl.zadd("PTOutputSnapshot_PROCESSED", {key: 0})
    print(f"Saved output object: {key}")


def handle_command_result(result: str, dl: Redis, status: dict):
    twin_id, executionId = status["twinId"], status["executionId"]
    command = status["command"]
    command_id = command["commandId"]
    hash = {
        "twinId": twin_id,
        "executionId": executionId,
        "commandId": command_id,
        "commandName": command["name"],
        "commandArguments": command["arguments"]
        # timestamp, commandTimestamp
    }

    # Save to the Data Lake
    key = f"PTCommandResult:{twin_id}:{executionId}:{command_id}"
    dl.hset(key, mapping=hash)
    dl.zadd("PTCommandResult_PROCESSED", {key: 0})
    print(f"Saved output object: {key}")


def output_handler(robot: Braccio, dl: Redis, status: dict):
    try:
        while not status["quit"]:
            out = robot.read()
            if out:
                print(f"OUT >> {out}")
                if out.startswith("OUT:"):
                    # This is an output snapshot
                    handle_output_snapshot(out[4:], dl, status)
                elif out.startswith("RES:"):
                    # This is a command result
                    handle_command_result(out[4:], dl, status)
    except EOFError:
        print("End of file found. Aborting.")
        status["quit"] = True