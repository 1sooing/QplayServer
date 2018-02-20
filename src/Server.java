import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	private static final int SERVER_PORT = Server_Settings.SERVER_PORT;
	public static final int PLAYER_MAX_COUNT = Server_Settings.PLAYER_MAX_COUNT;

	static ServerSocket serverSocket;
	static User[] user = new User[PLAYER_MAX_COUNT];
	
	static Server_Handler handler;
	static Server_Looper looper;

	public static void main(String[] args) throws Exception {

		serverSocket = new ServerSocket(SERVER_PORT);
		Say("\nServer Started...");
		Say("Set port [" + SERVER_PORT + "]");
		
		handler = new Server_Handler(user);
		
		looper = new Server_Looper();
		Thread server_looper = new Thread(looper);
		server_looper.start();
		
		while (true) {

			int userid = 1;

			Socket socket = serverSocket.accept();
			
			while (true) {

				/* 
				 * Handle user logout status
				 * 
				 *  if user[userid] is not null, we explicitly check whether that user logged out or is occupied.
				 *  user logout function make user status -21. so we can check through whether user status is -21 or not.
				 */
				if (user[userid] != null) {
					userid++;
				}
				
				/*
				 * If userid is null
				 * we can use that slot for new user connection. without initializing
				 */
				else {

					Say("\nConnection from: " + socket.getInetAddress());

					DataOutputStream out = new DataOutputStream(socket.getOutputStream());
					DataInputStream in = new DataInputStream(socket.getInputStream());

					user[userid] = new User(out, in, userid);
					handler.loginUser(user[userid]);
					
					Thread th = new Thread(user[userid]);
					th.start();
					break;
				}
				
			}

		}
		
	}
	
	public static void FinishConnetction(int pid) {
		user[pid] = null;
	}
	
	public static void Say(String msg) {
		System.out.println(msg);
	}
}
