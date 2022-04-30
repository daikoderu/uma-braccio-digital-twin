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

#include <Arduino.h>
#include "BraccioPT.h"

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

    currentPosition[0] = base.read();
    currentPosition[1] = shoulder.read();
    currentPosition[2] = elbow.read();
    currentPosition[3] = wrist.read();
    currentPosition[4] = wristRotation.read();
    currentPosition[5] = gripper.read();
    moveToPosition(startPosition, INIT_TIME_SECONDS);

    if (doSoftStart)
    {
        softStart();
    }
    nextMs = 0;

    // Initialize serial port
    Serial.begin(baudRate);

}

void _BraccioPT::loop(unsigned long ms)
{
    handleMovement(ms);
    generateSnapshots(ms);
}

void _BraccioPT::moveToPosition(const Position& newPosition, float minTime)
{
    float time = getMoveDuration(newPosition, minTime);
    for (int i = 0; i < 6; i++)
    {
        float displacement = abs(newPosition.get(i) - currentPosition[i]);
        targetPosition[i] = newPosition.get(i);
        currentSpeeds[i] = displacement / time;
    }
}

float _BraccioPT::getMoveDuration(const Position& newPosition, float minTime)
{
    float actualTimes[6];
    for (int i = 0; i < 6; i++)
    {
        float displacement = abs(newPosition.get(i) - currentPosition[i]);
        int maxSpeed = i >= 3 ? MAXIMUM_SPEED : BIG_JOINT_MAXIMUM_SPEED;

        // As fast as possible
        actualTimes[i] = displacement / maxSpeed;

        if (minTime > 0)
        {
            float speed = displacement / minTime;
            if (speed <= maxSpeed)
            {
                // Can complete movement in at least minTime seconds
                actualTimes[i] = minTime;
            }
        }
    }

    float actualTime = 0;
    for (int i = 0; i < 6; i++) {
        actualTime = max(actualTime, actualTimes[i]);
    }
    return actualTime;
}

bool _BraccioPT::isMoving()
{
    for (int i = 0; i < 6; i++)
    {
        if (currentSpeeds[i] > 0)
        {
            return true;
        }
    }
    return false;
}

void _BraccioPT::softStart()
{
    long int startTime = millis();
    while (millis() - startTime < SOFT_START_TIME)
    {
        digitalWrite(SOFT_START_CONTROL_PIN, LOW);
        delayMicroseconds(450);
        digitalWrite(SOFT_START_CONTROL_PIN, HIGH);
        delayMicroseconds(20); 
    }
}

void _BraccioPT::handleMovement(unsigned long ms)
{
    while (ms >= nextMs)
    {
        if (isMoving())
        {
            for (int i = 0; i < 6; i++)
            {
                float step = currentSpeeds[i] * STEP_DELAY_MS / MS_PER_S;
                if (currentPosition[i] < targetPosition[i])
                {
                    currentPosition[i] = min(currentPosition[i] + step, targetPosition[i]);
                }
                else if (currentPosition[i] > targetPosition[i])
                {
                    currentPosition[i] = max(currentPosition[i] - step, targetPosition[i]);
                }
                else
                {
                    currentSpeeds[i] = 0;
                }
            }
            
            // Write position to servos
            base.write(int(currentPosition[0]));
            shoulder.write(int(currentPosition[1]));
            elbow.write(int(currentPosition[2]));
            wrist.write(int(currentPosition[3]));
            wristRotation.write(int(currentPosition[4]));
            gripper.write(int(currentPosition[5]));
        }
        nextMs += STEP_DELAY_MS;
    }
}

void _BraccioPT::generateSnapshots(unsigned long ms)
{
}