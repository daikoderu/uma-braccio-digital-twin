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

CommandType commandList[] = {
    {"moveto", handleMovetoImmediate, handleMoveto},
    {NULL, NULL}
};