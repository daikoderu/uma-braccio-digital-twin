#include <BraccioPT.h>
#include <CommandMonitor.h>

Position positions[] = {
    Position(90, 90, 180, 172, 90, GRIPPER_OPEN),
    Position(90, 90, 180, 172, 90, GRIPPER_CLOSED),
    Position(90, 55, 170, 86, 90, GRIPPER_CLOSED),
    Position(90, 90, 90, 90, 90, GRIPPER_CLOSED),
    Position(0, 55, 170, 86, 90, GRIPPER_CLOSED),
    Position(0, 55, 170, 86, 90, GRIPPER_OPEN),
};

void loopSequence(unsigned long ms)
{
    static int i = 0;
    if (!BraccioPT.isMoving())
    {
        BraccioPT.moveToPosition(positions[i], 2);
        i = (i + 1) % 6;
    }
}

void setup()
{
    BraccioPT.init();
    delay(2000);
}

void loop()
{
    unsigned long ms = millis();
    CommandMonitor.loop(ms);
    BraccioPT.loop(ms);
    loopSequence(ms);
}