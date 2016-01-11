import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.xml.ws.soap.AddressingFeature;

public class PCMain extends JPanel {
	static InputStream inputStream;
	static DataInputStream dataInputStream;

	static int[][] grid = new int[6][6];
	static ArrayList<Point> greens = new ArrayList<>();
	static ArrayList<Point> reds = new ArrayList<>();
	static Point current = new Point();
	static Button button;

	public PCMain() {
		super();
		setMinimumSize(new Dimension(198, 198));

		setLayout(null);
		// Adding to JFrame
		setVisible(true);

		for (int i = 1; i < 5; i++) {
			grid[0][i] = -1;
			grid[5][i] = -1;
			grid[i][5] = -1;
		}
		for (int i = 1; i < 5; i++) {
			for (int j = 1; j < 5; j++) {
				grid[i][j] = 0;
			}
		}

	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(10.0f));

		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 6; j++) {
				if (grid[i][j] == -1) {
					g2.setColor(Color.GRAY);
					g2.fillRect((5 - i) * 33, (5 - j) * 33, 33, 33);
				} else if (grid[i][j] == 1) {
					g2.setColor(Color.BLACK);
					g2.fillRect((5 - i) * 33, (5 - j) * 33, 33, 33);
				}
			}
		}

		g2.setColor(Color.RED);

		for (Point p : reds) {
			g2.drawLine(198 - p.x, 198 - p.y, 198 - p.x, 198 - p.y);
		}

		g2.setColor(Color.GREEN);

		for (Point p : greens) {
			g2.drawLine(198 - p.x, 198 - p.y, 198 - p.x, 198 - p.y);
		}

		g2.setColor(Color.BLUE);

		g2.drawLine(198 - current.x, 198 - current.y, 198 - current.x, 198 - current.y);

	}

	public static void main(String[] args) throws Exception {

		JFrame frame = new JFrame("a");
		PCMain monitor = new PCMain();

		frame.setLayout(new BorderLayout());

		frame.add(monitor, BorderLayout.CENTER);
		frame.setSize(250,250);
		
		frame.setVisible(true);
		
		
		String ip = "10.0.1.1";

		@SuppressWarnings("resource")
		Socket socket = null;
		while (true) {
			try {
				socket = new Socket(ip, 1234);
				if (socket != null) {
					break;
				}
			} catch (IOException e) {
				Thread.sleep(1000);
			}
		}
		System.out.println("Connected!");

		inputStream = socket.getInputStream();
		dataInputStream = new DataInputStream(inputStream);
		BufferedReader reader = new BufferedReader(new InputStreamReader(dataInputStream));
		while (true) {
			String s = reader.readLine();
			System.out.println(s);
			if (s.startsWith("Location X Y: ")) {
				Scanner scanner = new Scanner(s);
				scanner.next();
				scanner.next();
				scanner.next();
				current.x = scanner.nextInt();
				current.y = scanner.nextInt();
			} else if (s.startsWith("Red found X Y:")) {
				Point p = new Point();
				Scanner scanner = new Scanner(s);

				scanner.next();
				scanner.next();
				scanner.next();
				scanner.next();
				p.x = scanner.nextInt();
				p.y = scanner.nextInt();
				reds.add(p);
			} else if (s.startsWith("Green found X Y:")) {
				Point p = new Point();
				Scanner scanner = new Scanner(s);
				scanner.next();
				scanner.next();
				scanner.next();
				scanner.next();
				p.x = scanner.nextInt();
				p.y = scanner.nextInt();
				greens.add(p);
			} else if (s.startsWith("There is obstacle: ")) {

				Scanner scanner = new Scanner(s);
				scanner.next();
				scanner.next();
				scanner.next();
				grid[scanner.nextInt()][scanner.nextInt()] = 1;
			} else if (s.startsWith("There is no obstacle: ")) {
				Scanner scanner = new Scanner(s);
				scanner.next();
				scanner.next();
				scanner.next();
				scanner.next();
				grid[scanner.nextInt()][scanner.nextInt()] = 0;
			}
			frame.repaint();

		}
	}

}
