import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import javax.swing.JFrame;


public class Pc_Side extends JFrame{

	static InputStream inputStream;
	static DataInputStream dataInputStream;
	
	public static void main(String[] args) throws IOException {
		Pc_Side map = new Pc_Side();
		

		String ip = "10.0.1.1";
		
		@SuppressWarnings("resource")
		Socket socket = new Socket(ip, 1234);
		System.out.println("Connected!");
		
		inputStream = socket.getInputStream();
		dataInputStream = new DataInputStream(inputStream);
		
		while( true ){
			String log = dataInputStream.readUTF();
			float value = dataInputStream.readFloat();
			System.out.println(log + "   " + value);
		}
	}
}
