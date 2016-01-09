package robo;

import java.awt.Point;
import java.util.ArrayList;

public class ConstantsVariables {
	public static final int PHASE_ENTRANCE = 0;
	public static final int PHASE_MAPPING = 1;
	public static final int PHASE_EXECUTION = 3;

	public static final int FORWARD = 4;
	public static final int LEFT = 5;

	public static final int LOG_FORWARD = 6;
	public static final int LOG_LEFT = 7;
	public static final int LOG_RIGHT = 8;
	public static final int LOG_ANGLE = 9;
	public static final int LOG_DIRECTION = 10;
	public static final int LOG_DISTANCE = 11;
	public static final int LOG_ROTATE_CORNER = 12;
	public static final int LOG_ENTER = 13;
	public static final int LOG_CURRENT_POINT_X = 14;
	public static final int LOG_CURRENT_POINT_Y = 15;
	public static final int LOG_RETRIEVEGRID = 15;

	public static int CURRENT_PHASE = -1;

	public static boolean IS_INTERRUPTED = false;
	
	public static ArrayList<Point> red = new ArrayList<>();
	public static ArrayList<Point> green = new ArrayList<>();

	public static Point getRedAverage(){
		Point x = new Point();
		for(int i = 0; i < red.size(); i++){
			x.x += red.get(i).x;
			x.y += red.get(i).y;
		}
		x.x /= red.size();
		x.y /= red.size();
		return x;
	}
	public static Point getGreenAverage(){
		Point x = new Point();
		for(int i = 0; i < green.size(); i++){
			x.x += green.get(i).x;
			x.y += green.get(i).y;
		}
		x.x /= green.size();
		x.y /= green.size();
		return x;
	}
}
