#include <Arduino.h>
#include <BraccioPT.h>

#ifndef COMMAND_H
#define COMMAND_H

#define COMMAND_BUFFER_SIZE 64
#define MAX_ARGS 8

struct _Command
{
    char buffer[COMMAND_BUFFER_SIZE];
    size_t commandSize;
    char *argv[MAX_ARGS];
    size_t argc;
    char *result;
};
typedef struct _Command Command;

typedef const char* (*CommandHandler)(
    Command command,
    _BraccioPT robot
);

struct _CommandType
{
    const char *name;
    CommandHandler handler;
};
typedef struct _CommandType CommandType;

extern CommandType commandList[];

#endif // COMMAND_H
