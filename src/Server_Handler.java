import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Server_Handler {

	/*
	 * private info
	 */
	private static Server_Handler instance;

	public static int USER_CAPACITY = Server_Settings.PLAYER_MAX_COUNT;
	public static int ROOM_CAPACITY = Server_Settings.ROOM_MAX_COUNT;
	public static int ROOM_MAX_USER = Server_Settings.ROOM_CAPACITY;

	private static final int PLAYER_INITX = 800;
	private static final int PLAYER_INITY = 600;
	
	private int CURRENT_USERS = 0;
	private int CURRENT_LOBBY = 0;
	private int CURRENT_ROOMS = 0;

	private User[] users;
	private LobbyPlayer[] lobbyplayers = new LobbyPlayer[USER_CAPACITY];
	private AquaRoom[] aquarooms = new AquaRoom[ROOM_CAPACITY];
	
	private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	/*
	 * Constructor
	 */
	public Server_Handler(User[] users) {
		this.instance = this;
		this.users = users;
	}

	/*
	 * SingleTon design
	 */
	public static Server_Handler getInstance() {
		// null state is impossible
		return instance;
	}

	public void loginUser(User user) {
		
		Date date = new Date();
		
		int pid = user.getId();
		CURRENT_USERS++;
		
		/* Broadcast current */
		for (int i = 0; i < USER_CAPACITY; i++) {

			if(users[i] == null)
				continue;
			
			users[i].packetCurrent(CURRENT_USERS);
		}
		
		Say("[user" + pid + "] logged in [" + sdf.format(date) + "]");
		
		/* Directly enter lobby. no other choice */
		users[pid].packetID(pid);
	}

	/*
	 * logoutUser is called when user exit lobby
	 * or checking keepalive timed out and server needs to explicitly logged out user.
	 * In this case, we should make USER STATUS to -21. 
	 */
	public void logoutUser(User user) {
		
		if (user == null)
			return;
		
		user.running = false;
		
		int pid = user.getId();
		int status = user.USER_STATUS;
		int roomid = user.roomid;
		int slotid = user.slotid;
		
		switch(status) {
		case 1:
			exitLobby(pid);
			break;
			
		case 2:
			if(slotid == 0)
				removeAquaRoom(roomid);
			else {
				
				/* Broadcast to others */
				for (int i = 0; i < ROOM_MAX_USER; i++) {

					if (aquarooms[roomid].players[i] == null)
						continue;

					int sendpid = aquarooms[roomid].players[i].getId();
					
					if(sendpid == pid)
						continue;
					
					users[sendpid].packetExitAqua(slotid);
				}
				
				aquarooms[roomid].players[slotid].reduceHp(-10);
				
				/* Someone is dead. check whether game is over */
				if( !aquarooms[roomid].players[slotid].isAlive() ) {
					
					if( aquarooms[roomid].ifGameEnded() ) {
						
						int defeated = aquarooms[roomid].getDefeated();
						aquarooms[roomid].roomInit();
						
						/* Broadcast to all game ended */
						for(int i = 0; i < ROOM_MAX_USER; i++) {
							
							if(aquarooms[roomid].players[i] == null)
								continue;
						
							int sendpid = aquarooms[roomid].players[i].getId(); 
							
							/* Lose team */
							if(aquarooms[roomid].players[i].getTeamid() == defeated) {
								users[sendpid].packetVictory(true);
							} else {
								users[sendpid].packetVictory(false);
							}
						}
						exitAqua(roomid, slotid);
						exitRoom(roomid, slotid);

					}
				}
			}
			break;
			
		case 3:
			exitRoom(roomid, slotid);
			break;
		}

		Server.FinishConnetction(pid);
		
		/* Handle data remove events */
		CURRENT_USERS--;
		
		Date date = new Date();
		Say("[user" + pid + "] logged out [" + sdf.format(date) + "]");
		
		/* Broadcast current */
		for (int i = 0; i < USER_CAPACITY; i++) {

			if(users[i] == null)
				continue;
			
			
			users[i].packetCurrent(CURRENT_USERS);
		}
	}

	public void enterLobby(int pid, String nick, int dresscode) {

		users[pid].USER_STATUS = 1;
		CURRENT_LOBBY++;
		
		LobbyPlayer lp = new LobbyPlayer(pid, nick, dresscode, PLAYER_INITX, PLAYER_INITY);
		lobbyplayers[pid] = lp;

		Say("[user" + pid + "[" + nick + "]] entered lobby");
		
		/* Get lobby info */
		for (int i = 0; i < USER_CAPACITY; i++) {

			if(lobbyplayers[i] == null)
				continue;
			
			users[pid].packetLobbyPlayerInfo(lobbyplayers[i].getId(), lobbyplayers[i].getNick(), lobbyplayers[i].getDresscode(), (int) lobbyplayers[i].getX(), (int) lobbyplayers[i].getY());
		}
		
		/* Broadcast to others */
		for(int i = 0; i < USER_CAPACITY; i++) {
			
			if (users[i] == null)
				continue;
			
			if(lobbyplayers[i] == null)
				continue;
			
			if(i == pid)
				continue;
			
			users[i].packetLobbyPlayerInfo(-pid, nick, dresscode, PLAYER_INITX, PLAYER_INITY);
		}
		
	}

	public void exitLobby(int pid) {
		CURRENT_LOBBY--;
		lobbyplayers[pid] = null;
		
//		window.say("[user" + pid + "] exit lobby");
//		window.say("============ Current lobby: " + CURRENT_LOBBY + " ============");
		
		/* Broadcast to others */
		for (int i = 0; i < USER_CAPACITY; i++) {

			if (lobbyplayers[i] == null)
				continue;

			users[i].packetExitLobby(pid);
		}
	}
	
	public int enterRoom(DataOutputStream out, int roomid, int slotid, int teamid, int pid, String nick, int dresscode) {

		if (slotid == -1) {
			/* New to room 
			 * First, Check room is not full 
			 */
			if( aquarooms[roomid].hasSeat() ) {
				slotid = aquarooms[roomid].enterPlayer(out, -1, -1, pid, nick, dresscode);
				teamid = slotid;
				users[pid].USER_STATUS = 3;
				
				users[pid].packetAquaRoomEnter(roomid, slotid);
//				window.say("[user" + pid + "] entered room" + roomid);
			} else {return -1;}
			
			/* Broadcast to others */
			for(int i = 0; i < ROOM_MAX_USER; i++) {
				
				if(aquarooms[roomid].players[i] == null)
					continue;

				int tpid = aquarooms[roomid].players[i].getId();
				users[tpid].packetAquaRoomPlayerInfo(-slotid, teamid, nick, dresscode, false, -1, -1);
			}
			
		} else {
			/* Return to room */
			users[pid].USER_STATUS = 3;
			
			aquarooms[roomid].enterPlayer(out, slotid, teamid, pid, nick, dresscode);
			users[pid].packetAquaRoomEnter(roomid, slotid);
//			window.say("[user" + pid + "] returned to room" + roomid);
		}

		/* Get room info */
		for (int i = 0; i < ROOM_MAX_USER; i++) {

			if(aquarooms[roomid].players[i] == null)
				continue;
			
			users[pid].packetAquaRoomPlayerInfo(
					i,
					aquarooms[roomid].players[i].getTeamid(),
					aquarooms[roomid].players[i].getNick(), 
					aquarooms[roomid].players[i].getDresscode(),
					aquarooms[roomid].players[i].getReady()
					, -1, -1);
		}
		return slotid;
	}

	public void exitRoom(int roomid, int slotid) {
		
//		window.say("player" + slotid + " exit room" + roomid + "[" + aquarooms[roomid].roomname + "]");
		
		/* Boss left the room, room is removed */
		if(slotid == 0) {
			removeAquaRoom(roomid);
		}
		
		else {
			
			/* Broadcast to others */
			for (int i = 0; i < ROOM_MAX_USER; i++) {

				if (aquarooms[roomid].players[i] == null)
					continue;
				
				if (i == slotid)
					continue;
				
				int pid = aquarooms[roomid].players[i].getId();
				
				users[pid].packetExitRoom(slotid);
			}
			
			aquarooms[roomid].exitPlayer(slotid);
		}
	}

	public void aquaGameStart(int roomid) {
		
		/* Init users position */
		aquarooms[roomid].startGame();
		
		/* Write start to all users */
		for(int i = 0; i < ROOM_MAX_USER; i++) {
			
			if (aquarooms[roomid].players[i] == null)
				continue;
			
			int pid = aquarooms[roomid].players[i].getId();
			
			users[pid].packetStartSign();
			users[pid].USER_STATUS = 2;
		}
		
		/* Get all users info */
		for (int i = 0; i < ROOM_MAX_USER; i++) {

			if(aquarooms[roomid].players[i] == null)
				continue;
			
			int pid = aquarooms[roomid].players[i].getId();
			
			for(int j = 0; j < ROOM_MAX_USER; j++) {
				
				if(aquarooms[roomid].players[j] == null)
					continue;
				
				users[pid].packetAquaGamePlayerInfo(
						j, 
						aquarooms[roomid].players[j].getTeamid(), 
						aquarooms[roomid].players[j].getNick(), 
						aquarooms[roomid].players[j].getDresscode(), 
						aquarooms[roomid].players[j].getX(),
						aquarooms[roomid].players[j].getY());				
			}
		}
	}
	
	public void enterAqua(int roomid, int slotid, int teamid, String nick, int dresscode) {

		/* Broadcast to others */
		for (int i = 0; i < ROOM_MAX_USER; i++) {

			if (aquarooms[roomid].players[i] == null)
				continue;
			
			/* Except mine */
			if ( i == slotid)
				continue; 
			
			int pid = aquarooms[roomid].players[i].getId();

			users[pid].packetAquaGamePlayerInfo(slotid, teamid, nick, dresscode, aquarooms[roomid].players[i].getX(), aquarooms[roomid].players[i].getY());
		}
	}
	
	public void exitAqua(int roomid, int slotid) {
		
		/* Broadcast to others */
		for (int i = 0; i < ROOM_MAX_USER; i++) {

			if (aquarooms[roomid].players[i] == null)
				continue;

			/* Except mine */
			if ( i == slotid)
				continue; 
		
			int pid = aquarooms[roomid].players[i].getId();
			
			users[pid].packetExitAqua(slotid);
		}
	}
	
	public int createAquaRoom(String roomname) {

		int roomid = -1;

		/* Search empty id */
		for (int i = 0; i < ROOM_CAPACITY; i++) {
			if (aquarooms[i] == null) {
				roomid = i;
				break;
			}
		}

		AquaRoom ar = new AquaRoom(roomid, roomname);
		aquarooms[roomid] = ar;

		CURRENT_ROOMS++;
//		window.say("create room " + roomid + " [" + roomname + "]");
//		window.say("============ Current room: " + CURRENT_ROOMS + " ============");

		return roomid;
	}
	
	public void removeAquaRoom(int roomid) {
		
		/* 
		 * Broadcast to others 
		 * Except for the boss 
		 * So i != 0
		 */
		
		for (int i = 1; i < ROOM_MAX_USER; i++) {

			if (aquarooms[roomid].players[i] == null)
				continue;
			
			int pid = aquarooms[roomid].players[i].getId();
			
			users[pid].packetLobbyEnterAccept();
			
			enterLobby(users[pid].playerid, users[pid].nick, users[pid].dresscode);
		}
		
		aquarooms[roomid] = null;
		CURRENT_ROOMS--;		
//		window.say("remove room " + roomid);
//		window.say("============ Current room: " + CURRENT_ROOMS + " ============");
	}
	
	public void showAquaRoom(int pid) {
		
		/* Get room info */
		for (int i = 0; i < ROOM_CAPACITY; i++) {

			if(aquarooms[i] == null)
				continue;

			users[pid].packetAquaRoomInfo(aquarooms[i].roomid, aquarooms[i].roomname, aquarooms[i].capacity);
		}
	}
	
	public void moveInLobby(int pid, int direction) {
		
		// Broadcast to others
		for (int i = 0; i < USER_CAPACITY; i++) {

			if (lobbyplayers[i] == null)
				continue;

			users[i].packetLobbyPlayerMove(pid, direction);
		}
	}

	public void sayInLobby(int pid, String text ) {
		
		for (int i = 0; i < USER_CAPACITY; i++) {

			if (lobbyplayers[i] == null)
				continue;

			users[i].packetLobbyPlayerText(pid, text);
		}
	}
	
	public void sayInRoom(int roomid, int slotid, String text ) {

		for (int i = 0; i < ROOM_MAX_USER; i++) {

			if (aquarooms[roomid].players[i] == null)
				continue;

			int pid = aquarooms[roomid].players[i].getId();
			
			users[pid].packetAquaPlayerText(slotid, text);
		}
	}
	
	public void dressInLobby(int pid, int dresscode) {
		
		lobbyplayers[pid].setDresscode(dresscode);
		
		for (int i = 0; i < USER_CAPACITY; i++) {

			if (lobbyplayers[i] == null)
				continue;

			users[i].packetLobbyPlayerDresscode(pid, dresscode);
		}
	}

	public void teamInRoom(int roomid, int slotid, int teamid) {
		
		aquarooms[roomid].players[slotid].setTeamid(teamid);
		
//		System.out.println("player " + slotid + ", team: " + teamid);
		
		for (int i = 0; i < ROOM_MAX_USER; i++) {

			if (aquarooms[roomid].players[i] == null)
				continue;

			int pid = aquarooms[roomid].players[i].getId();
			
			users[pid].packetRoomPlayerTeamid(slotid, teamid);
		}
	}

	public void readyAquaRoomPlayer(int roomid, int slotid, boolean ready) {
		
		aquarooms[roomid].playerReady(slotid, ready);
		
		/* Broadcast to others */
		for (int i = 0; i < ROOM_MAX_USER; i++) {

			if (aquarooms[roomid].players[i] == null)
				continue;
			
			int pid = aquarooms[roomid].players[i].getId();

			users[pid].packetAquaRoomReady(slotid, ready);
		}
		
		/* Check we if can start */
		users[aquarooms[roomid].getBoss()].packetEnableStartSign(aquarooms[roomid].isEnableStart());
	}
	
	public void moveInAquaGame(int roomid, int slotid, boolean direction, double acc) {

		aquarooms[roomid].playerMovement(slotid, direction, acc);
	}
	
	public void hitMoveInAquaGame(int slotidx, int roomid, int slotid, int damage, double accx, double accy) {

		aquarooms[roomid].players[slotid].reduceHp(damage);
		
		/* Broadcast hit info to others */
		for (int i = 0; i < ROOM_MAX_USER; i++) {

			if (aquarooms[roomid].players[i] == null)
				continue;
			
			int pid = aquarooms[roomid].players[i].getId();
			
			users[pid].packetAquaPlayerHit(slotidx, slotid, aquarooms[roomid].players[slotid].getHp(), accx, accy);
		}
		
		/* Someone is dead. check whether game is over */
		if( !aquarooms[roomid].players[slotid].isAlive() ) {
			
//			System.out.println("[*] player " + slotid + " is dead");
			
			aquarooms[roomid].players[slotidx].increaseKilled();
			
			if( aquarooms[roomid].ifGameEnded() ) {
				
				int won = aquarooms[roomid].getWon();
				
				/* Broadcast to all game ended */
				for(int i = 0; i < ROOM_MAX_USER; i++) {
					
					if(aquarooms[roomid].players[i] == null)
						continue;

					int pid = aquarooms[roomid].players[i].getId();
					
					/* Won team */
					if(aquarooms[roomid].players[i].getTeamid() == won) {
						users[pid].packetVictory(true);
						aquarooms[roomid].players[i].increaseWon();
					
					/* Lose team */
					} else {
						users[pid].packetVictory(false);
						aquarooms[roomid].players[i].increaseLost();
					}
				}
				
				aquarooms[roomid].gameEnded();
				return;
			}
		}
	}
	
	public void shotInAquaGame(int roomid, int slotid, int teamid, int type, int x, int y, double velx, double vely) {
		
//		aquarooms[roomid].missiles[slotid].type = type;
		
		/* Broadcast to others */
		for (int i = 0; i < ROOM_MAX_USER; i++) {

			if (aquarooms[roomid].players[i] == null)
				continue;

			int pid = aquarooms[roomid].players[i].getId();
			
			users[pid].packetMissileMove(slotid, teamid, type, (int) x, (int) y, velx, vely);
		}
	}
	
	public void broadcastLobbyPos() {
		
		/* Broadcast server-side positions */
		for(int i = 0; i < USER_CAPACITY; i++) {
			
			/* To user */
			if(lobbyplayers[i] == null)
				continue;
			
			for(int j = 0; j < USER_CAPACITY; j++) {
				
				/* Of users */
				if(lobbyplayers[j] == null)
					continue;
				
				/*
				 * Write
				 * 1. userid 
				 * 2. direction
				 * 3. posx
				 * 4. posy 
				 */
				users[i].packetLobbyMove(j, lobbyplayers[j].getDirection(), lobbyplayers[j].getX(), lobbyplayers[j].getY());
			}
		}
	}

	public void setAquaPos(int roomid, int slotid, int posx, int posy) {
		aquarooms[roomid].players[slotid].setLocation(posx, posy);
	}
	
	public void setLobbyPos(int pid, int posx, int posy) {
		lobbyplayers[pid].setLocation(posx, posy);
	}
	
	public void broadcastAquaPos(long delta) {
		
		/* Search existing aqua rooms */
		for (int j = 0; j < ROOM_CAPACITY; j++) {
			
			if (aquarooms[j] == null)
				continue;
			
			if (aquarooms[j].getPlaying()) {

				/* Broadcast server-side positions */
				for(int i = 0; i < ROOM_MAX_USER; i++) {
					
					if(aquarooms[j].players[i] == null)
						continue;
					
					int pid = aquarooms[j].players[i].getId();
					
					/* Of players */
					for(int k = 0; k < ROOM_MAX_USER; k++) {
						
						if(aquarooms[j].players[k] == null)
							continue;
						
						/* Except mine */
						if (k == i)
							continue;
						
						users[pid].packetAquaGamePos(k, delta, aquarooms[j].players[k].getX(), aquarooms[j].players[k].getY());
					}
				}			
				
			}
		}
	}

	public void broadcastKeepAlive() {
		
		for(int i = 0; i < USER_CAPACITY; i++) {
			
			if(users[i] == null || users[i].USER_STATUS == -21)
				continue;
			
			users[i].packetKeepAlive(System.currentTimeMillis());
		}
	}
	
//	public void checkKeepAlive(long currenttime) {
//		
//		for (int i = 0; i < USER_CAPACITY; i++) {
//			
//			if(users[i] == null)
//				continue;
//			
//			/* 
//			 * User not responding during timeout
//			 * consider these users as logged out
//			 * current timeout is 2000 ms (3 sec)
//			 */
//			if(currenttime - users[i].recentAlive > Server_Settings.USER_ALIVE_TIMEOUT) {
//				
////				Say("[user" + i + "] do not responding...");
////				logoutUser(users[i]);
//			}
//			
//		}
//	}
	
	public void broadcastTime(int roomid, long time) {

		/* Broadcast to others */
		for (int i = 0; i < ROOM_MAX_USER; i++) {

			if (aquarooms[roomid].players[i] == null)
				continue;

			int pid = aquarooms[roomid].players[i].getId();
			
			users[pid].writeTime(time);
		}
	}
	
	public void lobbyNotice(int pid) {
		
		URL path = ClassLoader.getSystemResource("notice.txt");
		File f = null;
		String notice = "";
		
		try {
			f = new File(path.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			notice = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		users[pid].packetNotice(notice);
	}
	
	public void Say(String msg) {
		System.out.println(msg);
	}
}
