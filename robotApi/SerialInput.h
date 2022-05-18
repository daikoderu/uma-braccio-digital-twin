#include <Arduino.h>

#ifndef SERIAL_INPUT_H
#define SERIAL_INPUT_H

#define INPUT_BUFFER_SIZE 64
#define MAX_ARGS 8

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