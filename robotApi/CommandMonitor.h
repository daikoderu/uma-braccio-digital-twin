#include "Command.h"

#ifndef COMMAND_MONITOR_H
#define COMMAND_MONITOR_H

class _CommandMonitor
{

    public:

        // Function to be called in the Arduino loop() function, taking the current value of millis()
        void loop(unsigned long ms);

        _CommandMonitor();

    private:
        _SerialInput command;
        CommandHandler commandHandler;
        char *result;
        bool busy;

        void processCommands(unsigned long ms);
        
};

extern _CommandMonitor CommandMonitor;

#endif // COMMAND_MONITOR_H