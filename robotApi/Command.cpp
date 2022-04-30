#include <Arduino.h>
#include <BraccioPT.h>
#include "Command.h"

const char* handleMoveto(Command command, _BraccioPT *robot)
{
    if (command.argc != 7)
    {
        return "error";
    }
    Position myPosition;
    for (int i = 1; i <= 6; i++)
    {
        long int angle = strtol(command.argv[i], NULL, 10);
        myPosition.set(i - 1, (int)angle);
    }
    robot->moveToPosition(myPosition, 0);
    return "ok";
}

CommandType commandList[] = {
    {"moveto", handleMoveto},
    {NULL, NULL}
};