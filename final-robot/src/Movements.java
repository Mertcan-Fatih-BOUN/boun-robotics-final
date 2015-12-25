public class Movements {
	public static void goStraight(int distance){
		if(distance <= 10){
			goStraight_(distance);
		}else{
			goStraight_(10);
			goStraight(distance - 10);
		}
	}
	
	public static void goStraight_(int distance){
		Main.pilot.travel(distance);
		float difference = Main.getGyroStabilizer() - Main.readAngle();
		rotate_exact(difference);
	}
	
	public static void rotate_exact(float degree){
		float _degree = Main.readAngle();
		float _goal = _degree + degree;
		Main.pilot.rotate(degree);
		float read_angle = Main.readAngle();
		if(read_angle <= _goal - 0.5 || read_angle >= _goal + 0.5){
			rotate_exact(_goal - read_angle);
		}
	}
}
