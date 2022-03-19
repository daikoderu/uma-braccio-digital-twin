# Braccio Digital Twin
 
A digital twin system of a [TinkerKit Braccio robotic arm](https://www.arduino.cc/en/Guide/Braccio) developed using [Atenea Research Group's Digital Twin Framework](https://github.com/atenearesearchgroup/digitalTwinModelingFramework/tree/traces-management).

## Overview

The structure of this repository is the following:

### robotApi (BraccioPT)

A custom library for the robot, inspired by the [BraccioRobot library](https://github.com/stefangs/arduino-library-braccio-robot).

### useModel

The USE model that represent the robot.

### shell

This folder contains shell scripts used in the development of this project:

*   ``updateLibrary``: used to update an Arduino IDE installation's libraries to the newest version of the BraccioPT API.