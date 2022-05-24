import sys
from threading import Thread
import time

import redis
import serial

from ptdriver.driver_exception import DriverException
from ptdriver.output_handler import output_handler
from ptdriver.input_handler import input_handler
from ptdriver.braccio import Braccio
from ptdriver.ptcontext import PTContext


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


def setup() -> PTContext:
    """Asks the user to set up the connections to the Data Lake and the physical twin."""
    try:
        # Read Twin Id
        twin_id = input("Enter Twin Id: ")

        # Create data lake connection
        host, port = enter_redis_hostport()
        dl = redis.Redis(host, port)

        # Get execution ID
        execution_id_bytes = dl.get("executionId")
        if not execution_id_bytes:
            raise DriverException(
                "Execution ID not set."
                "Please initialize the Digital Twin first."
            )
        execution_id = execution_id_bytes.decode()

        # Read serial port
        serialport = input("Enter serial port: ")
        robot = Braccio(serialport)

        return PTContext(robot, dl, twin_id, execution_id)
    except serial.serialutil.SerialException as ex:
        raise DriverException(f"Serial port error: {ex}")
    except redis.exceptions.ConnectionError as ex:
        raise DriverException(f"Error when connecting to the Data Lake: {ex}")

def main():
    context = None
    try:
        # Set up connections
        context = setup()
    except DriverException as ex:
        print(ex)
        return 1
    except KeyboardInterrupt:
        print("Quitting...")
        return 0

    if context is not None:
        print(f"PTDriver connected. Execution ID: {context.execution_id}")
        input_thread = Thread(target=input_handler, name="InputThread", args=(context,))
        output_thread = Thread(target=output_handler, name="OutputThread", args=(context,))

        input_thread.start()
        output_thread.start()

        try:
            while True:
                time.sleep(10000)
        except KeyboardInterrupt:
            context.quit = True
            print("Quitting...")
            return 0

if __name__ == "__main__":
    sys.exit(main())