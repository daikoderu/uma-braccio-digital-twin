/*
    File: Config.h

    Copyright 2022 Daniel Pérez Porras, daniperezporras@gmail.com

    Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
    in compliance with the License. You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software distributed under the License
    is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied. See the License for the specific language governing permissions and
    limitations under the License.
*/

/* CONFIGURATION FILE FOR THE PHYSICAL TWIN
 ******************************************************************************/

// Delay between robot movement steps
#define STEP_DELAY_MS 10

// Serial port timeout
#define SERIAL_PORT_TIMEOUT 5

// Time to wait between snapshots
#define SNAPSHOT_PERIOD_MS 100

// Maximum speed in degrees per second
#define MAXIMUM_SPEED 200

// Maximum speed in degrees per second for the base and shoulder joints
#define BIG_JOINT_MAXIMUM_SPEED 140

// Serial port input buffer size
#define INPUT_BUFFER_SIZE 64

// Maximum number of arguments for commands (including the "COM" prefix
// and the command name, e.g. the command "freeze 1000" uses 3 arguments).
#define MAX_ARGS 8