/* CONFIGURATION FILE FOR THE USE DIGITAL TWIN
 ******************************************************************************/

// Delay between robot movement steps
#define STEP_DELAY_MS 10

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