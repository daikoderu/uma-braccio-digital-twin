#include <SerialInput.h>
#include <CommandMonitor.h>
#include <BraccioPT.h>

void setup()
{
    BraccioPT.init();
}

void loop()
{
    unsigned long ms = millis();
    SerialInput.loop();
    CommandMonitor.loop(ms);
    BraccioPT.loop(ms);
}