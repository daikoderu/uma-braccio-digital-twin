import sys
from threading import Thread
import time

import redis
import serial

from ptdriver.output_handler import output_handler
from ptdriver.input_handler import input_handler
from ptdriver.braccio import Braccio

def enter_redis_hostport():
    host, _, port_str = input("Enter Data Lake host and port: ").partition(":")
    port = 6379
    if port_str:
        try:
            port = int(port_str)
            if port < 0 or port > 65535:
                port = 6379
        except ValueError:
            pass
    return host, port


def setup():
    try:
        # Read Twin Id
        twin_id = input("Enter Twin Id: ")

        # Create data lake connection
        host, port = enter_redis_hostport()
        dl = redis.Redis(host, port)

        # Get execution ID
        execution_id_bytes = dl.get("executionId")
        if not execution_id_bytes:
            return {
                "success": False,
                "error": "Execution ID not set. Please initialize the Digital Twin first."
            }
        execution_id = execution_id_bytes.decode()

        # Read serial port
        serialport = input("Enter serial port: ")
        robot = Braccio(serialport)

        return {
            "success": True,
            "robot": robot,
            "dl": dl,
            "twinId": twin_id,
            "executionId": execution_id
        }
    except serial.serialutil.SerialException as ex:
        return {
            "success": False,
            "error": f"Serial port error: {ex}"
        }
    except KeyboardInterrupt as ex:
        return {
            "success": False,
            "error": "Goodbye!"
        }

def main():
    setup_result = setup()
    if (setup_result["success"]):
        robot, dl = setup_result["robot"], setup_result["dl"]
        status = {
            "twinId": setup_result["twinId"],
            "executionId": setup_result["executionId"],
            "quit": False,
            "command": None
        }
        input_thread = Thread(target=input_handler, name="InputThread", args=(robot, dl, status))
        output_thread = Thread(target=output_handler, name="OutputThread", args=(robot, dl, status))

        input_thread.start()
        output_thread.start()

        try:
            while True:
                time.sleep(10000)
        except KeyboardInterrupt:
            status["quit"] = True
            print("Goodbye!")
            return 0
    else:
        print(setup_result["error"])
        return 1

if __name__ == "__main__":
    sys.exit(main())