/*
    File: SerialInput.h

    Copyright 2022 Daniel Pérez Porras, daniperezporras@gmail.com

    Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
    in compliance with the License. You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software distributed under the License
    is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied. See the License for the specific language governing permissions and
    limitations under the License.
*/

#include <Arduino.h>

#ifndef SERIAL_INPUT_H
#define SERIAL_INPUT_H

#include "Config.h"

class _SerialInput
{

    public:

        // Function to be called in the Arduino loop() function
        void loop();

        inline size_t getLength() const { return length; }
        inline char* getArgument(int i) const { return argv[i]; }
        inline size_t getArgumentCount() const { return argc; }

        _SerialInput();

        void copy(_SerialInput *dest);

        // Returns whether a new command is available and can be processed
        inline bool available() const { return fAvailable; }

        // "Consumes" the command so other tasks do not process it
        void consume();

    private:
        char buffer[INPUT_BUFFER_SIZE];
        size_t length;
        char *argv[MAX_ARGS];
        size_t argc;
        bool fAvailable;
        
};

extern _SerialInput SerialInput;

#endif // SERIAL_INPUT_H