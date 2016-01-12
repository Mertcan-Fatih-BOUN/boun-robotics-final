package robo;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import actuator.GripperController;
import actuator.MovementController;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.Color;
import lejos.robotics.ColorAdapter;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.utility.Delay;
import out.LCDController;
import out.PCOutController;
import sensor.ColorReader;
import sensor.UltrasonicReader;
import task.EntranceTask;
import task.ExecutionTask;
import task.MappingTask;

public class Main {

	public static final int PHASE_ENTRANCE = 0;
	public static final int PHASE_MAPPING = 1;
	public static final int PHASE_EXECUTION = 3;

	public static int CURRENT_PHASE = -1;

	public static boolean IS_INTERRUPTED = false;

	public static void main(String[] args) throws Exception {
		LCDController.initialize(BrickFinder.getDefault().getGraphicsLCD());
		GripperController.initialize(MotorPort.B);
		ColorReader.initialize(SensorPort.S3);
		UltrasonicReader.initialize(SensorPort.S4, SensorPort.S2, MotorPort.C);
		MovementController.initialize(MotorPort.A, MotorPort.D, SensorPort.S1);
		PCOutController.initialize();

		LCDController.print("Hello");

		Thread entrance_thread = null;
		Thread execution_thread = null;
		Thread mapping_thread = null;

		EntranceTask entranceTask = null;
		ExecutionTask executionTask = null;
		MappingTask mappingTask = null;

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

				UltrasonicReader.readRight();
				break;
			}
		}
	}
}
