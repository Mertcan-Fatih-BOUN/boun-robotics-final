package robo;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.ColorAdapter;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.utility.Delay;
import out.LCDController;
import task.EntranceTask;
import task.ExecutionTask;
import task.MappingTask;
import static robo.ConstantsVariables.*;

public class Main {

	static final int WALL_RETURN = -1;
	static final int EDGE_END_RETURN = -2;

	public static float currentGyroFix = 0;
	public static int current_usonic_mode = ConstantsVariables.FORWARD;

	static EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.A);
	static EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.D);
	static EV3LargeRegulatedMotor gripperMotor = new EV3LargeRegulatedMotor(MotorPort.B);
	static NXTRegulatedMotor ultrasonicMotor = new NXTRegulatedMotor(MotorPort.C);

	static NXTUltrasonicSensor ultrasonicSensorLeft = new NXTUltrasonicSensor(SensorPort.S4);
	static NXTUltrasonicSensor ultrasonicSensorRight = new NXTUltrasonicSensor(SensorPort.S2);
	static EV3ColorSensor lightSensor = new EV3ColorSensor(SensorPort.S3);
	static EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S1);
	static ColorAdapter colorAdapter = new ColorAdapter(lightSensor);

	// static ColorAdapter colorAdapter = new ColorAdapter(lightSensor);
	static SampleProvider sampleProviderGyro = gyroSensor.getAngleAndRateMode();
	static SampleProvider sampleProviderLeft = ultrasonicSensorLeft.getDistanceMode();
	static SampleProvider sampleProviderRight = ultrasonicSensorRight.getDistanceMode();

	static DifferentialPilot pilot;

	// static OutputStream outputStream;

	// static DataOutputStream dataOutputStream;

	public static void main(String[] args) throws Exception {
		LCDController.initialize(BrickFinder.getDefault().getGraphicsLCD());
		MovementController.initialize(leftMotor, rightMotor, gyroSensor, colorAdapter);

		// ServerSocket serverSocket = new ServerSocket(1234);
		// Socket client = serverSocket.accept();

		LCDController.print("Hello");

		// outputStream = client.getOutputStream();

		// dataOutputStream = new DataOutputStream(outputStream);

		Thread entrance_thread = null;
		Thread execution_thread = null;
		Thread mapping_thread = null;

		EntranceTask entranceTask = null;
		ExecutionTask executionTask = null;
		MappingTask mappingTask = null;

		System.out.println("fsafsafas");
		while (true) {
			int button = Button.waitForAnyPress();

			LCDController.print(button + "");

			if (button == Button.ID_UP || button == Button.ID_DOWN || button == Button.ID_LEFT
					|| button == Button.ID_ESCAPE) {

				IS_INTERRUPTED = true;

				LCDController.print("stopping");

				if (entrance_thread != null) {
					while (entrance_thread.isAlive()) {
					}
				}
				if (execution_thread != null) {
					while (execution_thread.isAlive()) {
					}
				}
				if (mapping_thread != null) {
					while (mapping_thread.isAlive()) {
					}
				}

				LCDController.print("stopped");

				IS_INTERRUPTED = false;

				switch (button) {
				case Button.ID_UP:
					CURRENT_PHASE = PHASE_ENTRANCE;
					entranceTask = new EntranceTask();
					entrance_thread = new Thread(entranceTask);
					entrance_thread.start();
					break;
				case Button.ID_DOWN:
					CURRENT_PHASE = PHASE_MAPPING;
					mappingTask = new MappingTask();
					mapping_thread = new Thread(mappingTask);
					mapping_thread.start();
					break;
				case Button.ID_LEFT:
					CURRENT_PHASE = PHASE_EXECUTION;
					executionTask = new ExecutionTask();
					execution_thread = new Thread(executionTask);
					execution_thread.start();
					break;
				default:
					IS_INTERRUPTED = true;
					break;
				}
			} else if (button == Button.ID_ENTER) {
				switch (CURRENT_PHASE) {
				case PHASE_EXECUTION:
					executionTask.grapBall();
					break;
				case PHASE_MAPPING:
					mappingTask.rotate();
					break;
				default:

					break;
				}
			} else if (button == Button.ID_RIGHT) {
				IS_INTERRUPTED = true;

				if (entrance_thread != null) {
					while (entrance_thread.isAlive()) {
					}
				}
				if (execution_thread != null) {
					while (execution_thread.isAlive()) {
					}
				}
				if (mapping_thread != null) {
					while (mapping_thread.isAlive()) {
					}
				}

				readRight();
				break;
			}
		}
	}

	public static void send_to_pc(int i, float f) {
		// try {
		// dataOutputStream.writeInt(i);
		// dataOutputStream.flush();
		//
		// dataOutputStream.writeFloat(f);
		// dataOutputStream.flush();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	public static float readLeft() {
		if (current_usonic_mode != ConstantsVariables.LEFT) {
			ultrasonicMotor.rotate(90);
			current_usonic_mode = ConstantsVariables.LEFT;
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
		send_to_pc(ConstantsVariables.LOG_LEFT, distance / 3 * 100);
		return distance / 3 * 100;
	}

	public static float readRight() {
		if (current_usonic_mode != ConstantsVariables.FORWARD) {
			ultrasonicMotor.rotate(-90);
			current_usonic_mode = ConstantsVariables.FORWARD;
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
		send_to_pc(ConstantsVariables.LOG_RIGHT, distance / 3 * 100);
		return distance / 3 * 100;
	}

	public static float readForward() {
		SampleProvider sp;
		if (current_usonic_mode != ConstantsVariables.FORWARD) {
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
		send_to_pc(ConstantsVariables.LOG_FORWARD, distance / 3 * 100);
		return distance / 3 * 100;

	}

	// public static float[] readColor() {
	// //Color color = colorAdapter.getColor();
	//
	// return new float[] { color.getRed(), color.getGreen(), color.getBlue() };
	// }

	public static void setGyroStabilizer(float f) {
		currentGyroFix = f;
	}

	public static float getGyroStabilizer() {
		return currentGyroFix;
	}

}
