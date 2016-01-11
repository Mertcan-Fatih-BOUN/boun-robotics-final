package task;

import static robo.Main.IS_INTERRUPTED;
import static actuator.MovementController.*;
import static sensor.ColorReader.*;
import lejos.hardware.Sound;
import out.LCDController;
import java.awt.Point;

import actuator.GripperController;
import actuator.MovementController;

public class ExecutionTask implements Runnable {

	public void run() {
		Sound.playTone(440, 100, 10);
		LCDController.print("execution");
		Point[] points = getColors();
		Point red = points[0];
		Point green = points[1];
		MovementController.readFromFile();

		LCDController.print("execution\n" + red.x + "\n" + red.y + "\n" + green.x + "\n" + green.y);
		goTo(red.x, red.y);
		Sound.playTone(440, 100, 10);

		while (!GripperController.isDown() && !IS_INTERRUPTED) {
		}

		goTo(green.x, green.y);

		GripperController.up();
	}

	public void grapBall() {
		GripperController.down();
	}

}
