package out;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class PCOutController {

	private static BufferedWriter writer;

	public static void initialize() throws IOException {
		ServerSocket serverSocket = new ServerSocket(1234);
		Socket client = serverSocket.accept();
		OutputStream outputStream = client.getOutputStream();
		DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
		writer = new BufferedWriter(new OutputStreamWriter(dataOutputStream));
	}

	public static void write(String s) {
		if (writer == null)
			return;
		try {
			writer.write(s + "\n");
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
