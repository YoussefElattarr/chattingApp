import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

import javax.swing.*;

@SuppressWarnings("serial")
class Client extends JFrame implements ActionListener {
	static Socket clientSocket = null;
	String dest;
	String username;
	int ttl;
	private JPanel panel1;
	//private JPanel panel2;
	private JPanel panel3;
	private JTextArea messages;
	private JTextArea texts;
	private JButton send;
	//private JTextArea members;

	public static ArrayList<User> GetMemberList() {
		System.out.println(Server1.GetMemberList().toString());
		System.out.println(Server2.GetMemberList().toString());
		ArrayList<User> temp = Server1.GetMemberList();
		temp.addAll(Server2.GetMemberList());
		return temp;
	}

	public Client() {
		setTitle("Client");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setBounds(50, 50, 800, 600);
		panel1 = new JPanel();
		// panel2 = new JPanel();
		panel3 = new JPanel();
		panel1.setPreferredSize(new Dimension(this.getWidth(), 450));
		// panel2.setPreferredSize(new Dimension(300, 450));
		panel3.setPreferredSize(new Dimension(this.getWidth(), 150));
		add(panel1, BorderLayout.WEST);
		// add(panel2, BorderLayout.EAST);
		add(panel3, BorderLayout.SOUTH);
		messages = new JTextArea();
		messages.setEditable(false);
		messages.setPreferredSize(new Dimension(500, 450));
		messages.setText("Your messages here");
		panel1.add(messages);
		texts = new JTextArea();
		texts.setEditable(true);
		texts.setPreferredSize(new Dimension(500, 150));
		// texts.setText("Your texts here");
		panel3.add(texts, BorderLayout.CENTER);
		// panel = new JPanel();
		// panel.setPreferredSize(new Dimension(300, this.getHeight()));
		send = new JButton("Send");
		send.addActionListener(this);
		// send.setPreferredSize(new Dimension(300, 150));
		panel3.add(send, BorderLayout.EAST);
		// panel.add(send, BorderLayout.SOUTH);
		// members = new JTextArea();
		// members.setPreferredSize(new Dimension(250, 450));
		// members.setText("See who's online here");
		// panel2.add(members);
		// panel.add(members, BorderLayout.NORTH);
		// add(panel, BorderLayout.EAST);
		revalidate();
		this.setVisible(true);

	}

	public boolean Join() throws IOException {
		boolean flag = false;
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		username = JOptionPane.showInputDialog(this, "Choose Username");
		String usernameToServer = "chosen username: " + username;
		outToServer.writeBytes(usernameToServer + '\n');
		if (inFromServer.readLine().equalsIgnoreCase("from server: cool")) {
			flag = true;
		} else {
			flag = false;
		}
		if (flag) {
			JLabel label = new JLabel(username);
			panel3.add(label, BorderLayout.WEST);
			revalidate();
			repaint();
		}
		return flag;
	}

	public void Quit() throws IOException {
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		outToServer.writeBytes("bye");
		clientSocket.close();

	}

	public String getTexts() {
		return texts.getText();
	}

	public void Chat(String Source, String Destination, int TTL, String Message) throws IOException {
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		this.append("To " + Destination + ": " + Message);
		outToServer.writeBytes((Message == null || Message == "") ? "" + TTL : TTL + Message + '\n');
	}

	public static void main(String argv[]) throws Exception {
		Client c = new Client();
		// System.out.println("Choose your server");
		String sentence = null;
		String s = null;
		String modifiedSentence;
		s = JOptionPane.showInputDialog(c, "Choose Server");
		// BufferedReader inFromUser = new BufferedReader(new
		// InputStreamReader(System.in));
		// String s = inFromUser.readLine();
		// while (!(c.getTexts().equals("1") || c.getTexts().equals(2))) {
		// String s = c.getTexts();
		// }
		// String s = c.getTexts();
		if (s.equals("1")) {
			clientSocket = new Socket("localhost", 6968);
		} else {
			clientSocket = new Socket("localhost", 4200);
		}
		// System.out.println("Enter username");
		// c.append("FROM SERVER: Enter username");
		// s = JOptionPane.showInputDialog(c,"Choose Username");
		while (!c.Join()) {
			// System.out.println("Choose another username");
			// c.append("FROM SERVER: Choose another username");
		}
		c.ttl = Integer.parseInt(JOptionPane.showInputDialog(c, "Set time to live"));
		// DataOutputStream outToServer = new
		// DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		// String username = inFromUser.readLine();
		// String usernameToServer = "chosen username: " + username;
		// outToServer.writeBytes(usernameToServer + '\n');
		while (true) {
			// while (inFromUser.ready()) {
			// sentence = inFromUser.readLine();
			// }
			if (sentence != null && sentence.equalsIgnoreCase("show me what you got")) {
				System.out.println("here3");
				ArrayList<User> temp = GetMemberList();
				System.out.println(temp.toString());
				for (int i = 0; i < temp.size(); i++) {
					System.out.println("fml");
					System.out.println(temp.get(i).username);
				}
				sentence = null;
			} else {
				// outToServer.writeBytes(sentence == null ? "" : sentence +
				// '\n');
				// if (sentence != null && (sentence.equalsIgnoreCase("Bye") ||
				// sentence.equalsIgnoreCase("Exit")
				// || sentence.equalsIgnoreCase("Quit"))) {
				// c.Quit();
				// break;
				// }
				sentence = null;
				while (inFromServer.ready()) {
					modifiedSentence = inFromServer.readLine();
					if (!modifiedSentence.equals("sent")
							&& (!modifiedSentence.equals("") || !modifiedSentence.equals(" "))) {
						c.append(modifiedSentence);
						// System.out.println(modifiedSentence);
					}
				}
			}
		}
	}

	public void append(String sentence) {
		String temp = this.messages.getText() + '\n';
		this.messages.setText(temp + sentence);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// JButton button = (JButton) e.getSource();
		String temp = this.getTexts();
		if (temp.length() > 9 && temp.substring(0, 9).equalsIgnoreCase("chat with")) {
			this.dest = temp.substring(10);
			try {
				DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
				outToServer.writeBytes((temp == null || temp == "") ? "" : temp + '\n');
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (temp != null
				&& (temp.equalsIgnoreCase("Bye") || temp.equalsIgnoreCase("Exit") || temp.equalsIgnoreCase("Quit"))) {
			try {
				this.Quit();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (temp.length() >= 7 && temp.substring(0, 7).equalsIgnoreCase("set ttl")) {
			this.ttl = Integer.parseInt(temp.substring(8));
		} else {
			try {
				this.Chat("", dest, ttl, temp);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		this.texts.setText("");
	}
}