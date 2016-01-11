package actuator;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.utility.Delay;
import out.PCOutController;
import sensor.ColorReader;

import static robo.Main.*;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class MovementController {
	private static final int step = 10;

	private static int X = 0;
	private static int Y = 0;
	private static int HEADING = 0;

	private static EV3GyroSensor gyroSensor;
	private static DifferentialPilot pilot;
	private static SampleProvider sampleProviderGyro;

	public static void initialize(Port leftMotorPort, Port rightMotorPort, Port gyroPort) {
		EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(leftMotorPort);
		EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(rightMotorPort);
		gyroSensor = new EV3GyroSensor(gyroPort);
		sampleProviderGyro = gyroSensor.getAngleAndRateMode();

		pilot = new DifferentialPilot(5.5, 5.5, 12.1, leftMotor, rightMotor, false);
		pilot.setTravelSpeed(100);
		pilot.setAcceleration(50);

		gyroSensor.reset();
	}

	public static void gyroReset() {
		gyroSensor.reset();
		Delay.msDelay(500);
		Thread.yield();
	}

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

		PCOutController.write("Location X Y: " + X + " " + Y);

		if (CURRENT_PHASE == PHASE_MAPPING) {
			ColorReader.readColor(X, Y);
		}

	}

	public static void rotate_left() {
		if (IS_INTERRUPTED)
			return;

		float _goal = HEADING + 90;
		rotate_exact_to(_goal);
		HEADING = HEADING + 90;

		if (CURRENT_PHASE == PHASE_MAPPING) {
			ColorReader.readColor(X, Y);
		}
	}

	public static void rotate_right() {
		if (IS_INTERRUPTED)
			return;

		float _goal = HEADING - 90;
		rotate_exact_to(_goal);
		HEADING = HEADING - 90;

		if (CURRENT_PHASE == PHASE_MAPPING) {
			ColorReader.readColor(X, Y);
		}
	}

	public static void rotate_back() {
		if (IS_INTERRUPTED)
			return;

		float _goal = HEADING - 180;
		rotate_exact_to(_goal);
		HEADING = HEADING - 180;

		if (CURRENT_PHASE == PHASE_MAPPING) {
			ColorReader.readColor(X, Y);
		}
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

	private static void rotate_exact(float degree) {
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
		PCOutController.write("Angle: " + angle);
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

	public static void writeToFile() {
		File f = new File("location.txt");
		try {
			FileWriter writer = new FileWriter(f, false);
			writer.write(X + " " + Y + " " + HEADING);
			writer.flush();
			writer.close();
			PCOutController.write("Location is written to file");
		} catch (IOException e) {
			PCOutController.write("Location cannot be written to file");
		}
	}

	public static void readFromFile() {
		try {
			Scanner s = new Scanner(new BufferedReader(new FileReader(new File("location.txt"))));
			X = s.nextInt();
			Y = s.nextInt();
			PCOutController.write("Location is read from file");
		} catch (FileNotFoundException e) {
			PCOutController.write("Location cannot be read location from file");
		}
	}

}
