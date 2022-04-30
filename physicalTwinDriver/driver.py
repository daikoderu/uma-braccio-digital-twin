import sys
from threading import Thread

import redis
import serial
import time

class Braccio:
    def __init__(self, serial_port):
        self.port = serial.Serial(serial_port, 115200, timeout=5)
        time.sleep(3)

    def write(self, string):
        self.port.write(string.encode())

    def read(self):
        return self.port.readline().decode().strip()

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


def input_channels(robot, dl):
    while True:
        # Here we would check for commands from the Data Lake
        # for now, we just show a prompt
        robot.write(input("> "))


def output_channels(robot, dl):
    while True:
        out = robot.read()
        if out:
            print(f"[RECEIVED: {out}]")


def setup():
    try:
        # Create data lake connection
        # host, port = enter_redis_hostport()
        # dl = redis.Redis(host, port)

        # Read serial port
        serialport = input("Enter serial port: ")
        robot = Braccio(serialport)

        return {
            "success": True,
            "robot": robot,
            "dl": None,
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
        input_thread = Thread(target=input_channels, name="InputThread", args=(robot, dl))
        output_thread = Thread(target=output_channels, name="OutputThread", args=(robot, dl))

        input_thread.start()
        output_thread.start()

        input_thread.join()
        output_thread.join()
    else:
        print(setup_result["error"])
        return 1

if __name__ == "__main__":
    sys.exit(main())