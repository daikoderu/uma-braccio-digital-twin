package api;

/**
 * Class to encapsulate the robot's positions, which are given by six servo angles in degrees:
 *
 * Servo 1 controls "base", with values from 0 to 180.
 * Servo 2 controls "shoulder", with values from 15 to 165.
 * Servo 3 controls "elbow", with values from 0 to 180.
 * Servo 4 controls "wrist", with values from 0 to 180.
 * Servo 5 controls "wristRotation", with values from 0 to 180.
 * Servo 6 controls "gripper", with values from 10 (open) to 73 (closed).
 */
@SuppressWarnings("unused")
public class Position {

    public static final int GRIPPER_OPEN = 10;
    public static final int GRIPPER_CLOSED = 73;

    private int base, shoulder, elbow, wrist, wristRotation, gripper;

    /**
     * Base constructor.
     * @param base The angle for the base joint, with values from 0 to 180.
     * @param shoulder The angle for the shoulder joint, with values from 15 to 165.
     * @param elbow The angle for the elbow joint, with values from 0 to 180.
     * @param wrist The angle for the wrist vertical joint, with values from 0 to 180.
     * @param wristRotation The wrist rotation, with values from 0 to 180.
     * @param gripper The gripper position, with values from 10 (open) to 73 (closed).
     */
    public Position(int base, int shoulder, int elbow, int wrist, int wristRotation, int gripper) {
        setBase(base);
        setShoulder(shoulder);
        setElbow(elbow);
        setWrist(wrist);
        setWristRotation(wristRotation);
        setGripper(gripper);
    }

    /**
     * Creates a copy of a given position.
     * @param position The position to copy.
     */
    public Position(Position position) {
        this(position.getBase(), position.getShoulder(), position.getElbow(),
                position.getWrist(), position.getWristRotation(), position.getGripper());
    }

    /**
     * Default constructor.
     */
    public Position() {
        this(90, 90, 90, 90, 90, GRIPPER_CLOSED);
    }

    public int getBase() {
        return base;
    }
    public void setBase(int base) {
        this.base = clamp(base, 0, 180);
    }
    public int getShoulder() {
        return shoulder;
    }
    public void setShoulder(int shoulder) {
        this.shoulder = clamp(shoulder, 15, 165);
    }
    public int getElbow() {
        return elbow;
    }
    public void setElbow(int elbow) {
        this.elbow = clamp(elbow, 0, 180);
    }
    public int getWrist() {
        return wrist;
    }
    public void setWrist(int wrist) {
        this.wrist = clamp(wrist, 0, 180);
    }
    public int getWristRotation() {
        return wristRotation;
    }
    public void setWristRotation(int wristRotation) {
        this.wristRotation = clamp(wristRotation, 0, 180);
    }
    public int getGripper() {
        return gripper;
    }
    public void setGripper(int gripper) {
        this.gripper = clamp(gripper, GRIPPER_OPEN, GRIPPER_CLOSED);
    }

    public int get(int index) {
        return switch (index) {
            case 0 -> getBase();
            case 1 -> getShoulder();
            case 2 -> getElbow();
            case 3 -> getWrist();
            case 4 -> getWristRotation();
            case 5 -> getGripper();
            default -> throw new IllegalArgumentException("Index must be between 0 and 5");
        };
    }

    public void set(int index, int value) {
        switch (index) {
            case 0 -> setBase(value);
            case 1 -> setShoulder(value);
            case 2 -> setElbow(value);
            case 3 -> setWrist(value);
            case 4 -> setWristRotation(value);
            case 5 -> setGripper(value);
            default -> throw new IllegalArgumentException("Index must be between 0 and 5");
        }
    }

    public String toString() {
        return "(" + base + ", " + shoulder + ", " + elbow + ", "
                + wrist + ", " + wristRotation + ", " + gripper +")";
    }

    private static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(val, max));
    }

}
