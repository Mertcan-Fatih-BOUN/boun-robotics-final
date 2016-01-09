package out;

import lejos.hardware.lcd.GraphicsLCD;

public class LCDController {
	private static GraphicsLCD graphicsLCD;

	public static void initialize(GraphicsLCD lcd) {
		graphicsLCD = lcd;
	}

	public static void print(String string) {
		graphicsLCD.clear();
		String[] strings = string.split("\n");

		for (int i = 0; i < strings.length; i++) {
			graphicsLCD.drawString(strings[i], graphicsLCD.getWidth() / 2, i * 15, GraphicsLCD.HCENTER);

		}

		// graphicsLCD.drawString(string, graphicsLCD.getWidth() / 2,
		// graphicsLCD.getHeight() / 2,
		// GraphicsLCD.VCENTER | GraphicsLCD.HCENTER);
	}
}
