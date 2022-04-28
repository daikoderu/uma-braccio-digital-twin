#include <BraccioPT.h>

Position positions[] = {
    Position(90, 135, 0, 180, 90, GRIPPER_CLOSED),
    Position(90, 45, 180, 0, 90, GRIPPER_CLOSED),
};

void loopSequence(unsigned long ms)
{
    static int i = 0;
    if (!BraccioPT.isMoving())
    {
        BraccioPT.moveToPosition(positions[i], 2);
        i = (i + 1) % 2;
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
    BraccioPT.loop(ms);
    loopSequence(ms);
}