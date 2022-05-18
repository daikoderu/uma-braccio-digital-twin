#include <Arduino.h>
#include "Command.h"

const char* handleMoveto(_SerialInput *input, _BraccioPT *robot, unsigned long ms)
{
    if (input->getArgumentCount() != 8)
    {
        return "error";
    }
    Position myPosition;
    for (int i = 0; i < 6; i++)
    {
        char *err;
        Serial.print("DEBUG ");
        Serial.print(i);
        Serial.print(": ");
        Serial.println(input->getArgument(i + 2));
        long int angle = strtol(input->getArgument(i + 2), &err, 10);
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