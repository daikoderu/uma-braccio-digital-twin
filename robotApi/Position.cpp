/*
    File: Position.cpp

    Copyright 2018 Stefan Strömberg, stefangs@nethome.nu

    Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
    in compliance with the License. You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software distributed under the License
    is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied. See the License for the specific language governing permissions and
    limitations under the License.
*/

#include "Position.h"
#include "Arduino.h"

int minAngles[6] = {0, 15, 0, 0, 0, 10};
int maxAngles[6] = {180, 165, 180, 180, 180, 73};

Position&
Position::setBase(int b)
{
    base = limit(b, minAngles[0], maxAngles[0]);
    return *this;
}

Position&
Position::setShoulder(int s) {
    shoulder = limit(s, minAngles[1], maxAngles[1]);
    return *this;
}

Position&
Position::setElbow(int e)
{
    elbow = limit(e, minAngles[2], maxAngles[2]);
    return *this;
}

Position&
Position::setWrist(int v)
{
    wrist = limit(v, minAngles[3], maxAngles[3]);
    return *this;
}

Position& 
Position::setWristRotation(int w)
{
    wristRotation = limit(w, minAngles[4], maxAngles[4]);
    return *this;
}

Position&
Position::setGripper(int g)
{
    gripper = limit(g, minAngles[5], maxAngles[5]);
    return *this;
}

Position&
Position::set(int basePos, int shoulderPos, int elbowPos, int wristPos, int wristRotationPos, int gripperPos)
{
    setBase(basePos);
    setShoulder(shoulderPos);
    setElbow(elbowPos);
    setWrist(wristPos);
    setWristRotation(wristRotationPos);
    setGripper(gripperPos);
    return *this;
}

Position&
Position::set(int i, int pos)
{
    switch (i)
    {
        case 0: return setBase(pos);
        case 1: return setShoulder(pos);
        case 2: return setElbow(pos);
        case 3: return setWrist(pos);
        case 4: return setWristRotation(pos);
        case 5: return setGripper(pos);
    }
    return *this;
}

Position::Position()
{
    Position(90, 90, 90, 90, 90, 73);
}

Position::Position(int basePos, int shoulderPos, int elbowPos, int wristPos, int wristRotationPos, int gripperPos)
{
    set(basePos, shoulderPos, elbowPos, wristPos, wristRotationPos, gripperPos);
}

int
Position::get(int i) const
{
    switch (i)
    {
        case 0: return base;
        case 1: return shoulder;
        case 2: return elbow;
        case 3: return wrist;
        case 4: return wristRotation;
        case 5: return gripper;
        default: return -1;
    }
}

int 
Position::maxPositionDiff(const Position& p) const
{
    int maxDiff = 0;
    maxDiff = max(maxDiff, abs(base - p.base));
    maxDiff = max(maxDiff, abs(shoulder - p.shoulder));
    maxDiff = max(maxDiff, abs(elbow - p.elbow));
    maxDiff = max(maxDiff, abs(wrist - p.wrist));
    maxDiff = max(maxDiff, abs(wristRotation - p.wristRotation));
    maxDiff = max(maxDiff, abs(gripper - p.gripper));
    return maxDiff;
}

int
Position::setFromString(char* string)
{
    bool isSuccess = true;
    int speed;
    setBase(parseInt(string, isSuccess));
    setShoulder(parseInt(string, isSuccess));
    setElbow(parseInt(string, isSuccess));
    setWrist(parseInt(string, isSuccess));
    setWristRotation(parseInt(string, isSuccess));
    setGripper(parseInt(string, isSuccess));
    speed = parseInt(string, isSuccess);
    return isSuccess ? speed : -1;
}

Position& 
Position::operator=(const Position& p)
{
    base = p.base;
    shoulder = p.shoulder;
    elbow = p.elbow;
    wristRotation = p.wristRotation;
    wrist = p.wrist;
    gripper = p.gripper;
    return *this;
}

bool
Position::operator==(const Position& rhs)
{
    return base == rhs.base &&
        shoulder == rhs.shoulder &&
        elbow == rhs.elbow &&
        wristRotation == rhs.wristRotation &&
        wrist == rhs.wrist &&
        gripper == rhs.gripper;
}

bool
Position::operator!=(const Position& rhs)
{
    return !(*this == rhs);
}

int
Position::limit(int value, int minv, int maxv)
{
    int result;
    if (value < minv)
    {
        result = minv;
    }
    else if (value > maxv)
    {
        result = maxv;
    }
    else
    {
        result = value;
    }
    return result;
}

int
Position::parseInt(char *&in, bool &isSuccess)
{
    bool success = false;
    int result = 0;

    // Read past separator(s)
    while (((*in > '9') || (*in < '0')) && (*in != 0))
    {
        in++;
    }
    
    // Read integer
    while ((*in <= '9') && (*in >= '0'))
    {
        result *= 10;
        result += (*in - '0');
        in++;
        success = true;
    }
    isSuccess &= success;
    return result;
}