package robo;

import java.io.IOException;


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
import lejos.robotics.LightDetectorAdaptor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.utility.Delay;
import lejos.utility.PilotProps;

public class Main {
	static EV3 ev3 = (EV3) BrickFinder.getDefault();
	static GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();
	public static int current_phase = -1;
	public static float currentGyroFix = 0;
	
	static EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.A);
	static EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.D);
	static EV3LargeRegulatedMotor gripperMotor = new EV3LargeRegulatedMotor(MotorPort.B);
	static NXTRegulatedMotor ultrasonicMotor = new NXTRegulatedMotor(MotorPort.C);
	
	
	static NXTUltrasonicSensor ultrasonicSensorLeft = new NXTUltrasonicSensor(SensorPort.S2);
	static NXTUltrasonicSensor ultrasonicSensorRight = new NXTUltrasonicSensor(SensorPort.S4);
	static EV3ColorSensor lightSensor = new EV3ColorSensor(SensorPort.S3);
	static EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S1);
	
	static ColorAdapter colorAdapter = new ColorAdapter(lightSensor);
	static SampleProvider sampleProviderGyro = gyroSensor.getAngleAndRateMode();
	static SampleProvider sampleProviderLeft = ultrasonicSensorLeft.getDistanceMode();
	static SampleProvider sampleProviderRight = ultrasonicSensorRight.getDistanceMode();
	
	static DifferentialPilot pilot;
	
	public static void main(String[] args)  throws Exception {
		setPilot();
		pilot.setTravelSpeed(100);
		pilot.setAcceleration(50);
		
		
		graphicsLCD.clear();
		graphicsLCD.drawString("Hello World", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		
		while (Button.readButtons() != Button.ID_UP) {
			Delay.msDelay(100);
		}
		current_phase = Constants.PHASE1;
		//start_(current_phase);
		Movements.goStraight(50);
		Movements.rotate_exact(90);
		setGyroStabilizer(90);
		
		Movements.goStraight(50);
		Movements.rotate_exact(90);
		setGyroStabilizer(180);
		

		Movements.goStraight(50);
		Movements.rotate_exact(90);
		setGyroStabilizer(270);
		

		Movements.goStraight(50);
		Movements.rotate_exact(90);
		setGyroStabilizer(0);
		/*Movements.rotate_exact(-45);
		Movements.rotate_exact(90);
		Movements.rotate_exact(-90);
		*/
	}
	
	public static void setPilot() throws IOException{
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
	
	public static float readLeft(){
		float [] sample = new float[sampleProviderLeft.sampleSize()];
    	sampleProviderLeft.fetchSample(sample, 0);
    	
    	float distance = sample[0];
    	return distance;
	}
	
	public static float readRight(){
		float [] sample = new float[sampleProviderRight.sampleSize()];
    	sampleProviderRight.fetchSample(sample, 0);
    	
    	float distance = sample[0];
    	return distance;
	}
	
	public static float readAngle(){
		float [] sample = new float[sampleProviderGyro.sampleSize()];
    	sampleProviderGyro.fetchSample(sample, 0);
    	
    	float angle = sample[0];
    	graphicsLCD.clear();
		graphicsLCD.drawString(angle + "", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		
    	return angle;
	}
	
	public static float[] readColor(){
		Color color = colorAdapter.getColor();
		
    	return new float[]{color.getRed(), color.getGreen(), color.getBlue()};
	}
	
	public static void setGyroStabilizer(float f){
		currentGyroFix = f;
	}
	public static float getGyroStabilizer(){return currentGyroFix;}
		
}
