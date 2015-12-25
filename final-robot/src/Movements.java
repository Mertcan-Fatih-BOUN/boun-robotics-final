import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;

public class Movements {
	private int X = 0;
	private int Y = 0;
	private int HEADING = 0;

	private EV3GyroSensor gyroSensor;
	private DifferentialPilot pilot;
	private SampleProvider sampleProviderGyro;

	public Movements(EV3GyroSensor gyroSensor, DifferentialPilot pilot) {
		this.gyroSensor = gyroSensor;
		this.pilot = pilot;
		this.sampleProviderGyro = gyroSensor.getAngleMode();
	}

	public void goStraight(int distance) {
		int _dir = 0;
		if (distance < 0)
			_dir = -1;
		else
			_dir = 1;
		int abs_distance = distance / _dir;

		if (abs_distance <= 10) {
			goStraight_(_dir * abs_distance);
		} else {
			goStraight_(_dir * 10);
			goStraight(_dir * (abs_distance - 10));
		}
	}

	private void goStraight_(int distance) {
		float difference = HEADING - readAngle();
		rotate_exact(difference);
		pilot.travel(distance);
		difference = HEADING - readAngle();
		rotate_exact(difference);
	}

	public void rotate_left() {
		float _goal = HEADING + 90;
		rotate_exact_to(_goal);
		HEADING = HEADING + 90;
	}

	public void rotate_right() {
		float _goal = HEADING - 90;
		rotate_exact_to(_goal);
		HEADING = HEADING - 90;
	}

	public void rotate_back() {
		float _goal = HEADING - 180;
		rotate_exact_to(_goal);
		HEADING = HEADING - 180;
	}

	private void rotate_exact_to(float goal) {
		float diff = goal - readAngle();
		while (Math.abs(diff) > 0.5) {
			pilot.rotate(diff);
			diff = goal - readAngle();
		}
	}

	private void rotate_exact(float degree) {
		float _degree = readAngle();
		float _goal = _degree + degree;
		pilot.rotate(degree);
		float read_angle = readAngle();
		if (read_angle <= _goal - 0.5 || read_angle >= _goal + 0.5) {
			rotate_exact(_goal - read_angle);
		}
	}

	public float readAngle() {
		float[] sample = new float[sampleProviderGyro.sampleSize()];
		sampleProviderGyro.fetchSample(sample, 0);
		float angle = sample[0];
		Main.send_to_pc("gyro", angle);
		return angle;
	}

	private float normalize(float angle) {
		float a = angle;
		while (a > 180)
			a -= 360;
		while (a < -180)
			a += 360;
		return a;
	}

}
