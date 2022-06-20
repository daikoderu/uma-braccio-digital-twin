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

#include <Arduino.h>
#include <string.h>
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
    
    base.write(startPosition.getBase());
    shoulder.write(startPosition.getShoulder());
    elbow.write(startPosition.getElbow());
    wrist.write(startPosition.getWrist());
    wristRotation.write(startPosition.getWristRotation());
    gripper.write(startPosition.getGripper());
    
    for (int i = 0; i < 6; i++)
    {
        currentPosition[i] = startPosition.get(i);
        currentSpeeds[i] = 0;
    }
    targetPosition = startPosition;

    if (doSoftStart)
    {
        softStart();
    }
    nextMs = 0;
    nextSnapshotMs = 0;
    isFrozen = false;

    // Initialize serial port
    Serial.begin(baudRate);
    Serial.setTimeout(SERIAL_PORT_TIMEOUT);

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
        targetPosition.set(i, newPosition.get(i));
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

int _BraccioPT::readServo(int i)
{
    switch (i)
    {
        case 0: return base.read();
        case 1: return shoulder.read();
        case 2: return elbow.read();
        case 3: return wrist.read();
        case 4: return wristRotation.read();
        case 5: return gripper.read();
        default: return -1;
    }
}

void _BraccioPT::readAllServos(Position& dest)
{
    dest.set(
        base.read(),
        shoulder.read(),
        elbow.read(),
        wrist.read(),
        wristRotation.read(),
        gripper.read());
}

void _BraccioPT::setFrozen(bool isFrozen)
{
    this->isFrozen = isFrozen;
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
    if (ms >= nextMs)
    {
        if (!isFrozen && isMoving())
        {
            for (int i = 0; i < 6; i++)
            {
                float step = currentSpeeds[i] * STEP_DELAY_MS / MS_PER_S;
                if (currentPosition[i] < targetPosition.get(i))
                {
                    currentPosition[i] = min(currentPosition[i] + step, targetPosition.get(i));
                }
                else if (currentPosition[i] > targetPosition.get(i))
                {
                    currentPosition[i] = max(currentPosition[i] - step, targetPosition.get(i));
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
        nextMs = ms + STEP_DELAY_MS;
    }
}

void _BraccioPT::generateSnapshots(unsigned long ms)
{
    if (ms >= nextSnapshotMs)
    {
        Serial.print("OUT ");
        Serial.print(ms);
        Serial.print(':');

        for (int i = 0; i < 6; i++)
        {
            if (i > 0)
            {
                Serial.print(",");
            }
            Serial.print(readServo(i));
        }

        Serial.print(':');
        
        for (int i = 0; i < 6; i++)
        {
            if (i > 0)
            {
                Serial.print(",");
            }
            Serial.print(targetPosition.get(i));
        }

        Serial.print(':');

        for (int i = 0; i < 6; i++)
        {
            if (i > 0)
            {
                Serial.print(",");
            }
            Serial.print(currentSpeeds[i]);
        }

        Serial.println();
        nextSnapshotMs += SNAPSHOT_PERIOD_MS;
    }
}