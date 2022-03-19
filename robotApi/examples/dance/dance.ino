#include <BraccioPT.h>

Position frontPos(90, 135, 0, 180, 90, GRIPPER_CLOSED);
Position backPos(90, 45, 180, 0, 90, GRIPPER_CLOSED);

void setup() {
    BraccioPT.init();
    delay(2000);
}

void loop() {
    BraccioPT.moveToPosition(frontPos, 1);
    delay(500);
    BraccioPT.moveToPosition(backPos, 1);
    delay(500);
}