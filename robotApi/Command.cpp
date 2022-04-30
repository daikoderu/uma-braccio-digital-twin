#include <Arduino.h>
#include "BraccioPT.h"
#include "Command.h"

const char* handleMoveto(Command command, _BraccioPT *robot)
{
    if (command.argc != 7)
    {
        return "error";
    }
    Position myPosition;
    for (int i = 0; i < 6; i++)
    {
        char *err;
        long int angle = strtol(command.argv[i + 1], &err, 10);
        if (*err || angle < minAngles[i] || angle > maxAngles[i])
        {
            return "error";
        }
        myPosition.set(i, (int)angle);
    }
    robot->moveToPosition(myPosition, 0);
    return "ok";
}

CommandType commandList[] = {
    {"moveto", handleMoveto},
    {NULL, NULL}
};