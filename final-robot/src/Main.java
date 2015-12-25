
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.GraphicsLCD;
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
import lejos.robotics.navigation.Pose;
import lejos.utility.Delay;
import lejos.utility.PilotProps;

public class Main {

	static final int WALL_RETURN = -1;
	static final int EDGE_END_RETURN = -2;

	static EV3 ev3 = (EV3) BrickFinder.getDefault();
	static GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();
	public static int current_phase = -1;
	public static float currentGyroFix = 0;
	public static int current_usonic_mode = Constants.FORWARD;

	static EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.A);
	static EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.D);
	static EV3LargeRegulatedMotor gripperMotor = new EV3LargeRegulatedMotor(MotorPort.B);
	static NXTRegulatedMotor ultrasonicMotor = new NXTRegulatedMotor(MotorPort.C);

	static NXTUltrasonicSensor ultrasonicSensorLeft = new NXTUltrasonicSensor(SensorPort.S4);
	static NXTUltrasonicSensor ultrasonicSensorRight = new NXTUltrasonicSensor(SensorPort.S2);
	static EV3ColorSensor lightSensor = new EV3ColorSensor(SensorPort.S3);
	static EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S1);

	// static ColorAdapter colorAdapter = new ColorAdapter(lightSensor);
	static SampleProvider sampleProviderGyro = gyroSensor.getAngleAndRateMode();
	static SampleProvider sampleProviderLeft = ultrasonicSensorLeft.getDistanceMode();
	static SampleProvider sampleProviderRight = ultrasonicSensorRight.getDistanceMode();

	static DifferentialPilot pilot;

	static Movements movements;
	static int EDGE = -1;
	static double DISTANCE = -1;
	static int DIRECTION = 0;

	static OutputStream outputStream;
	
	static DataOutputStream dataOutputStream;
	
	public static void main(String[] args) throws Exception {
		setPilot();
		pilot.setTravelSpeed(100);
		pilot.setAcceleration(50);
		movements = new Movements(gyroSensor, pilot);

		ServerSocket serverSocket = new ServerSocket(1234);
		Socket client = serverSocket.accept();
		
		graphicsLCD.clear();
		graphicsLCD.drawString("Hello World", graphicsLCD.getWidth() / 2, graphicsLCD.getHeight() / 2,
				GraphicsLCD.VCENTER | GraphicsLCD.HCENTER);
		
		outputStream = client.getOutputStream();
		
		dataOutputStream = new DataOutputStream(outputStream);
		
		Delay.msDelay(100);
		
		Delay.msDelay(100);
		
		while (Button.readButtons() != Button.ID_UP) {
			Delay.msDelay(100);
		}
		current_phase = Constants.PHASE1;

		double forward_distance;
		double backward_distance;

		double left_distance = readLeft();
		double right_ditance = readRight();

		if (left_distance < right_ditance)
			DIRECTION = -1;
		else
			DIRECTION = 1;

		while (current_phase == Constants.PHASE1) {
			if (DISTANCE == -1)
				forward_distance = readForward();
			else
				forward_distance = 220 - DISTANCE;

			double traveledDistance = travelUntilNoneWall(forward_distance);

			// Encounter wall
			if (traveledDistance == -1) {
				DISTANCE = readForward();
				movements.rotate_back();
				DIRECTION *= -1;
			} else {
				// Now where it is
				if (DISTANCE != -1) {
					DISTANCE += traveledDistance;
					if (DISTANCE > 66 && DISTANCE < 132) {
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

		// start_(current_phase);
		// movements.goStraight(50);
		// movements.rotate_left();
		//
		// movements.goStraight(50);
		// movements.rotate_left();
		//
		// movements.goStraight(50);
		// movements.rotate_left();
		//
		// movements.goStraight(50);
		// movements.rotate_left();
		/*
		 * Movements.rotate_exact(-45); Movements.rotate_exact(90);
		 * Movements.rotate_exact(-90);
		 */
	}

	public static void send_to_pc(String s, float f){
		try {
			dataOutputStream.writeChars(s);
			dataOutputStream.flush();
			
			dataOutputStream.writeFloat(f);
			dataOutputStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void setPilot() throws IOException {
		PilotProps pilotProps = new PilotProps();
		pilotProps.setProperty(PilotProps.KEY_WHEELDIAMETER, "5.5");
		pilotProps.setProperty(PilotProps.KEY_TRACKWIDTH, "12.1");
		pilotProps.setProperty(PilotProps.KEY_LEFTMOTOR, "A");
		pilotProps.setProperty(PilotProps.KEY_RIGHTMOTOR, "D");
		pilotProps.setProperty(PilotProps.KEY_REVERSE, "false");
		pilotProps.storePersistentValues();
		pilotProps.loadPersistentValues();

		float wheelDiameter = Float.parseFloat(pilotProps.getProperty(PilotProps.KEY_WHEELDIAMETER, "5.5"));
		float trackWidth = Float.parseFloat(pilotProps.getProperty(PilotProps.KEY_TRACKWIDTH, "12.1"));
		boolean reverse = Boolean.parseBoolean(pilotProps.getProperty(PilotProps.KEY_REVERSE, "false"));

		pilot = new DifferentialPilot(wheelDiameter, trackWidth, leftMotor, rightMotor, reverse);
	}

	public static float readLeft() {
		if (current_usonic_mode != Constants.LEFT) {
			ultrasonicMotor.rotate(90);
			current_usonic_mode = Constants.LEFT;
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
		send_to_pc("left", distance / 3 * 100);
		return distance / 3 * 100;
	}

	public static float readRight() {
		if (current_usonic_mode != Constants.FORWARD) {
			ultrasonicMotor.rotate(-90);
			current_usonic_mode = Constants.FORWARD;
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
		send_to_pc("right", distance / 3 * 100);
		return distance / 3 * 100;
	}

	public static float readForward() {
		SampleProvider sp;
		if (current_usonic_mode != Constants.FORWARD) {
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
		send_to_pc("forward", distance / 3 * 100);
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

	private static void rotateCorner() {
		if (DIRECTION == 1)
			movements.rotate_right();
		else
			movements.rotate_left();

		double d = 1000;
		while (d < 50) {
			movements.goStraight(10);
			if (DIRECTION == 1)
				d = readRight();
			else
				d = readLeft();
		}
		DISTANCE = 0;

	}

	private static void enterEntrance() {

		if (DIRECTION == 1)
			movements.rotate_right();
		else
			movements.rotate_left();

		double d = 1000;

		while (d > 250) {
			movements.goStraight(10);
			d = readLeft() + readRight();
		}
		current_phase = Constants.PHASE2;

	}

	private static boolean isEntranceByControllForward() {
		boolean isEntrance = false;
		movements.goStraight(40);
		double d;
		if (DIRECTION == 1)
			d = readRight();
		else
			d = readLeft();

		if (d < 50)
			isEntrance = true;
		else
			isEntrance = false;
		movements.goStraight(-40);
		return isEntrance;

	}

	public static float travelUntilNoneWall() {
		int count = 0;
		float traveled_distance = 0;
		double wall_distance = 10000;
		while (wall_distance > 30) {
			movements.goStraight(20);
			traveled_distance += 20;
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
