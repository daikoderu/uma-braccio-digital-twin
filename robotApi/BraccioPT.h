/*
    File: BraccioPT.h

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
#include "SerialInput.h"

#define SOFT_START_CONTROL_PIN 12
#define SOFT_START_TIME 1000

#define MS_PER_S 1000


class _BraccioPT
{

    public:

        static Position initialPosition;

        // Initializes the arm.
        void init(
            Position& startPosition = _BraccioPT::initialPosition,
            bool doSoftStart = true,
            unsigned long baudRate = 115200);

        // Function to be called in the Arduino loop() function, taking the current value of millis()
        void loop(unsigned long ms);

        // Sets a target position in at least minTime seconds.
        void moveToPosition(const Position& newPosition, float minTime);

        // Returns how much time will take to move the arm to the given position.
        float getMoveDuration(const Position& newPosition, float minTime);

        // Returns the value of a servo by index (0~5)
        int readServo(int i);

        // Returns the value of all servos
        void readAllServos(Position& dest);

        // Sets whether this robot is frozen or not.
        void setFrozen(bool isFrozen);

        // Returns true if the robot is moving right now. If frozen, this may still be true
        // if the robot was moving just before being frozen.
        bool isMoving();

    private:

        Servo base;
        Servo shoulder;
        Servo elbow;
        Servo wristRotation;
        Servo wrist;
        Servo gripper;
        float currentPosition[6];
        Position targetPosition;
        float currentSpeeds[6];
        unsigned long nextMs;
        unsigned long nextSnapshotMs;
        bool isFrozen;

        void softStart();
        void handleMovement(unsigned long ms);
        void generateSnapshots(unsigned long ms);
        
};

extern _BraccioPT BraccioPT;

#endif // BRACCIO_PT_H
