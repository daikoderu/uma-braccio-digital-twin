package api;

/**
 * Class to encapsulate the robot's speeds in degrees per second.
 *
 * Servo 1 controls "base", with values from 0 to 180.
 * Servo 2 controls "shoulder", with values from 15 to 165.
 * Servo 3 controls "elbow", with values from 0 to 180.
 * Servo 4 controls "wrist", with values from 0 to 180.
 * Servo 5 controls "wristRotation", with values from 0 to 180.
 * Servo 6 controls "gripper", with values from 10 (open) to 73 (closed).
 */
@SuppressWarnings("unused")
public class ServoVector {

    private double base, shoulder, elbow, wrist, wristRotation, gripper;

    /**
     * Base constructor.
     * @param base The speed for the base jodouble, with values from 0 to 180.
     * @param shoulder The speed for the shoulder jodouble, with values from 15 to 165.
     * @param elbow The speed for the elbow jodouble, with values from 0 to 180.
     * @param wrist The speed for the wrist vertical jodouble, with values from 0 to 180.
     * @param wristRotation The wrist angular velocity, with values from 0 to 180.
     * @param gripper The gripper position, with values from 10 (open) to 73 (closed).
     */
    public ServoVector(double base, double shoulder, double elbow, double wrist, double wristRotation, double gripper) {
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
    public ServoVector(Position position) {
        this(position.getBase(), position.getShoulder(), position.getElbow(),
                position.getWrist(), position.getWristRotation(), position.getGripper());
    }

    /**
     * Default constructor.
     */
    public ServoVector() {
        this(0, 0, 0, 0, 0, 0);
    }

    public double getBase() {
        return base;
    }

    public void setBase(double base) {
        this.base = base;
    }

    public double getShoulder() {
        return shoulder;
    }

    public void setShoulder(double shoulder) {
        this.shoulder = shoulder;
    }

    public double getElbow() {
        return elbow;
    }

    public void setElbow(double elbow) {
        this.elbow = elbow;
    }

    public double getWrist() {
        return wrist;
    }

    public void setWrist(double wrist) {
        this.wrist = wrist;
    }

    public double getWristRotation() {
        return wristRotation;
    }

    public void setWristRotation(double wristRotation) {
        this.wristRotation = wristRotation;
    }

    public double getGripper() {
        return gripper;
    }

    public void setGripper(double gripper) {
        this.gripper = gripper;
    }
    
}
