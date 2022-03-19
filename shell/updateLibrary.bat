@echo off

call shell/_paths.bat

rmdir %arduinoLibPath% /s /q > NUL
robocopy robotApi %arduinoLibPath% /E > NUL