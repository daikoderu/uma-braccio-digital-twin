#include <SerialInput.h>
#include <CommandMonitor.h>
#include <BraccioPT.h>

void setup()
{
    BraccioPT.init();
}

void loop()
{
    SerialInput.loop();
    CommandMonitor.loop();
    BraccioPT.loop();
}