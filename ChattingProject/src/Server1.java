import java.io.*;
import java.net.*;
import java.util.ArrayList;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server1 {

	static Socket toServer2;
	static int clientNumber;

	public static ArrayList<User> GetMemberList() {
		ArrayList<User> temp = Capitalizer.GetMemberListHelper();
		return temp;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("The server is running.");
		ExecutorService pool = Executors.newFixedThreadPool(20);
		clientNumber = -1;
		try (ServerSocket listener = new ServerSocket(6968)) {
			while (toServer2 == null) {
				initServer();
			}
			pool.execute(new Capitalizer(toServer2, clientNumber, true));
			while (true) {
				Socket s = listener.accept();
				if (!s.equals(toServer2)) {
					pool.execute(new Capitalizer(s, clientNumber++));
					System.out.println(GetMemberList());
				}

			}
		}
	}

	public static void initServer() { // waits until the server opens

		try {
			toServer2 = new Socket("localhost", 4200);
			// PrintWriter s = new PrintWriter(toServer2.getOutputStream(),
			// true);
			// s.println("connected to server 1");
			// s.flush();
		} catch (Exception e) {
			return;
		}

	}

	private static class Capitalizer implements Runnable {
		static ArrayList<User> currentConnections = new ArrayList<User>();
		static ArrayList<Boolean> searching = new ArrayList<Boolean>();
		private Socket socket;
		private int clientNumber;
		private String sentence;
		private String username;
		private String destUsername;
		// private String toServerUsernam;
		private boolean flagFoundHere;
		private boolean flagFound;
		private boolean flagFoundInServer2;
		private Socket dest;
		static private Socket originalClient;
		@SuppressWarnings("unused")
		private boolean isServer;
		@SuppressWarnings("unused")
		DataOutputStream outToServer2;

		public Capitalizer(Socket socket, int clientNumber) {
			this.socket = socket;
			this.clientNumber = clientNumber;
			flagFoundHere = false;
			flagFoundInServer2 = false;
			flagFound = false;

			System.out.println("New client #" + clientNumber + " connected at " + socket);
			// System.out.println(GetMemberListHelper());
		}

		public Capitalizer(Socket socket, int clientNumber, boolean server) throws IOException {
			this.socket = socket;
			this.clientNumber = clientNumber;
			flagFoundHere = false;
			this.outToServer2 = new DataOutputStream(toServer2.getOutputStream());
			flagFoundInServer2 = false;
			flagFound = false;
			isServer = server;

			System.out.println("New client #" + clientNumber + " connected at " + socket);
		}

		public static ArrayList<User> GetMemberListHelper() {
			// System.out.println(currentConnections.toString());
			ArrayList<User> temp = currentConnections;
			// temp.add(new User("ahmed", null));
			// temp.add(new User("amr", null));
			return temp;
		}

		public void getMemberList() throws IOException {
			System.out.println("H1");
			// outToServer2.writeBytes("from server1#get members");
			// System.out.println("H2");
			// BufferedReader in=new BufferedReader(new
			// InputStreamReader(socket.getInputStream()));
			/*
			 * String server2Members=infromServer2.readLine();
			 * System.out.println("H3"); PrintWriter out = new
			 * PrintWriter(originalClient.getOutputStream(), true);
			 * out.println("FROM SERVER: "+currentConnections+","+server2Members
			 * ); out.flush(); System.out.println("H4");
			 */
			Socket serverhelp = new Socket("localhost", 4200);
			DataOutputStream outToServerhelp = new DataOutputStream(serverhelp.getOutputStream());
			BufferedReader inFromServerhelp = new BufferedReader(new InputStreamReader(serverhelp.getInputStream()));
			outToServerhelp.writeBytes("from server1#getmembers" + '\n');
			System.out.println("H2");
			String server2members = inFromServerhelp.readLine();
			System.out.println("H3");
			serverhelp.close();
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			String s = "";
			for (User usr : currentConnections) {
				s += usr.username;
				s += ",";
			}
			out.println("FROM SERVER: " + s + server2members);
			out.flush();

		}

		public boolean JoinResponse() {
			for (int i = 0; i < currentConnections.size(); i++) {
				if (currentConnections.get(i) != null && currentConnections.get(i).username != null
						&& currentConnections.get(i).username.equalsIgnoreCase(username)) {
					return false;
				}
			}
			return true;
		}

		public void run() {
			while (true) {
				try {
					DataOutputStream outToServer2 = new DataOutputStream(toServer2.getOutputStream());
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					sentence = in.readLine();
					// System.out.println(sentence);
					if (sentence != null && sentence.length() >= 23
							&& (sentence.substring(0, 23).equalsIgnoreCase("from server2#getmembers"))) {
						System.out.println("hereplease");
						DataOutputStream outhelp = new DataOutputStream(socket.getOutputStream());
						System.out.println("mf");
						String s = "";
						for (User usr : currentConnections) {
							s += usr.username;
							s += ",";
						}
						sentence = null;
						Server1.clientNumber--;
						outhelp.writeBytes("From Server1: " + s + '\n');
						return;
					} else if (sentence != null && sentence.length() >= 11
							&& (sentence.substring(1).equalsIgnoreCase("get members"))) {
						getMemberList();
						sentence = null;
					} else if (sentence != null && (sentence.length() >= 23)
							&& (sentence.substring(0, 23).equalsIgnoreCase("from server2#chat with "))) {
						destUsername = sentence.substring(23);
						for (int i = 0; i < currentConnections.size(); i++) {
							if (currentConnections.get(i) != null && currentConnections.get(i).username != null
									&& currentConnections.get(i).socket != null
									&& currentConnections.get(i).username.equalsIgnoreCase(destUsername)) {
								dest = currentConnections.get(i).socket;
								outToServer2.writeBytes("From Server1: found" + '\n');
								sentence = null;
								flagFound = true;
							}
						}
						if (!flagFound) {
							outToServer2.writeBytes("From Server1: not found" + '\n');
						}
						sentence = null;
					} else if (sentence != null && sentence.equals("From Server2: not found")) {
						int c = 0;
						for (int i = 0; i < searching.size(); i++) {
							if (searching.get(i) == true) {
								c = i;
								searching.set(i, false);
							}
						}
						currentConnections.get(c).foundInOtherServer = false;
						PrintWriter out = new PrintWriter(originalClient.getOutputStream(), true);
						out.println("FROM SERVER: The user is not online");
						out.flush();
						flagFoundInServer2 = false;
						sentence = null;
					} else if (sentence != null && sentence.equals("From Server2: found")) {
						int c = 0;
						for (int i = 0; i < searching.size(); i++) {
							if (searching.get(i) == true) {
								c = i;
								searching.set(i, false);
							}
						}
						currentConnections.get(c).foundInOtherServer = true;
						PrintWriter out = new PrintWriter(originalClient.getOutputStream(), true);
						out.println("FROM SERVER: connected");
						out.flush();
						sentence = null;
					} else if (sentence != null && sentence.length() >= 23
							&& sentence.substring(0, 22).equalsIgnoreCase("Sentence From Server2#")) {
						sentence = sentence.substring(22);
						String fromUser = "";
						String toUser = "";
						String message = "";
						String temp = "";
						int c = 3;
						for (int i = 0; i < sentence.length(); i++) {
							if (!(sentence.charAt(i) == '#')) {
								temp += sentence.charAt(i);
							} else if (c == 3 && (sentence.charAt(i) == '#')) {
								fromUser = temp;
								temp = "";
								c--;
							} else if (c == 2 && (sentence.charAt(i) == '#')) {
								toUser = temp;
								temp = "";
								c--;
							} else if (c == 1 && (sentence.charAt(i) == '#')) {
								message = temp;
								temp = "";
							}
						}
						for (int i = 0; i < currentConnections.size(); i++) {
							if (currentConnections.get(i) != null && currentConnections.get(i).username != null
									&& currentConnections.get(i).socket != null
									&& currentConnections.get(i).username.equalsIgnoreCase(toUser)) {
								dest = currentConnections.get(i).socket;
							}
						}
						int ttl = Integer.parseInt(message.substring(0, 1));
						if (ttl > 0) {
							sentence = null;
							PrintWriter out = new PrintWriter(dest.getOutputStream(), true);
							out.println("FROM " + fromUser + ": " + message.substring(1));
							out.flush();
							sentence = null;
						}
						// else {
						// sentence = null;
						// PrintWriter out = new
						// PrintWriter(dest.getOutputStream(), true);
						// out.println("FROM SERVER: ttl reached zero");
						// out.flush();
						// sentence = null;
						// }
					} else if (sentence != null
							&& ((sentence.length() >= 17) && sentence.substring(0, 17).equals("chosen username: "))) {
						username = sentence.substring(17);
						if (JoinResponse() == true) {
							currentConnections.add(new User());
							currentConnections.get(clientNumber).username = this.username;
							currentConnections.get(clientNumber).socket = this.socket;
							searching.add(false);
							PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
							out.println("FROM SERVER: cool");
							out.flush();
							sentence = null;
						} else {
							PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
							out.println("FROM SERVER: choose a different username");
							out.flush();
							sentence = null;
						}
					} else if (sentence != null && ((sentence.length() >= 10)
							&& (sentence.substring(0, 9).equalsIgnoreCase("chat with")))) {
						destUsername = sentence.substring(10);
						flagFoundHere = false;
						for (int i = 0; i < currentConnections.size(); i++) {
							if (currentConnections.get(i) != null && currentConnections.get(i).username != null
									&& currentConnections.get(i).socket != null
									&& currentConnections.get(i).username.equalsIgnoreCase(destUsername)) {
								dest = currentConnections.get(i).socket;
								PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
								flagFoundHere = true;
								out.println("FROM SERVER: connected");
								out.flush();
								sentence = null;
							}
						}

						if (!flagFoundHere) {
							originalClient = socket;
							searching.set(clientNumber, true);
							outToServer2.writeBytes("From Server1#chat with " + destUsername + '\n');
							dest = null;
						}
					} else if (sentence != null) {
						flagFoundInServer2 = currentConnections.get(clientNumber).foundInOtherServer;
						if (dest == null && !flagFoundHere && !flagFoundInServer2) {
							if (sentence != null && (sentence.equalsIgnoreCase("Bye")
									|| sentence.equalsIgnoreCase("Exit") || sentence.equalsIgnoreCase("Quit"))) {
								currentConnections.get(clientNumber).username = "";
								currentConnections.get(clientNumber).socket = null;
								return;
							}
							PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
							out.println("FROM SERVER: please choose destination");
							sentence = null;
							out.flush();
						} else if (dest == null && flagFoundInServer2) {
							int ttl = Integer.parseInt(sentence.substring(0, 1));
							ttl--;
							if (ttl > 0) {
								sentence = ttl + sentence.substring(1);
								outToServer2.writeBytes(
										"Sentence From Server1#" + currentConnections.get(clientNumber).username + "#"
												+ destUsername + "#" + sentence + "#" + '\n');
								PrintWriter out2 = new PrintWriter(socket.getOutputStream(), true);
								out2.println("sent");
								out2.flush();
							} else {
								sentence = null;
								PrintWriter out = new PrintWriter(
										currentConnections.get(clientNumber).socket.getOutputStream(), true);
								out.println("FROM SERVER: ttl reached zero");
								out.flush();
								sentence = null;
							}

						} else {
							if (sentence != null && (sentence.equalsIgnoreCase("Bye")
									|| sentence.equalsIgnoreCase("Exit") || sentence.equalsIgnoreCase("Quit"))) {
								currentConnections.get(clientNumber).username = "";
								currentConnections.get(clientNumber).socket = null;
								return;
							}
							int ttl = Integer.parseInt(sentence.substring(0, 1));
							if (ttl > 0) {
								PrintWriter out = new PrintWriter(dest.getOutputStream(), true);
								out.println("FROM " + username + ": " + sentence.substring(1));
								sentence = null;
								out.flush();
								PrintWriter out2 = new PrintWriter(socket.getOutputStream(), true);
								out2.println("sent");
								out2.flush();
							} else {
								sentence = null;
								PrintWriter out = new PrintWriter(
										currentConnections.get(clientNumber).socket.getOutputStream(), true);
								out.println("FROM SERVER: ttl reached zero");
								out.flush();
								sentence = null;
							}
						}
					}
				} catch (Exception e) {
					System.out.println(e.getClass());
					System.out.println("Error handling client #" + clientNumber);
				} /*
					 * finally { try { socket.close(); } catch (IOException e) {
					 * } System.out.println("Connection with client # " +
					 * clientNumber + " closed"); }
					 */
			}
		}
	}
}