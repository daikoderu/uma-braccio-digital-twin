#include <Arduino.h>
#include <BraccioPT.h>
#include "Command.h"

Position example(50, 90, 90, 90, 0, GRIPPER_OPEN);

const char* handleMoveto(Command command, _BraccioPT robot)
{
    robot.moveToPosition(example, 0);
    return "ok";
}

CommandType commandList[] = {
    {"moveto", handleMoveto},
    {NULL, NULL}
};