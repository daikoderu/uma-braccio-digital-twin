# Braccio Digital Twin
 
A digital twin system of a [TinkerKit Braccio robotic arm](https://www.arduino.cc/en/Guide/Braccio) developed using [Atenea Research Group's Digital Twin Framework](https://github.com/atenearesearchgroup/digitalTwinModelingFramework/tree/traces-management).

## Overview

The structure of this repository is the following:


### robotApi (BraccioPT)

A custom library for the robot, inspired by the [BraccioRobot library](https://github.com/stefangs/arduino-library-braccio-robot).


### useModel

The USE model that represents the robot.


### useConnector

A USE plugin to connect and synchronize the contents of the USE model with a data lake.


### shell

This folder contains shell scripts used in the development of this project:

*   ``updateRobotApi``: used to update an Arduino IDE installation's libraries to include the newest version of the BraccioPT API.
*   ``updateUsePlugin``: used to update an USE installation's plugins to include the newest USE connector plugin ``jar`` file.
*   ``openModel``: opens the digital twin model in USE and loads its instantiation in SOIL.