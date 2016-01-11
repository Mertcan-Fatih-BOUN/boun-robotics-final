package task;

import lejos.hardware.Sound;
import out.LCDController;
import out.PCOutController;

import static actuator.MovementController.*;
import static robo.Main.*;
import static sensor.UltrasonicReader.*;

import actuator.MovementController;

public class EntranceTask implements Runnable {
	int EDGE = -1;
	double DISTANCE = -1;
	int DIRECTION = 0;

	public void run() {
		LCDController.print("entrance");
		MovementController.reset();
		Sound.playTone(440, 100, 10);

		double left_distance = readLeft();
		double right_ditance = readRight();

		if (left_distance < right_ditance)
			DIRECTION = -1;
		else
			DIRECTION = 1;

		while (CURRENT_PHASE == PHASE_ENTRANCE & !IS_INTERRUPTED) {
			double traveledDistance = travelUntilNoneWall();

			// Encounter wall
			if (traveledDistance == -1) {
				DISTANCE = readForward();
				rotate_back();
				DIRECTION *= -1;
				PCOutController.write("Wall encountered Distance: " + DIRECTION);
				PCOutController.write("New Direction is: " + DIRECTION);
			} else {
				// Know where it is
				if (DISTANCE != -1) {
					DISTANCE += traveledDistance;
					PCOutController.write("Distance: " + DISTANCE);
					if (DISTANCE > 66 && DISTANCE < 132 & !IS_INTERRUPTED) {
						enterEntrance();
					} else
						rotateCorner();
				}
				// Not sure
				else {
					boolean isEntrance = isEntranceByControllForward();
					if (isEntrance)
						enterEntrance();
					else
						rotateCorner();
				}
			}
		}
	}

	private void rotateCorner() {
		if (IS_INTERRUPTED)
			return;

		goStraight(20);
		PCOutController.write("Starting to rotate corner");

		if (DIRECTION == 1)
			rotate_right();
		else
			rotate_left();

		double d = 1000;
		while (d > 50 & !IS_INTERRUPTED) {
			goStraight(10);
			if (DIRECTION == 1)
				d = readRight();
			else
				d = readLeft();
		}
		DISTANCE = 0;

	}

	private void enterEntrance() {
		if (IS_INTERRUPTED)
			return;

		int count = 0;
		PCOutController.write("Starting to enter");
		if (DIRECTION == 1)
			rotate_right();
		else
			rotate_left();

		double d = 1000;

		while (true & !IS_INTERRUPTED) {
			goStraight(10);
			d = readLeft() + readRight();
			if (d < 250)
				count++;
			if (count > 2) {
				CURRENT_PHASE = -1;
				break;
			}
		}
		Sound.playTone(440, 100, 10);
	}

	private boolean isEntranceByControllForward() {
		if (IS_INTERRUPTED)
			return false;

		boolean isEntrance = false;
		goStraight(40);
		double d;
		if (DIRECTION == 1)
			d = readRight();
		else
			d = readLeft();

		if (d < 50)
			isEntrance = true;
		else
			isEntrance = false;
		goStraight(-40);
		return isEntrance;

	}

	private float travelUntilNoneWall() {
		if (IS_INTERRUPTED)
			return -1;

		int count = 0;
		float traveled_distance = 0;
		double wall_distance = 10000;
		while (wall_distance > 30 & !IS_INTERRUPTED) {
			goStraight(10);
			traveled_distance += 10;
			PCOutController.write("Distance: " + DISTANCE + traveled_distance);
			float f;
			if (DIRECTION == 1)
				f = readRight();
			else
				f = readLeft();
			if (f > 50) {
				count++;
				if (count > 1)
					return traveled_distance;
			}
			wall_distance = readForward();
		}
		return -1;
	}
}
