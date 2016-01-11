package sensor;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.w3c.dom.css.RGBColor;

import lejos.hardware.Sound;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.Color;
import lejos.robotics.ColorAdapter;
import out.LCDController;
import out.PCOutController;

public class ColorReader {
	private static ArrayList<Point> red = new ArrayList<>();
	private static ArrayList<Point> green = new ArrayList<>();

	private static ColorAdapter colorAdapter;
	private static EV3ColorSensor lightSensor;

	public static void initialize(Port port) {
		lightSensor = new EV3ColorSensor(port);
		colorAdapter = new ColorAdapter(lightSensor);
	}

	public static void readColor(int X, int Y) {
		Color c = colorAdapter.getColor();
		LCDController.print("r: " + c.getRed() + "\ng: " + c.getGreen() + "\nb: " + c.getBlue());
		if (c.getRed() > 7 && c.getBlue() < 7 && c.getGreen() < 7) {
			red.add(new Point(X, Y));
			Sound.playTone(440, 100, 10);
			PCOutController.write("Red found X Y: " + X + " " + Y);
		}
		if (c.getRed() < 7 && c.getBlue() < 7 && c.getGreen() > 7) {
			green.add(new Point(X, Y));
			Sound.beepSequence();
			PCOutController.write("Green found X Y: " + X + " " + Y);
		}
	}

	private static Point getRedAverage() {
		Point x = new Point();

		if (red.size() == 0) {
			x.x = 66;
			x.y = 66;
		} else {
			for (int i = 0; i < red.size(); i++) {
				x.x += red.get(i).x;
				x.y += red.get(i).y;
			}
			x.x /= red.size();
			x.y /= red.size();
		}
		return x;
	}

	private static Point getGreenAverage() {
		Point x = new Point();
		if (green.size() == 0) {
			x.x = 99;
			x.y = 99;
		} else {
			for (int i = 0; i < green.size(); i++) {
				x.x += green.get(i).x;
				x.y += green.get(i).y;
			}
			x.x /= green.size();
			x.y /= green.size();
		}
		return x;
	}

	public static void writeToFile() {
		Point r = getRedAverage();
		Point g = getGreenAverage();

		File f = new File("color.txt");
		try {
			FileWriter writer = new FileWriter(f, false);
			writer.write(r.x + " " + r.y + " " + g.x + " " + g.y);
			writer.flush();
			writer.close();
			PCOutController.write("Color points are written to file");
		} catch (IOException e) {
			PCOutController.write("Color points cannot be written to file");
		}
	}

	public static Point[] getColors() {
		Point[] colors = new Point[2];
		try {
			Scanner s = new Scanner(new BufferedReader(new FileReader(new File("color.txt"))));
			Point r = new Point(s.nextInt(), s.nextInt());
			Point g = new Point(s.nextInt(), s.nextInt());
			colors[0] = r;
			colors[1] = g;
			PCOutController.write("Color points are read from file");
		} catch (FileNotFoundException e) {
			Point r = new Point(33, 33);
			Point g = new Point(66, 66);
			colors[0] = r;
			colors[1] = g;
			PCOutController.write("Color points cannot be read from file");
		}
		PCOutController.write("Red found X Y: " + colors[0].x + " " + colors[0].y);
		PCOutController.write("Green found X Y: " + colors[1].x + " " + colors[1].y);
		return colors;
	}

	public static boolean allFound() {
		return green.size() > 0 && red.size() > 0;
	}

}
