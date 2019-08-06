import java.net.Socket;

public class User {
	String username;
	Socket socket;
	boolean foundInOtherServer;
	
	public User(){
		
	}

	public User(String username, Socket socket) {
		this.username = username;
		this.socket = socket;
		this.foundInOtherServer = false;
	}
	
	public String toString(){
			return username + "and" + socket;
	}
}
