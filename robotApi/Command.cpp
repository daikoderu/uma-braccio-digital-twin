#include <Arduino.h>
#include "Command.h"


char ok[3] = {'o', 'k', '\0'};
char error[6] = {'e', 'r', 'r', 'o', 'r', '\0'};

char* handleMovetoImmediate(_SerialInput *input, _BraccioPT *robot, unsigned long ms)
{
    if (input->getArgumentCount() != 8)
    {
        return error;
    }
    Position myPosition;
    for (int i = 0; i < 6; i++)
    {
        char *err;
        long int angle = strtol(input->getArgument(i + 2), &err, 10);
        if (*err || angle < minAngles[i] || angle > maxAngles[i])
        {
            return error;
        }
        myPosition.set(i, (int)angle);
    }
    robot->moveToPosition(myPosition, 0);
    return ok;
}

char* handleMoveto(_SerialInput *input, _BraccioPT *robot, unsigned long ms)
{
    return ok;
}


unsigned long int frozenUntil;


char* handleFreezeImmediate(_SerialInput *input, _BraccioPT *robot, unsigned long ms)
{
    // Validate arguments
    if (input->getArgumentCount() != 3)
    {
        return error;
    }
    char *err;
    long int freezeTime = strtol(input->getArgument(2), &err, 10);
    if (*err || freezeTime < 0)
    {
        return error;
    }
    frozenUntil = ms + freezeTime;

    // Save the current state of the robot
    robot->setFrozen(true);
    return NULL;
}

char* handleFreeze(_SerialInput *input, _BraccioPT *robot, unsigned long ms)
{
    if (ms >= frozenUntil)
    {
        robot->setFrozen(false);
        return ok;
    }
    else
    {
        return NULL;
    }
}

CommandType commandList[] = {
    {"moveto", handleMovetoImmediate, handleMoveto},
    {"freeze", handleFreezeImmediate, handleFreeze},
    {NULL, NULL}
};