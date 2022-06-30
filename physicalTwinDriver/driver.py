import sys
from threading import Thread
import time

from neo4j import GraphDatabase
from neo4j.exceptions import Neo4jError
import serial

from ptdriver.driver_exception import DriverException
from ptdriver.output_handler import output_handler
from ptdriver.input_handler import input_handler
from ptdriver.braccio import Braccio
from ptdriver.ptcontext import PTContext
from ptdriver.transactions import get_execution_id


def enter_neo4j_hostport():
    host, _, port_str = input("Enter Data Lake host and port: ").partition(":")
    port = 7687
    if port_str:
        try:
            port = int(port_str)
            if port < 0 or port > 65535:
                port = 7687
        except ValueError:
            pass
    return host, port


def setup() -> PTContext:
    """Asks the user to set up the connections to the Data Lake and the physical twin."""
    try:
        # Read Twin Id
        twin_id = input("Enter Twin Id: ")

        # Create data lake connection
        host, port = enter_neo4j_hostport()
        dl = GraphDatabase.driver(f"neo4j://{host}:{port}")

        # Get execution ID
        with dl.session() as session:
            execution_id = session.read_transaction(get_execution_id)

            if not execution_id:
                raise DriverException(
                    "Execution ID not set. "
                    "Please initialize the Digital Twin first."
                )

        # Read serial port
        serialport = input("Enter serial port: ")
        robot = Braccio(serialport)

        return PTContext(robot, dl, twin_id, execution_id)
    except serial.serialutil.SerialException as ex:
        raise DriverException(f"Serial port error: {ex}")
    except Neo4jError as ex:
        raise DriverException(f"Data Lake error: {ex}")

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

        # Create robot in the data lake
        context.create_robot()

        print(f"PTDriver connected. Execution ID: {context.execution_id}")
        input_thread = Thread(target=input_handler, name="InputThread", args=(context,))
        output_thread = Thread(target=output_handler, name="OutputThread", args=(context,))

        #input_thread.start()
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