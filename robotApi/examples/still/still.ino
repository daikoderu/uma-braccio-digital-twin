#include <BraccioPT.h>
#include <CommandMonitor.h>

void setup()
{
    BraccioPT.init();
}

void loop()
{
    unsigned long ms = millis();
    CommandMonitor.loop(ms);
    BraccioPT.loop(ms);
}