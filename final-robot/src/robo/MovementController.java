package robo;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.Color;
import lejos.robotics.ColorAdapter;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.utility.Delay;
import lejos.utility.PilotProps;
import out.LCDController;

import static robo.ConstantsVariables.*;

import java.awt.Point;

public class MovementController {
	private static int X = 0;
	private static int Y = 0;
	public static int HEADING = 0;

	private static EV3GyroSensor gyroSensor;
	private static DifferentialPilot pilot;
	private static SampleProvider sampleProviderGyro;
	private static ColorAdapter colorAdapter;

	public static void initialize(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			EV3GyroSensor gyro, ColorAdapter ca) {
		pilot = new DifferentialPilot(5.5, 5.5, 12.1, leftMotor, rightMotor, false);
		pilot.setTravelSpeed(100);
		pilot.setAcceleration(50);
		gyroSensor = gyro;
		sampleProviderGyro = gyroSensor.getAngleMode();
		colorAdapter = ca;
	}

	public static void gyroReset() {
		gyroSensor.reset();
	}
	
	private static int step = 20;

	public static void goStraight(int distance) {
		if (IS_INTERRUPTED)
			return;

		int _dir = 0;
		if (distance < 0)
			_dir = -1;
		else
			_dir = 1;
		int abs_distance = distance / _dir;

		if (abs_distance <= step) {
			goStraight_(_dir * abs_distance);
		} else {
			goStraight_(_dir * step);
			goStraight(_dir * (abs_distance - step));
		}
	}

	public static void goStraight11(int distance) {
		if (IS_INTERRUPTED)
			return;

		int _dir = 0;
		if (distance < 0)
			_dir = -1;
		else
			_dir = 1;
		int abs_distance = distance / _dir;

		if (abs_distance <= 11) {
			goStraight_(_dir * abs_distance);
		} else {
			goStraight_(_dir * 11);
			goStraight11(_dir * (abs_distance - 11));
		}
	}

	private static void goStraight_(int distance) {
		if (IS_INTERRUPTED)
			return;

		float difference = HEADING - readAngle();
		rotate_exact(difference);
		pilot.travel(distance);
		difference = HEADING - readAngle();
		rotate_exact(difference);

		switch (normalize(HEADING)) {
		case -90:
			X -= distance;
			break;
		case 0:
			Y += distance;
			break;
		case 90:
			X += distance;
			break;
		case 180:
			Y -= distance;
			break;
		default:
			break;
		}

		if (CURRENT_PHASE == PHASE_MAPPING) {
			Color c = colorAdapter.getColor();
			LCDController.print("r: " + c.getRed() + "\ng: " + c.getGreen() + "\nb: " + c.getBlue());

			if (c.getRed() > 7 && c.getBlue() < 7 && c.getGreen() < 7) {
				red.add(new Point(X, Y));
				Sound.playTone(440, 100, 10);
				Sound.beepSequenceUp();
			}
			if (c.getRed() < 7 && c.getBlue() < 7 && c.getGreen() > 7) {

				green.add(new Point(X, Y));
				Sound.beepSequence();
			}
		}

	}

	public static void rotate_left() {
		if (IS_INTERRUPTED)
			return;

		float _goal = HEADING + 90;
		rotate_exact_to(_goal);
		HEADING = HEADING + 90;
	}

	public static void rotate_right() {
		if (IS_INTERRUPTED)
			return;

		float _goal = HEADING - 90;
		rotate_exact_to(_goal);
		HEADING = HEADING - 90;
	}

	public static void rotate_back() {
		if (IS_INTERRUPTED)
			return;

		float _goal = HEADING - 180;
		rotate_exact_to(_goal);
		HEADING = HEADING - 180;
	}

	private static void rotate_exact_to(float goal) {
		if (IS_INTERRUPTED)
			return;

		float diff = goal - readAngle();
		while (Math.abs(diff) > 0.5 && IS_INTERRUPTED) {
			pilot.rotate(diff);
			diff = goal - readAngle();
		}
	}

	public static void rotate_exact(float degree) {
		if (IS_INTERRUPTED)
			return;

		float _degree = readAngle();
		float _goal = _degree + degree;
		pilot.rotate(degree);
		float read_angle = readAngle();
		if (read_angle <= _goal - 0.5 || read_angle >= _goal + 0.5) {
			rotate_exact(_goal - read_angle);
		}
	}

	public static void goToGrid(int mx, int my) {
		goTo(mx * 33 + 15, my * 33 + 15);
		// goTo(mx * 10, my * 10);
	}

	public static void goTo(int mx, int my) {
		if (IS_INTERRUPTED)
			return;

		switch ((int) normalize(HEADING)) {
		case -90:
			goStraight(X - mx);
			if (my > Y)
				rotate_left();
			else if (my < Y)
				rotate_right();
			goStraight(Math.abs(my - Y));
			break;
		case 0:
			goStraight(my - Y);
			if (mx > X)
				rotate_left();
			else if (mx < X)
				rotate_right();
			goStraight(Math.abs(mx - X));
			break;
		case 90:
			goStraight(mx - X);
			if (my > Y)
				rotate_right();
			else if (my < Y)
				rotate_left();
			goStraight(Math.abs(my - Y));
			break;
		case 180:
			goStraight(Y - my);
			if (mx > X)
				rotate_right();
			else if (mx < X)
				rotate_left();
			goStraight(Math.abs(mx - X));
			break;
		default:
			break;
		}

	}

	private static float readAngle() {
		float[] sample = new float[sampleProviderGyro.sampleSize()];
		sampleProviderGyro.fetchSample(sample, 0);
		float angle = sample[0];
		Main.send_to_pc(ConstantsVariables.LOG_ANGLE, angle);
		return angle;
	}

	public static int normalize(float angle) {
		float a = angle;
		while (a > 180)
			a -= 360;
		while (a < -180)
			a += 360;
		if (a == -180)
			a = 180;
		return (int) a;
	}

	public static void reset() {
		X = 0;
		Y = 0;
		HEADING = 0;
		gyroSensor.reset();
		Delay.msDelay(500);
		Thread.yield();
	}

	public static void setXY(float mx, float my) {
		X = (int) mx;
		Y = (int) my;
	}

	public static void setX(float mx) {
		X = (int) mx;
	}

	public static void setY(float my) {
		Y = (int) my;
	}

	public static int getX() {
		return X;
	}

	public static int getY() {
		return Y;
	}

}
