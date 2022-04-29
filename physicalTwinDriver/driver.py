import redis
import serial
import time

class Braccio:
    def __init__(self, serial_port):
        self.port = serial.Serial(serial_port, 115200, timeout=5)
        time.sleep(3)

    def write(self, string):
        self.port.write(string.encode())
        print(self.port.readline())

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

if __name__ == "__main__":
    # Create data lake connection
    # host, port = enter_redis_hostport()
    # datalake = redis.Redis(host, port)

    # Read serial port
    serialport = input("Enter serial port: ")
    robot = Braccio(serialport)

    while True:
        robot.write(input("> "))