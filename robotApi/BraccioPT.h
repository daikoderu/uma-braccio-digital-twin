/*
    File: BraccioPT.cpp

    Copyright 2022 Daniel Pérez Porras, daniperezporras@gmail.com
    Some parts from Stefan Strömberg's API (https://github.com/stefangs/arduino-library-braccio-robot)

    Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
    in compliance with the License. You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software distributed under the License
    is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied. See the License for the specific language governing permissions and
    limitations under the License.
*/

#ifndef BRACCIO_PT_H
#define BRACCIO_PT_H

#include <Servo.h>
#ifndef Servo_h
#error Sketch must include Servo.h
#endif

#include "Position.h"

#define SOFT_START_CONTROL_PIN 12
#define SOFT_START_TIME 1000

#define MS_PER_S 1000
#define STEP_DELAY_MS 10

// Maximum speed in degrees per second
#define MAXIMUM_SPEED 200

// Maximum speed in degrees per second for the base and shoulder joints
#define BIG_JOINT_MAXIMUM_SPEED 140

class _BraccioPT
{

    public:

        static Position initialPosition;

        // Initializes the arm.
        void init(
            Position& startPosition = _BraccioPT::initialPosition,
            bool doSoftStart = true,
            unsigned long baudRate = 115200);

        // Moves the arm to the given position in at least minTime seconds.
        void moveToPosition(const Position& newPosition, double minTime);

        // Returns how much time will take to move the arm to the given position.
        double getMoveDuration(const Position& newPosition, double minTime);

    private:

        Servo base;
        Servo shoulder;
        Servo elbow;
        Servo wristRotation;
        Servo wrist;
        Servo gripper;
        Position currentPosition;

        void softStart();

};

extern _BraccioPT BraccioPT;

#endif // BRACCIO_PT_H
