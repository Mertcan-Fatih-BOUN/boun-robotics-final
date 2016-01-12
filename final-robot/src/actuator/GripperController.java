package actuator;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;

public class GripperController {

	private static EV3LargeRegulatedMotor gripperMotor;
	private static boolean isDown = false;

	public static void initialize(Port port) {
		gripperMotor = new EV3LargeRegulatedMotor(port);
	}

	public static void up() {
		if (!isDown)
			return;
		gripperMotor.rotate(90);
		isDown = false;
	}

	public static void down() {
		if (isDown)
			return;
		gripperMotor.rotate(-90);
		isDown = true;
	}

	public static boolean isDown() {
		return isDown;
	}
}
