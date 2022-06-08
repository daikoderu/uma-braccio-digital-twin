/*
    File: CommandMonitor.cpp

    Copyright 2022 Daniel Pérez Porras, daniperezporras@gmail.com

    Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
    in compliance with the License. You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software distributed under the License
    is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied. See the License for the specific language governing permissions and
    limitations under the License.
*/

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