/*
    File: CommandMonitor.h

    Copyright 2022 Daniel Pérez Porras, daniperezporras@gmail.com

    Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
    in compliance with the License. You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software distributed under the License
    is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied. See the License for the specific language governing permissions and
    limitations under the License.
*/

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