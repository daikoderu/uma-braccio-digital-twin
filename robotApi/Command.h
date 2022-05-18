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
