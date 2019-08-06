import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Threads extends Thread {
	String sentence;
	String modifiedSentence;
	BufferedReader inFromClient;
	DataOutputStream outToClient;
	Socket connectionSocket;

	public Threads(Socket socket) throws IOException {
		this.connectionSocket = socket;
		inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
		outToClient = new DataOutputStream(connectionSocket.getOutputStream());
	}

	public void run() {
		while (true) {
			try {
				sentence = inFromClient.readLine();
				System.out.println("From Client: " + sentence);
				if (!sentence.isEmpty()) {
					if (sentence.equalsIgnoreCase("Bye") || sentence.equalsIgnoreCase("Exit")
							|| sentence.equalsIgnoreCase("Quit")) {
						connectionSocket.close();
						break;
					}
					modifiedSentence = sentence.toUpperCase() + '\n';
					outToClient.writeBytes(modifiedSentence);
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}