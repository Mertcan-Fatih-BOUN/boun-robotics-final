package task;

import lejos.hardware.Sound;
import out.LCDController;
import robo.Main;
import robo.MovementController;

import static robo.ConstantsVariables.*;
import static robo.MovementController.goStraight;
import static robo.MovementController.rotate_right;
import static robo.MovementController.setX;

import java.awt.Point;

import static robo.MovementController.*;

public class MappingTask implements Runnable {
	float l = 0;
	float r = 0;
	float f = 0;
	static int[][] grid = new int[6][6];

	public void run() {
		Sound.playTone(440, 100, 10);
		LCDController.print("mapping");

		MovementController.reset();

		// readLeft();
		// readRight();
		// while (l + r < 1 & !IS_INTERRUPTED) {
		// goStraight(-3);
		// readLeft();
		// readRight();
		// }
		//
		// setX(r);
		//
		// rotate_right();
		// readRight();
		// while (r > 50) {
		// goStraight(5);
		// readRight();
		// }
		//
		// setY(r);

		setXY(0, 0);

//		goToGrid(1, 1);
//		LCDController.print(getX() + "\n" + getY());
//		readRight();
//		if(f < 30){
//			grid[0][1] = 1;
//		}else{
//			grid[0][1] = 0;
//		}
//		for(int i = 2; i < 5; i++){
//			goToGrid(1, i);
//			readRight();
//			if(r < 30){
//				grid[0][i] = 1;
//			}else{
//				grid[0][i] = 0;
//			}
//		}
//		rotate_left();
//		readRight();
//		if(r < 30){
//			grid[1][5] = 1;
//		}else{
//			grid[1][5] = 0;
//		}
//		for(int i = 2; i < 5; i++){
//			goToGrid(4, i);
//			readRight();
//			if(r < 30){
//				grid[i][5] = 1;
//			}else{
//				grid[i][5] = 0;
//			}
//		}
//		LCDController.print(getX() + "\n" + getY());
////		goToGrid(4, 4);
////		LCDController.print(getX() + "\n" + getY());
//		rotate_left();
//		readRight();
//		if(r < 30){
//			grid[5][4] = 1;
//		}else{
//			grid[5][4] = 0;
//		}
//		for(int i = 3; i > 0; i--){
//			goToGrid(4, i);
//			readRight();
//			if(r < 30){
//				grid[5][i] = 1;
//			}else{
//				grid[5][i] = 0;
//			}
//		}
////		goToGrid(4, 1);
////		LCDController.print(getX() + "\n" + getY());
//		goToGrid(2, 1);
//		LCDController.print(getX() + "\n" + getY());
//		goToGrid(2, 3);
//		LCDController.print(getX() + "\n" + getY());
//		goToGrid(3, 3);
//		LCDController.print(getX() + "\n" + getY());
//		goToGrid(3, 2);
//		LCDController.print(getX() + "\n" + getY());
//
		
//		EXECUTION PART STARTS HERE
//		Point red = getRedAverage();
//		Point green = getGreenAverage();
		Point red = new Point();
		Point green = new Point();
//		goTo(red.x, red.y);
		goTo(10, 10);
		red.x = 10; red.y = 10;
		green.x = 30; green.y = 10;
		if(green.x <= red.x + 1 && green.x >= red.x - 1){
			while(normalize(HEADING) != 0)
				rotate_right();
			goStraight((int)(green.y - red.y));
		}else if(green.y <= red.y + 1 && green.y >= red.y - 1){
			while(normalize(HEADING) != 90)
				rotate_right();
			goStraight((int)(green.x - red.x));
		}else{
			int angle = (int)Math.toDegrees(Math.atan(((double)red.y - (double)green.y)/Math.abs(red.x - green.x)));		
			LCDController.print(angle + "\n");
			while(normalize(HEADING) != 0)
				rotate_right();
			if(green.x < red.x)
				rotate_exact(angle);
			else
				rotate_exact(-angle);
			gyroReset();
			goStraight((int)red.distance(green));
		}
		Sound.playTone(440, 100, 10);
	}

	/**
	 * Rotates
	 */
	public void rotate() {
		// TODO Auto-generated method stub
		float f = Main.readForward();
		float l = Main.readLeft();
		float r = Main.readRight();
		LCDController.print(f + "\n" + l + "\n" + r);
	}

	private void readLeft() {
		l = Main.readLeft();
	}

	private void readRight() {
		r = Main.readRight();

	}

	private void readForward() {
		f = Main.readForward();

	}
}
