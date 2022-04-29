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
        // Not executing a command right now
        if (Serial.available() > 0)
        {
            // Command received, tokenize and save it
            command.commandSize = Serial.readBytesUntil('\n', command.buffer, COMMAND_BUFFER_SIZE - 1);
            char* cur = command.buffer;
            command.argc = 0;
            bool insideWord = false;
            for (int i = 0; i < command.commandSize; i++)
            {
                if (!insideWord && *cur != ' ' && command.argc < MAX_ARGS)
                {
                    // New argument
                    command.argv[command.argc] = cur;
                    command.argc++;
                }
                insideWord = *cur != ' ';
                if (!insideWord)
                {
                    *cur = '\0';
                }
                cur++;
            }
            *cur = '\0';

            // Find the appropiate handler
            if (command.argc >= 1)
            {
                commandHandler = NULL;
                {
                    int i = 0;
                    CommandType ct = commandList[0];
                    while (ct.name != NULL)
                    {
                        if (!strcmp(ct.name, command.argv[0]))
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
                    command.result = NULL;
                }
                else
                {
                    Serial.println("invalid command");
                }
            }
        }
    }
    else
    {
        // Executing a command
        const char *result = commandHandler(command, BraccioPT);
        if (result != NULL)
        {
            busy = false;
            Serial.print("RET:");
            Serial.println(result);
        }
    }
    
}