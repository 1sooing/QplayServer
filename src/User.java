import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class User implements Runnable {

	DataOutputStream out;
	DataInputStream in;

	Server_Handler handler;

	/*
	 * USER PRIVATE INFO
	 */
	int USER_STATUS = -1;
	boolean running = true;

	int slotid = -1;
	int playerid = -1;
	int teamid = -1;
	String nick = "";

	int dresscode = -1;
	
	/*
	 * USER STREAM INFO
	 */
	long recentAlive = System.currentTimeMillis();
	
	int roomidin = -1;
	String roomnamein = "";
	int capacityin = -1;
	String passwordin;

	int slotidin = -1;
	int teamidin = -1;
	int playeridin = -1;
	int damage = -1;
	String nickin = "";
	int dresscodein = -1;

	int xin = -1;
	int yin = -1;
	int xmissilein = -1;
	int ymissilein = -1;
	int missiletype = -5;

	int mode = -1;
	int direction = 0;
	double accx = -1;
	double accy = -1;

	double vel_missile_x = 0;
	double vel_missile_y = 0;

	String text1 = "";
	String text2 = "";
	
	int roomid = -1;
	String roomname = "";
	int capacity = -1;
	String password = "";

	long timein = -1;
	
	public User(DataOutputStream out, DataInputStream in, int pid) {

		this.out = out;
		this.in = in;
		this.playerid = pid;
		this.dresscode = pid;
		this.handler = Server_Handler.getInstance();
	}

	public int getId() {
		return this.playerid;
	}
	
	public void run() {

		while (running) {

			String line = null;
			line = readData();

			if (line == null)
				continue;

			String contents[] = line.split(Character.toString((char) 007));

			mode = Integer.parseInt(contents[0]);

			switch (USER_STATUS) {

			case 2:
				switch (mode) {
//				case Server_Settings.SET_KEEP_ALIVE:
//
//					/* Renew recent alive time */
//					this.recentAlive = System.currentTimeMillis();
//					break;
				
//				case Server_Settings.SET_DEBUG_TIME:
//					long time = Long.parseLong(contents[1]);
//					handler.broadcastTime(roomid, time);
//					break;
				
				case Server_Settings.SET_AQUA_PLAYER_MOVE:
					boolean movement = Boolean.parseBoolean(contents[1]);
					double acc = Double.parseDouble(contents[2]);

					handler.moveInAquaGame(roomid, slotid, movement, acc);
					break;
					
				case Server_Settings.SET_AQUA_MISSILE_MOVE:
					missiletype = Integer.parseInt(contents[1]);
					xmissilein = (int) Double.parseDouble(contents[2]);
					ymissilein = (int) Double.parseDouble(contents[3]);
					vel_missile_x = Double.parseDouble(contents[4]);
					vel_missile_y = Double.parseDouble(contents[5]);
					
					handler.shotInAquaGame(roomid, slotid, teamid, missiletype, xmissilein, ymissilein, vel_missile_x, vel_missile_y);
					break;
					
				case Server_Settings.SET_AQUA_HIT:
					slotidin = Integer.parseInt(contents[1]);
					damage = Integer.parseInt(contents[2]);
					accx = Double.parseDouble(contents[3]);
					accy = Double.parseDouble(contents[4]);
					
					handler.hitMoveInAquaGame(slotid, roomid, slotidin, damage, accx, accy);
					break;

//				case Server_Settings.SET_AQUA_PLAYER_POS:
//					xin = Integer.parseInt(contents[1]);
//					yin = Integer.parseInt(contents[2]);
//					handler.setAquaPos(roomid, slotid, xin, yin);
//					break;

				case Server_Settings.SET_EXIT_AQUA:
					handler.exitAqua(roomid, slotid);
					handler.enterRoom(this.out, roomid, slotid, teamid, playerid, nick, dresscode);
					break;
				}
				break;
			
			case 1:
				switch (mode) {
//				case Server_Settings.SET_KEEP_ALIVE:
//
//					/* Renew recent alive time */
//					this.recentAlive = System.currentTimeMillis();
//					break;
				
				case Server_Settings.SET_LOBBY_PLAYER_MOVE:
					direction = Integer.parseInt(contents[1]);
					handler.moveInLobby(playerid, direction);
					break;
					
				case Server_Settings.SET_LOBBY_PLAYER_TEXT:
					int concat = Integer.parseInt(contents[1]);
					text1 = contents[2];
					
					switch(concat) {
					case 0:
						handler.sayInLobby(playerid, text1);
						text2 = "";
						break;
					case 1:
						text2 = text1;
						break;
					case 2:
						text2 += text1;
						handler.sayInLobby(playerid, text2);
						text2 = "";
						break;
					}
					break;
					
				case Server_Settings.SET_LOBBY_DRESSCODE:
					/*
					 *  Update dresscode
					 */
					dresscode = Integer.parseInt(contents[1]);
					handler.dressInLobby(playerid, dresscode);
					break;
					
				case Server_Settings.SET_LOBBY_PLAYER_POS:
					xin = Integer.parseInt(contents[1]);
					yin = Integer.parseInt(contents[2]);
					handler.setLobbyPos(playerid, xin, yin);
					break;

				case Server_Settings.SET_LOGOUT:
					handler.logoutUser(this);
					break;

				case Server_Settings.SET_AQUA_ROOM_INFO:
					handler.showAquaRoom(playerid);
					break;

				case Server_Settings.SET_NOTICE:
					handler.lobbyNotice(playerid);
					break;
					
				case Server_Settings.SET_AQUA_ROOM_CREATE_REQUEST:
					roomname = contents[1];
					capacity = Integer.parseInt(contents[2]);
					password = contents[3];

					/*
					 * Set roomid for boss
					 * Set teamid for boss
					 */
					roomid = handler.createAquaRoom(roomname);
					if ( roomid > -1) {
						slotid = 0;
						teamid = 0;
						
//						System.out.println("Change status of " + playerid + "[" + nick + "] to " + USER_STATUS);
						handler.exitLobby(playerid);
						handler.enterRoom(this.out, roomid, -1, -1, playerid, nick, dresscode);
					}
					break;

				case Server_Settings.SET_AQUA_ROOM_ENTER_REQUEST:
					roomidin = Integer.parseInt(contents[1]);

					slotid = handler.enterRoom(this.out, roomidin, -1, -1, playerid, nick, dresscode); 
					if( slotid > -1 ) {
						/*
						 * Set roomid for players except boss
						 * Set slotid
						 * Set teamid
						 */
						roomid = roomidin;
						teamid = slotid;
						
//						System.out.println("Change status of " + playerid + "[" + nick + "] to " + USER_STATUS);
						handler.exitLobby(playerid);
					}
					break;

				}
				break;

			case 3:
				switch (mode) {
				case Server_Settings.SET_KEEP_ALIVE:
					
					/* Renew recent alive time */
					this.recentAlive = System.currentTimeMillis();
					break;
				
				case Server_Settings.SET_AQUA_READY:
					boolean ready = Boolean.parseBoolean(contents[1]);

//					System.out.println("player " + slotid + " in room " + roomid + " ready: " + ready);
					handler.readyAquaRoomPlayer(roomid, slotid, ready);
					break;

				case Server_Settings.SET_AQUA_TEAM:
					/*
					 *  Update team
					 */
					teamid = Integer.parseInt(contents[1]);
					handler.teamInRoom(roomid, slotid, teamid);
					break;
					
				case Server_Settings.SET_AQUA_PLAYER_TEXT:
					int concat = Integer.parseInt(contents[1]);
					text1 = contents[2];
					
					switch(concat) {
					case 0:
						handler.sayInRoom(roomid, slotid, text1);
						text2 = "";
						break;
					case 1:
						text2 = text1;
						break;
					case 2:
						text2 += text1;
						handler.sayInRoom(roomid, slotid, text2);
						text2 = "";
						break;
					}
					break;
					
				case Server_Settings.SET_EXIT_ROOM:
					handler.exitRoom(roomid, slotid);
					packetLobbyEnterAccept();
					handler.enterLobby(playerid, nick, dresscode);
					break;

				case Server_Settings.SET_START:
					handler.aquaGameStart(roomid);
					break;
				}
				break;
				
			case -1:
				switch (mode) {
				case Server_Settings.SET_KEEP_ALIVE:
					
					/* Renew recent alive time */
					this.recentAlive = System.currentTimeMillis();
					break;
				
				case Server_Settings.SET_LOBBY_ENTER_REQUEST:

					nickin = contents[1];
					int VERSION = Integer.parseInt(contents[2]);
					nick = nickin;
					
					/* Version verify check */
					if(VERSION == Server_Settings.LATEST_CLIENT_VERSION) {
						packetLobbyEnterAccept();
						handler.enterLobby(playerid, nick, dresscode);	
					} else {
						packetVersion(Server_Settings.LATEST_CLIENT_VERSION);
					}
					break;
					
				}
				break;

			} // end of switch(USR_STATUS)

		}	// end of while(true)
		
	}	// end of run() 

	public void packetAquaRoomInfo(int roomid, String roomname, int capacity) {

		String msg = "" + Server_Settings.SET_AQUA_ROOM_INFO + (char) 007 + roomid + (char) 007 + roomname + (char) 007
				+ capacity + (char) 007;

		int count = 0;

		Matcher m = Pattern.compile("[ㄱ-ㅎㅏ-ㅣ가-힣]").matcher(roomname);
		while (m.find()) {
			count++;
		}

		int length = 50 - msg.length() - count * 2;
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}

	public void packetLobbyPlayerInfo(int pid, String nick, int dresscode, int x, int y) {

		String msg = "" + Server_Settings.SET_LOBBY_ENTRANCE + (char) 007 + pid + (char) 007 + nick + (char) 007
				+ dresscode + (char) 007 + x + (char) 007 + y + (char) 007;

		int count = 0;

		Matcher m = Pattern.compile("[ㄱ-ㅎㅏ-ㅣ가-힣]").matcher(nick);
		while (m.find()) {
			count++;
		}

		int length = 50 - msg.length() - count * 2;
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}

	public void packetLobbyPlayerDresscode(int pid, int dresscode) {
		String msg = "" + Server_Settings.SET_LOBBY_DRESSCODE + (char) 007 + pid + (char) 007 + dresscode + (char) 007;

		int length = 50 - msg.length();
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}
	
	public void packetRoomPlayerTeamid (int slotid, int teamid) {
		String msg = "" + Server_Settings.SET_AQUA_TEAM + (char) 007 + slotid + (char) 007 + teamid + (char) 007;

		int length = 50 - msg.length();
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}
	
	public void packetAquaRoomPlayerInfo(int slotid, int teamid, String nick, int dresscode, boolean ready, int x, int y) {

		String msg = "" + Server_Settings.SET_AQUA_ROOM_PLAYER_INFO + (char) 007 + slotid + (char) 007 + teamid + (char) 007 + nick + (char) 007 + dresscode + (char) 007 + ready + (char) 007 + x + (char) 007 + y + (char) 007;

		int count = 0;

		Matcher m = Pattern.compile("[ㄱ-ㅎㅏ-ㅣ가-힣]").matcher(nick);
		while (m.find()) {
			count++;
		}

		int length = 50 - msg.length() - count * 2;
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}

	public void packetAquaRoomEnter(int roomid, int slotid) {

		String msg = "" + Server_Settings.SET_AQUA_ROOM_ENTER_ACCEPT + (char) 007 + roomid + (char) 007 + slotid
				+ (char) 007;

		int length = 50 - msg.length();
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}

	public void packetLobbyEnterAccept() {

		String msg = "" + Server_Settings.SET_LOBBY_ENTER_ACCEPT + (char) 007;

		int length = 50 - msg.length();
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}
	
	public void packetVersion(int version) {

		String msg = "" + Server_Settings.SET_VERSION + (char) 007 + version + (char) 007;

		int length = 50 - msg.length();
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}

	public void packetUserLogout(int pid) {
		String msg = "" + Server_Settings.SET_LOGOUT + (char) 007 + pid + (char) 007;

		int length = 50 - msg.length();
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}

	public void packetExitLobby(int pid) {
		String msg = "" + Server_Settings.SET_EXIT_LOBBY + (char) 007 + pid + (char) 007;

		int length = 50 - msg.length();
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}
	
	public void packetExitAqua(int pid) {
		String msg = "" + Server_Settings.SET_EXIT_AQUA + (char) 007 + pid + (char) 007;

		int length = 50 - msg.length();
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}
	
	public void packetExitRoom(int slotid) {
		String msg = "" + Server_Settings.SET_EXIT_ROOM + (char) 007 + slotid + (char) 007;

		int length = 50 - msg.length();
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}
	
	public void packetAquaRoomReady(int slotid, boolean ready) {
		String msg = "" + Server_Settings.SET_AQUA_READY + (char) 007 + slotid + (char) 007
				+ ready + (char) 007;

		int length = 50 - msg.length();
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}

	public void packetAquaGamePlayerInfo(int slotid, int teamid, String nick, int dresscode, int x, int y) {

		String msg = "" + Server_Settings.SET_AQUA_ENTERANCE + (char) 007 + slotid + (char) 007 + teamid
				 + (char) 007 + nick + (char) 007 + dresscode + (char) 007 + x + (char) 007 + y + (char) 007;

		int count = 0;

		Matcher m = Pattern.compile("[ㄱ-ㅎㅏ-ㅣ가-힣]").matcher(nick);
		while (m.find()) {
			count++;
		}

		int length = 50 - msg.length() - count * 2;
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}

	public void writeTime(long time) {
		String msg = "" + Server_Settings.SET_DEBUG_TIME + (char)007 + time + (char) 007;

		int length = 50 - msg.length();
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}
	
	public void packetKeepAlive(long time) {
		String msg = "" + Server_Settings.SET_KEEP_ALIVE + (char)007 + time + (char)007;

		int length = 50 - msg.length();
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}
	
	public void packetEnableStartSign(boolean start) {
		String msg = "" + Server_Settings.SET_ENABLE_START + (char) 007 + start + (char) 007;

		int length = 50 - msg.length();
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}

	public void packetStartSign() {
		String msg = "" + Server_Settings.SET_START + (char) 007;

		int length = 50 - msg.length();
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}

	public void packetAquaGamePos(int slotid, long delta, int posx, int posy) {
		String msg = "" + Server_Settings.SET_AQUA_PLAYER_POS + (char) 007  + slotid + (char) 007 + delta + (char) 007 + posx + (char) 007 + posy + (char) 007;

		int length = 50 - msg.length();
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}

	public void packetLobbyMove(int userid, int direction, int posx, int posy) {
		String msg = "" + Server_Settings.SET_LOBBY_PLAYER_POS + (char) 007 + userid + (char) 007 + direction
				+ (char) 007 + posx + (char) 007 + posy + (char) 007;

		int length = 50 - msg.length();
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}

	public void packetMissileMove(int slotid, int teamid, int type, int x, int y, double velx, double vely) {
		String msg = "" + Server_Settings.SET_AQUA_MISSILE_MOVE + (char) 007 + slotid + (char) 007 + teamid + (char) 007 + type
				+ (char) 007 + x + (char) 007 + y + (char) 007 + velx + (char) 007 + vely + (char) 007;

		int length = 50 - msg.length();
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);
		
		writeData(data);
	}

	public void packetLobbyPlayerMove(int pid, int direction) {
		String msg = "" + Server_Settings.SET_LOBBY_PLAYER_MOVE + (char) 007 + pid + (char) 007 + direction
				+ (char) 007;

		int length = 50 - msg.length();
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}

	public void packetAquaPlayerMove(int slotid, int direction, boolean going) {
		String msg = "" + Server_Settings.SET_AQUA_PLAYER_MOVE + (char) 007 + slotid + (char) 007 + direction + (char) 007 + going + (char) 007;

		int length = 50 - msg.length();
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}
	
	public void packetAquaPlayerHit(int slotidx, int slotid, int hp, double accx, double accy) {
		String msg = "" + Server_Settings.SET_AQUA_HIT + (char) 007 + slotidx + (char) 007 + slotid + (char) 007 + hp + (char) 007 + accx + (char) 007 + accy + (char) 007;

		int length = 50 - msg.length();
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}

	public void packetLobbyPlayerText(int pid, String text) {

		/* Calculate the unicode */
		Matcher m = Pattern.compile("[ㄱ-ㅎㅏ-ㅣ가-힣]").matcher(text);
		int count = 0;
		while (m.find()) {
			count++;
		}
		
		int length = text.length() + 2 * count;
		
		if (length > 84)
			return;
		
		/* 
		 * We consider to send msg through two packet 
		 * split the message in half 
		 */
		if (length > 42) {
			
			int index = text.length() / 2; 

			String msg1 = text.substring(0, index);
			String msg2 = text.substring(index);
	
			Matcher m1 = Pattern.compile("[ㄱ-ㅎㅏ-ㅣ가-힣]").matcher(msg1);
			int count1 = 0;
			while (m1.find()) {
				count1++;
			}
			
			Matcher m2 = Pattern.compile("[ㄱ-ㅎㅏ-ㅣ가-힣]").matcher(msg2);
			int count2 = 0;
			while (m2.find()) {
				count2++;
			}
			
			String msg1out = "" + Server_Settings.SET_LOBBY_PLAYER_TEXT + (char)007 + pid + (char)007 + "1" + (char)007 + msg1 + (char)007;
			String msg2out = "" + Server_Settings.SET_LOBBY_PLAYER_TEXT + (char)007 + pid + (char)007 + "2" + (char)007 + msg2 + (char)007;

			int length1 = 50 - msg1out.length() - count1 * 2;
			int length2 = 50 - msg2out.length() - count2 * 2;
			
			for (int i = 0; i < length1; i++) {
				msg1out += "X";
			}
			
			for (int i = 0; i < length2; i++) {
				msg2out += "X";
			}

			byte[] data1 = msg1out.getBytes(StandardCharsets.UTF_8);
			byte[] data2 = msg2out.getBytes(StandardCharsets.UTF_8);

			writeMessage(data1, data2);
			
			return;
		}
		
		/* Othercase, we send msg through single packet */
		String msg = "" + Server_Settings.SET_LOBBY_PLAYER_TEXT + (char)007 + pid + (char)007 + "0" + (char)007 + text + (char)007;

//		System.out.println(msg);
		
		length = 50 - msg.length() - count * 2;
		
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}

	public void packetAquaPlayerText(int slotid, String text) {

		/* Calculate the unicode */
		Matcher m = Pattern.compile("[ㄱ-ㅎㅏ-ㅣ가-힣]").matcher(text);
		int count = 0;
		while (m.find()) {
			count++;
		}
		
		int length = text.length() + 2 * count;
		
		if (length > 84)
			return;
		
		/* 
		 * We consider to send msg through two packet 
		 * split the message in half 
		 */
		if (length > 42) {
			
			int index = text.length() / 2; 

			String msg1 = text.substring(0, index);
			String msg2 = text.substring(index);
	
			Matcher m1 = Pattern.compile("[ㄱ-ㅎㅏ-ㅣ가-힣]").matcher(msg1);
			int count1 = 0;
			while (m1.find()) {
				count1++;
			}
			
			Matcher m2 = Pattern.compile("[ㄱ-ㅎㅏ-ㅣ가-힣]").matcher(msg2);
			int count2 = 0;
			while (m2.find()) {
				count2++;
			}
			
			String msg1out = "" + Server_Settings.SET_AQUA_PLAYER_TEXT + (char)007 + slotid + (char)007 + "1" + (char)007 + msg1 + (char)007;
			String msg2out = "" + Server_Settings.SET_AQUA_PLAYER_TEXT + (char)007 + slotid + (char)007 + "2" + (char)007 + msg2 + (char)007;

			int length1 = 50 - msg1out.length() - count1 * 2;
			int length2 = 50 - msg2out.length() - count2 * 2;
			
			for (int i = 0; i < length1; i++) {
				msg1out += "X";
			}
			
			for (int i = 0; i < length2; i++) {
				msg2out += "X";
			}

			byte[] data1 = msg1out.getBytes(StandardCharsets.UTF_8);
			byte[] data2 = msg2out.getBytes(StandardCharsets.UTF_8);

			writeMessage(data1, data2);
			
			return;
		}
		
		/* Othercase, we send msg through single packet */
		String msg = "" + Server_Settings.SET_AQUA_PLAYER_TEXT + (char)007 + slotid + (char)007 + "0" + (char)007 + text + (char)007;

		length = 50 - msg.length() - count * 2;
		
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}
	
	public void packetNotice(String text) {

		/* Calculate the unicode */
		Matcher m = Pattern.compile("[ㄱ-ㅎㅏ-ㅣ가-힣]").matcher(text);
		int count = 0;
		while (m.find()) {
			count++;
		}
		
		int length = text.length() + 2 * count;
	
		if (length > 88)
			return;
		
		/* 
		 * We consider to send msg through two packet 
		 * split the message in half 
		 */
		if (length > 44) {
			
			int index = text.length() / 2; 

			String msg1 = text.substring(0, index);
			String msg2 = text.substring(index);
	
			Matcher m1 = Pattern.compile("[ㄱ-ㅎㅏ-ㅣ가-힣]").matcher(msg1);
			int count1 = 0;
			while (m1.find()) {
				count1++;
			}
			
			Matcher m2 = Pattern.compile("[ㄱ-ㅎㅏ-ㅣ가-힣]").matcher(msg2);
			int count2 = 0;
			while (m2.find()) {
				count2++;
			}
			
			String msg1out = "" + Server_Settings.SET_NOTICE + (char)007 + "1" + (char)007 + msg1 + (char)007;
			String msg2out = "" + Server_Settings.SET_NOTICE + (char)007 + "2" + (char)007 + msg2 + (char)007;

			int length1 = 50 - msg1out.length() - count1 * 2;
			int length2 = 50 - msg2out.length() - count2 * 2;
			
			for (int i = 0; i < length1; i++) {
				msg1out += "X";
			}
			
			for (int i = 0; i < length2; i++) {
				msg2out += "X";
			}

			byte[] data1 = msg1out.getBytes(StandardCharsets.UTF_8);
			byte[] data2 = msg2out.getBytes(StandardCharsets.UTF_8);

			writeMessage(data1, data2);
			
			return;
		}
		
		/* Othercase, we send msg through single packet */
		String msg = "" + Server_Settings.SET_NOTICE + (char)007 + "0" + (char)007 + text + (char)007;

		length = 50 - msg.length() - count * 2;
		
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}
	
	public void packetAquaRequest() {
		String msg = "" + Server_Settings.SET_AQUA_REQUEST + (char) 007;

		int length = 50 - msg.length();
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}

	public void packetID(int pid) {
		String msg = "" + Server_Settings.SET_ID + (char) 007 + pid + (char) 007;

		int length = 50 - msg.length();
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}
	
	public void packetCurrent(int count) {
		String msg = "" + Server_Settings.SET_CURRENT_USER + (char) 007 + count + (char) 007 ;

		int length = 50 - msg.length();
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}
	
	public void packetVictory(boolean victory) {
		String msg = "" + Server_Settings.SET_AQUA_GAME_RESULT + (char) 007 + victory + (char) 007;

		int length = 50 - msg.length();
		for (int i = 0; i < length; i++)
			msg += "X";

		byte[] data = msg.getBytes(StandardCharsets.UTF_8);

		writeData(data);
	}

	private boolean writeData(byte[] data) {
		try {
			out.write(data);
		} catch (IOException e) {
			System.out.println("[*] writeData error - broken pipe from user " + playerid);
			handler.logoutUser(this);
			return false;
		}
		return true;
	}
	
	private boolean writeMessage(byte[] data1, byte[] data2) {
		try {
			out.write(data1);
			out.write(data2);
		} catch (IOException e) {
			System.out.println("[*] writeMessage error - broken pipe from user" + playerid);
			handler.logoutUser(this);
			return false;
		}
		return true;
	}
	
	private String readData()
	/*
	 * Read a message in the form "<length> msg". The length allows us to know
	 * exactly how many bytes to read to get the complete message. Only the
	 * message part (msg) is returned, or null if there's been a problem.
	 */
	{
		byte[] data = null;

		try {
			data = new byte[50];
			int len = 0;
			// read the message, perhaps requiring several read() calls
			while (len != data.length) {
				int ch = in.read(data, len, data.length - len);

				if (ch == -1) {
					return null;
				}
				len += ch;
			}
		} catch (IOException e) {
			System.out.println("[*] readData error - broken pipe from user" + playerid);
			handler.logoutUser(this);
			return null;
		}

		return new String(data, StandardCharsets.UTF_8).trim();
	}
	
	public void finishConnection() {
		try {
			in.close();
			out.close();
		} catch (IOException e) {
			System.out.println("[*] finishConnection failed");
		}
	}
}
