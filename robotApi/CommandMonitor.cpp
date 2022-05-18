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
        // Receiving a new command
        if (SerialInput.available() && SerialInput.getLength() >= 2
                && !strcmp("COM", SerialInput.getArgument(0)))
        {
            SerialInput.consume();
            commandHandler = NULL;
            
            // Find the appropiate handler
            int i = 0;
            CommandType ct = commandList[0];
            while (ct.name != NULL)
            {
                if (!strcmp(ct.name, SerialInput.getArgument(1)))
                {
                    commandHandler = ct.execute;
                    break;
                }
                i++;
                ct = commandList[i];
            }

            if (commandHandler != NULL)
            {
                SerialInput.copy(&command);
                result = ct.executeImmediately(&command, &BraccioPT, ms);
                busy = result == NULL;
                if (!busy)
                {
                    // Command finished immediately
                    Serial.print("RET ");
                    Serial.println(result);
                }
            }
            else
            {
                Serial.println("RET invalid-command");
            }
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