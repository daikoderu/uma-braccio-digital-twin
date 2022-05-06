import serial
import time


class Braccio:
    def __init__(self, serial_port: str):
        self.port = serial.Serial(serial_port, 115200, timeout=5)
        time.sleep(3)

    def write(self, string: str):
        self.port.write(string.encode())

    def read(self) -> str:
        return self.port.readline().decode().strip()