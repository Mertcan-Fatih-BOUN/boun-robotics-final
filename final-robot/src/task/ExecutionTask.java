package task;

import static robo.ConstantsVariables.IS_INTERRUPTED;
import static robo.ConstantsVariables.*;

import lejos.hardware.Sound;
import out.LCDController;
import robo.ConstantsVariables;
import robo.Main;
import static robo.MovementController.*;

import java.awt.Point;

public class ExecutionTask implements Runnable {

	public boolean isGrapped = false;

	public void run() {
		Sound.playTone(440, 100, 10);
		LCDController.print("execution");
		Point red = new Point();
		if (ConstantsVariables.red.size() > 0)
			red = getRedAverage();
		else {
			red.x = 66;
			red.y = 66;
		}
		Point green = new Point();
		if (ConstantsVariables.green.size() > 0)
			green = getGreenAverage();
		else {
			green.x = 99;
			green.y = 99;
		}
		LCDController.print("execution\n" + red.x + "\n" + red.y + "\n" + green.x + "\n" + green.y);
		goTo(red.x, red.y);
		Sound.playTone(440, 100, 10);

		while (!isGrapped && !IS_INTERRUPTED) {
		}

		// goTo(10, 10);
		// red.x = 10;
		// red.y = 10;
		// green.x = 30;
		// green.y = 10;
		// if (green.x <= red.x + 1 && green.x >= red.x - 1) {
		// while (normalize(HEADING) != 0)
		// rotate_right();
		// goStraight((int) (green.y - red.y));
		// } else if (green.y <= red.y + 1 && green.y >= red.y - 1) {
		// while (normalize(HEADING) != 90)
		// rotate_right();
		// goStraight((int) (green.x - red.x));
		// } else {
		// int angle = (int) Math
		// .toDegrees(Math.atan(((double) red.y - (double) green.y) /
		// Math.abs(red.x - green.x)));
		// LCDController.print(angle + "\n" + normalize(HEADING));
		// while (normalize(HEADING) != 0)
		// rotate_right();
		// if (green.x < red.x)
		// rotate_exact(angle);
		// else
		// rotate_exact(-angle);
		// gyroReset();
		// goStraight((int) red.distance(green));
		// }

		goTo(green.x, green.y);

		if (isGrapped) {
			Main.gripperMotor.rotate(90);
		}
	}

	public void grapBall() {
		Main.gripperMotor.rotate(-90);
		isGrapped = true;
	}

}
