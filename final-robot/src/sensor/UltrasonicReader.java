package sensor;

import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import out.PCOutController;

public class UltrasonicReader {
	public static final int FORWARD = 4;
	public static final int LEFT = 5;

	private static NXTRegulatedMotor ultrasonicMotor;
	private static NXTUltrasonicSensor ultrasonicSensorLeft;
	private static NXTUltrasonicSensor ultrasonicSensorRight;

	private static SampleProvider sampleProviderLeft;
	private static SampleProvider sampleProviderRight;

	public static int current_usonic_mode = FORWARD;

	public static void initialize(Port leftPort, Port rightPort, Port motorPort) {
		ultrasonicMotor = new NXTRegulatedMotor(motorPort);
		ultrasonicSensorLeft = new NXTUltrasonicSensor(leftPort);
		ultrasonicSensorRight = new NXTUltrasonicSensor(rightPort);

		sampleProviderLeft = ultrasonicSensorLeft.getDistanceMode();
		sampleProviderRight = ultrasonicSensorRight.getDistanceMode();
	}

	public static float readLeft() {
		if (current_usonic_mode != LEFT) {
			ultrasonicMotor.rotate(90);
			current_usonic_mode = LEFT;
		}

		SampleProvider sp = sampleProviderLeft;
		float distance = 0;
		for (int i = 0; i < 3; i++) {
			float[] sample = new float[sp.sampleSize()];
			sp.fetchSample(sample, 0);
			distance += sample[0];
			Delay.msDelay(50);
			Thread.yield();
		}
		PCOutController.write("Left distance: " + (distance / 3) * 100);
		return distance / 3 * 100;
	}

	public static float readRight() {
		if (current_usonic_mode != FORWARD) {
			ultrasonicMotor.rotate(-90);
			current_usonic_mode = FORWARD;
		}

		SampleProvider sp = sampleProviderRight;
		float distance = 0;
		for (int i = 0; i < 3; i++) {
			float[] sample = new float[sp.sampleSize()];
			sp.fetchSample(sample, 0);
			distance += sample[0];
			Delay.msDelay(50);
			Thread.yield();
		}
		PCOutController.write("Right distance: " + (distance / 3) * 100);
		return distance / 3 * 100;
	}

	public static float readForward() {
		SampleProvider sp;
		if (current_usonic_mode != FORWARD) {
			sp = sampleProviderRight;
		} else {
			sp = sampleProviderLeft;
		}

		float distance = 0;
		for (int i = 0; i < 3; i++) {
			float[] sample = new float[sp.sampleSize()];
			sp.fetchSample(sample, 0);
			distance += sample[0];
			Delay.msDelay(50);
			Thread.yield();
		}
		PCOutController.write("Forward distance: " + (distance / 3) * 100);
		return distance / 3 * 100;
	}
}
