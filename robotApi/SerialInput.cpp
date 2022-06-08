/*
    File: SerialInput.cpp

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
#include "SerialInput.h"

_SerialInput SerialInput;

void _SerialInput::loop()
{
    if (Serial.available() > 0 && !fAvailable)
    {
        // Input received, tokenize and save it
        length = Serial.readBytesUntil('\n', buffer, INPUT_BUFFER_SIZE - 1);
        char* cur = buffer;
        argc = 0;
        bool insideWord = false;
        for (int i = 0; i < length; i++)
        {
            if (!insideWord && *cur != ' ' && argc < MAX_ARGS)
            {
                // New argument
                argv[argc] = cur;
                argc++;
            }
            insideWord = *cur != ' ';
            if (!insideWord)
            {
                *cur = '\0';
            }
            cur++;
        }
        *cur = '\0';
        fAvailable = true;
    }
}

_SerialInput::_SerialInput()
{
    length = 0;
    argc = 0;
    fAvailable = false;
}

void _SerialInput::copy(_SerialInput *dest)
{
    dest->length = length;
    dest->argc = argc;
    dest->fAvailable = fAvailable;
    
    // Copy bytes
    memcpy(dest->buffer, buffer, INPUT_BUFFER_SIZE);

    // Recalculate pointers
    for (int i = 0; i < argc; i++)
    {
        dest->argv[i] = argv[i] + (dest->buffer - buffer);
    }
}

void _SerialInput::consume()
{
    fAvailable = false;
}