/*
    File: BraccioPT.cpp

    Copyright 2022 Daniel Pérez Porras, daniperezporras@gmail.com
    Some parts from Stefan Strömberg (https://github.com/stefangs/arduino-library-braccio-robot)

    Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
    in compliance with the License. You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software distributed under the License
    is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied. See the License for the specific language governing permissions and
    limitations under the License.
*/

#include "BraccioPT.h"
#include <Arduino.h>

Position _BraccioPT::initialPosition(90, 90, 90, 90, 90, 73);
_BraccioPT BraccioPT;

void _BraccioPT::init(Position& startPosition, bool doSoftStart, unsigned long baudRate)
{
    if (doSoftStart)
    {
        pinMode(SOFT_START_CONTROL_PIN, OUTPUT);
        digitalWrite(SOFT_START_CONTROL_PIN, LOW);
    }

    base.attach(11);
    shoulder.attach(10);
    elbow.attach(9);
    wrist.attach(6);
    wristRotation.attach(5);
    gripper.attach(3);
            
    base.write(startPosition.getBase());
    shoulder.write(startPosition.getShoulder());
    elbow.write(startPosition.getElbow());
    wrist.write(startPosition.getWrist());
    wristRotation.write(startPosition.getWristRotation());
    gripper.write(startPosition.getGripper());

    if (doSoftStart)
    {
        softStart();
    }
    currentPosition = startPosition;

    // Initialize serial port
    Serial.begin(baudRate);
}

void _BraccioPT::moveToPosition(const Position& newPosition, double minTime)
{
    if (currentPosition != newPosition)
    {
        double time = getMoveDuration(newPosition, minTime);
        double positions[6];
        double steps[6];
        for (int i = 0; i < 6; i++)
        {
            double displacement = abs(newPosition.get(i) - currentPosition.get(i));
            positions[i] = currentPosition.get(i);
            steps[i] = displacement * STEP_DELAY_MS / (MS_PER_S * time);
        }

        for (int t = 0; t < MS_PER_S * time; t += STEP_DELAY_MS)
        {
            for (int i = 0; i < 6; i++)
            {
                if (positions[i] < newPosition.get(i))
                {
                    positions[i] = min(positions[i] + steps[i], newPosition.get(i));
                }
                else if (positions[i] > newPosition.get(i))
                {
                    positions[i] = max(positions[i] - steps[i], newPosition.get(i));
                }
            }
            base.write(int(positions[0]));
            shoulder.write(int(positions[1]));
            elbow.write(int(positions[2]));
            wrist.write(int(positions[3]));
            wristRotation.write(int(positions[4]));
            gripper.write(int(positions[5]));
            delay(STEP_DELAY_MS);
        }
        currentPosition = newPosition;
    }
}

double _BraccioPT::getMoveDuration(const Position& newPosition, double minTime)
{
    double actualTimes[6];
    for (int i = 0; i < 6; i++) {
        double displacement = abs(newPosition.get(i) - currentPosition.get(i));
        int maxSpeed = i >= 3 ? MAXIMUM_SPEED : BIG_JOINT_MAXIMUM_SPEED;

        // As fast as possible
        actualTimes[i] = displacement / maxSpeed;

        if (minTime > 0)
        {
            double speed = displacement / minTime;
            if (speed <= maxSpeed)
            {
                // Can complete movement in at least minTime seconds
                actualTimes[i] = minTime;
            }
        }
    }

    double actualTime = 0;
    for (int i = 0; i < 6; i++) {
        actualTime = max(actualTime, actualTimes[i]);
    }
    return actualTime;
}

void _BraccioPT::softStart() {
    long int startTime = millis();
    while (millis() - startTime < SOFT_START_TIME)
    {
        digitalWrite(SOFT_START_CONTROL_PIN, LOW);
        delayMicroseconds(450);
        digitalWrite(SOFT_START_CONTROL_PIN, HIGH);
        delayMicroseconds(20); 
    }
}