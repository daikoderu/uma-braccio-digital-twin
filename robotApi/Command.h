/*
    File: Command.h

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
#include "BraccioPT.h"
#include "SerialInput.h"

#ifndef COMMAND_H
#define COMMAND_H

typedef char* (*CommandHandler)(
    _SerialInput *input,
    _BraccioPT *robot,
    unsigned long ms
);

struct _CommandType
{
    const char *name;
    CommandHandler executeImmediately;
    CommandHandler execute;
};
typedef struct _CommandType CommandType;

extern CommandType commandList[];

#endif // COMMAND_H
