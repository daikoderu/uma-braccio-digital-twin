#include <string.h>
#include "CommandMonitor.h"

_CommandMonitor CommandMonitor;

_CommandMonitor::_CommandMonitor()
{
    busy = false;
}

void _CommandMonitor::loop(unsigned long ms)
{
    processCommands(ms);
}

void _CommandMonitor::processCommands(unsigned long ms)
{
    if (!busy)
    {
        // Find the appropiate handler
        if (SerialInput.available() && SerialInput.getLength() >= 2
                && !strcmp("COM", SerialInput.getArgument(0)))
        {
            commandHandler = NULL;
            {
                int i = 0;
                CommandType ct = commandList[0];
                while (ct.name != NULL)
                {
                    if (!strcmp(ct.name, SerialInput.getArgument(1)))
                    {
                        commandHandler = ct.handler;
                        break;
                    }
                    i++;
                    ct = commandList[i];
                }
            }
            if (commandHandler != NULL)
            {
                busy = true;
                SerialInput.copy(&command);
                result = NULL;
            }
            else
            {
                Serial.println("RET invalid-command");
            }
            SerialInput.consume();
        }
    }
    else
    {
        // Executing a command
        const char *result = commandHandler(&command, &BraccioPT, ms);
        if (result != NULL)
        {
            busy = false;
            Serial.print("RET ");
            Serial.println(result);
        }
    }
    
}