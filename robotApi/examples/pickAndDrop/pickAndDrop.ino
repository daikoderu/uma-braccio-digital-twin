#include <BraccioPT.h>

Position startPos(90, 90, 180, 172, 90, GRIPPER_OPEN);
Position grabPos(90, 90, 180, 172, 90, GRIPPER_CLOSED);
Position liftPos(90, 55, 170, 86, 90, GRIPPER_CLOSED);
Position verticalPos(90, 90, 90, 90, 90, GRIPPER_CLOSED);
Position moveObjectPos(0, 55, 170, 86, 90, GRIPPER_CLOSED);
Position dropPos(0, 55, 170, 86, 90, GRIPPER_OPEN);

void setup() {
    BraccioPT.init();
    delay(2000);
}

void loop() {
    BraccioPT.moveToPosition(startPos, 2);
    delay(500);
    BraccioPT.moveToPosition(grabPos, 1);
    delay(500);
    BraccioPT.moveToPosition(liftPos, 2);
    delay(500);
    BraccioPT.moveToPosition(verticalPos, 2);
    delay(500);
    BraccioPT.moveToPosition(moveObjectPos, 2);
    delay(500);
    BraccioPT.moveToPosition(dropPos, 1);
    delay(500);
}