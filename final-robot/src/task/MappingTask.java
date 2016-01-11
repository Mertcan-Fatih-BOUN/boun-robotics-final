package task;

import lejos.hardware.Sound;
import out.LCDController;
import out.PCOutController;
import sensor.ColorReader;
import sensor.UltrasonicReader;

import static actuator.MovementController.*;

import actuator.MovementController;
import static robo.Main.IS_INTERRUPTED;

import java.util.Random;

public class MappingTask implements Runnable {
	float l = 0;
	float r = 0;
	float f = 0;
	static int[][] grid = new int[6][6];

	public void run() {
		Sound.playTone(440, 100, 10);
		LCDController.print("mapping");

		MovementController.reset();

		readLeft();
		readRight();
		while (l + r < 180 & !IS_INTERRUPTED) {
			goStraight(-3);
			readLeft();
			readRight();
		}

		setX(r);

		rotate_right();
		readRight();
		while (r > 50 && !IS_INTERRUPTED) {
			goStraight(5);
			readRight();
		}
		readForward();

		setY(r);

		goToGrid(1, 1);
		LCDController.print(getX() + "\n" + getY());
		readRight();
		if (r < 30) {
			fillGrid(0, 1, 1);
		} else {
			fillGrid(0, 1, 0);
		}
		for (int i = 2; i < 5; i++) {
			goToGrid(1, i);
			readRight();
			if (r < 30) {
				fillGrid(0, i, 1);
			} else {
				fillGrid(0, i, 0);
			}
		}
		readForward();
		if (f < 30) {
			fillGrid(1, 5, 1);
		} else {
			fillGrid(1, 5, 0);
		}
		for (int i = 2; i < 5; i++) {
			goToGrid(i, 4);
			readRight();
			if (r < 30) {
				fillGrid(i, 5, 1);
			} else {
				fillGrid(i, 5, 0);
			}
		}
		LCDController.print(getX() + "\n" + getY());

		readForward();
		if (f < 30) {
			fillGrid(5, 4, 1);
		} else {
			fillGrid(5, 4, 0);
		}
		for (int i = 3; i > 0; i--) {
			goToGrid(4, i);
			readRight();
			if (r < 30) {
				fillGrid(5, i, 1);
			} else {
				fillGrid(5, i, 0);
			}
		}

		for (int i = 0; i < 1; i++) {
			if (ColorReader.allFound() || IS_INTERRUPTED)
				break;
			goToGrid(2, 1);
			if (ColorReader.allFound() || IS_INTERRUPTED)
				break;
			goToGrid(2, 3);
			if (ColorReader.allFound() || IS_INTERRUPTED)
				break;
			goToGrid(3, 3);
			if (ColorReader.allFound() || IS_INTERRUPTED)
				break;
			goToGrid(3, 2);
		}

		for (int i = 0; i < 1; i++) {
			if (ColorReader.allFound() || IS_INTERRUPTED)
				break;
			rotate_back();
			if (ColorReader.allFound() || IS_INTERRUPTED)
				break;
			goToGrid(3, 3);
			if (ColorReader.allFound() || IS_INTERRUPTED)
				break;
			goToGrid(2, 3);
			if (ColorReader.allFound() || IS_INTERRUPTED)
				break;
			goToGrid(2, 1);
			if (ColorReader.allFound() || IS_INTERRUPTED)
				break;
			goToGrid(4, 1);
			if (ColorReader.allFound() || IS_INTERRUPTED)
				break;
			goToGrid(4, 4);
			if (ColorReader.allFound() || IS_INTERRUPTED)
				break;
			goToGrid(1, 4);
			if (ColorReader.allFound() || IS_INTERRUPTED)
				break;
			goToGrid(1, 1);
		}

		Random generator = new Random();
		while (!ColorReader.allFound() && !IS_INTERRUPTED) {
			int i = generator.nextInt(3) + 1;
			int j = generator.nextInt(3) + 1;
			goToGrid(i, j);
		}

		MovementController.writeToFile();
		ColorReader.writeToFile();
		Sound.playTone(440, 100, 10);
	}

	/**
	 * Rotates
	 */
	public void rotate() {
		float f = UltrasonicReader.readForward();
		float l = UltrasonicReader.readLeft();
		float r = UltrasonicReader.readRight();
		LCDController.print(f + "\n" + l + "\n" + r);
	}

	private void readLeft() {
		l = UltrasonicReader.readLeft();
	}

	private void readRight() {
		r = UltrasonicReader.readRight();

	}

	private void readForward() {
		f = UltrasonicReader.readForward();
	}

	private void fillGrid(int x, int y, int fill) {
		grid[x][y] = fill;

		if (fill == 1) {
			Sound.buzz();
			PCOutController.write("There is obstacle: " + x + " " + y);
		} else {
			PCOutController.write("There is no obstacle: " + x + " " + y);
		}
	}
}
