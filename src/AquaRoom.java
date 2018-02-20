import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class AquaRoom {

	public int defeated = -1;
	public int won = -1;

	private int teamNo = 0;

	public int roomid = -1;
	public String roomname = "";
	public int capacity = 6;
	public int capacity_current = 0;
	private boolean playing = false;

	AquaPlayer[] players = new AquaPlayer[capacity];
	AquaMissile[] missiles = new AquaMissile[capacity];
	int[] result = new int[] { -1, -1, -1, -1, -1, -1 };

	AquaGameLooper agl;
	Thread aquagame_start;
	
	public AquaRoom(int roomid, String roomname) {
		this.roomid = roomid;
		this.roomname = roomname;
		this.capacity_current = 0;
	}

	public void playerReady(int slotid, boolean ready) {
		players[slotid].ready = ready;
	}

	/*
	 * We should check if there are at least two teams
	 */
	public boolean isEnableStart() {

		boolean isAllReady = true;
		int teamcount = 0;
		result = new int[] { -1, -1, -1, -1, -1, -1 };
		
		// except boss
		for (int i = 1; i < capacity; i++) {

			if (players[i] == null)
				continue;

			if (players[i].ready == false)
				isAllReady = false;
		}

		// at least two teams
		// select team
		for (int i = 0; i < capacity; i++) {

			for (int j = 0; j < capacity; j++) {

				if (players[j] == null)
					continue;

				if (players[j].getTeamid() == i && result[i] != 1) {
					teamcount++;
					
					/* Check the team with 1 */
					result[i] = 1;
//					System.out.println("[*] team " + i + " is searched");
					continue;
				}
			}
		}

		teamNo = teamcount;
//		System.out.println("teamNo: " + teamNo);

		return (isAllReady && (teamcount > 1));
	}
	
	public void startGame() {
		this.playing = true;
		initGamePosition();
		
		/* Run game thread */
		agl = new AquaGameLooper(players);
		aquagame_start = new Thread(agl);
		aquagame_start.start();
	}

	public void gameEnded() {
		this.playing = false;
		roomInit();
	}
	
	public boolean getPlaying() {
		return this.playing;
	}

	public void playerMovement(int slotid, boolean direction, double acc) {
		
		players[slotid].movement_changed = true;
		
		if(direction) {
			players[slotid].accX = acc;
		} else {
			players[slotid].accY = acc;
		}
	}
	
	
	/*
	 * Map size if 2490 x 1344
	 * 
	 */
	private void initGamePosition() {
		int count = 0;

		switch (capacity_current) {
		case 2:
			String[] pos2 = {"06000900", "19000900"};
			shuffleArray(pos2);
			
			for (int i = 0; i < capacity; i++) {
				if (players[i] != null) {
					
					int posx = Integer.parseInt(pos2[count].substring(0, 4));
					int posy = Integer.parseInt(pos2[count].substring(4, 8));
					
					players[i].setLocation(posx, posy);
					count++;
				}
			}
			break;

		case 3:
			String[] pos3 = {"06000400", "12500900", "19000400"};
			shuffleArray(pos3);
			
			for (int i = 0; i < capacity; i++) {
				if (players[i] != null) {
					
					int posx = Integer.parseInt(pos3[count].substring(0, 4));
					int posy = Integer.parseInt(pos3[count].substring(4, 8));
					
					players[i].setLocation(posx, posy);
					count++;
				}
			}
			break;

		case 4:
			String[] pos4 = {"06000900", "19000900", "07000400", "18000400"};
			shuffleArray(pos4);
			
			for (int i = 0; i < capacity; i++) {
				if (players[i] != null) {
					
					int posx = Integer.parseInt(pos4[count].substring(0, 4));
					int posy = Integer.parseInt(pos4[count].substring(4, 8));
					
					players[i].setLocation(posx, posy);
					count++;
				}
			}
			break;

		case 5:
			String[] pos5 = {"06000900", "19000900", "06000400", "19000400", "12500650"};
			shuffleArray(pos5);
			
			for (int i = 0; i < capacity; i++) {
				if (players[i] != null) {
					
					int posx = Integer.parseInt(pos5[count].substring(0, 4));
					int posy = Integer.parseInt(pos5[count].substring(4, 8));
					
					players[i].setLocation(posx, posy);
					count++;
				}
			}
			break;

		case 6:
			String[] pos6 = {"03000900", "09500900", "15500900", "22000900", "03250400", "12250400"};
			shuffleArray(pos6);
			
			for (int i = 0; i < capacity; i++) {
				if (players[i] != null) {
					
					int posx = Integer.parseInt(pos6[count].substring(0, 4));
					int posy = Integer.parseInt(pos6[count].substring(4, 8));
					
					players[i].setLocation(posx, posy);
					count++;
				}
			}
			break;

		default:
			System.out.println("Critical error[*] - game user less than 2");
			break;
		}
	}

	public boolean ifGameEnded() {

		/*
		 * We consider game is not ended because, we will discriminate defeated
		 * team first if game is not ended, we still have at least alive player
		 * in each team
		 */

		int defeatedNo = 0;

		/* Monitor 'j'th team */
		for (int j = 0; j < capacity; j++) {

			boolean atLeastOneAlive = false;
			boolean atLeastOnePlayer = false;

			/*
			 * Team validation test every team has at least one active player
			 * 
			 * Continue until only one team left
			 */
			for (int i = 0; i < capacity; i++) {
				if (players[i] == null)
					continue;

				if (players[i].getTeamid() != j)
					continue;

				atLeastOnePlayer = true;
			}

			for (int i = 0; i < capacity; i++) {

				/*
				 * Similarly we consider there is at least one defeated team. in
				 * checking process, we filter out team of least one player is
				 * alive
				 */
				if (players[i] == null)
					continue;

				if (players[i].getTeamid() != j)
					continue;

				/* this team not lose */
				if (players[i].isAlive()) {
//					System.out.println("player " + i + ", team: " + players[i].teamid + " is alive");
					atLeastOneAlive = true;
				}
			}

			/* Check lost team */
			if (!atLeastOneAlive && atLeastOnePlayer) {
				defeated = j;
				result[j] = 0;

				defeatedNo++;
//				System.out.println("Defeated team: " + defeated + ", " + teamNo + "/" + defeatedNo);

				/* Check if only one team left and search the team */
				if (defeatedNo >= teamNo - 1) {

					for (int k = 0; k < capacity; k++) {
						if (result[k] == 1) {
							won = k;
//							System.out.println("won team: " + k);
//							gameEnded();
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public boolean hasSeat() {
		return (capacity_current < capacity);
	}

	public int getBoss() {
		return players[0].playerid;
	}

	public int getDefeated() {
		return this.defeated;
	}

	public int getWon() {
		return this.won;
	}

	public int enterPlayer(DataOutputStream out, int slotid, int teamid, int pid, String nick, int dresscode) {

		AquaPlayer ap = new AquaPlayer(out, teamid, pid, nick, dresscode);

		if (slotid == -1) {
			for (int i = 0; i < capacity; i++) {

				// search empty slot
				if (players[i] == null) {

					capacity_current++;
					players[i] = ap;

					/*
					 * 
					 * Now teamid is slotid temporarily
					 * 
					 */
					players[i].setTeamid(i);

					AquaMissile am = new AquaMissile(-1, -1);
					missiles[i] = am;
					return i;
				}
			}
			return -1;
		}

		else {
			capacity_current++;
			return slotid;
		}
	}

	public void exitPlayer(int slotid) {
		players[slotid] = null;
		missiles[slotid] = null;
		capacity_current--;
	}

	public void matchTeam() {

	}

	public void roomInit() {
		defeated = -1;
		won = -1;

		result = new int[] { 0, 0, 0, 0, 0, 0 };
		capacity_current = 0;
		playing = false;
		for (int i = 0; i < capacity; i++) {

			if (players[i] == null)
				continue;

			players[i].playerInit();

		}
	}

	private static void shuffleArray(String[] array)
	{
	    int index;
	    String temp;
	    Random random = new Random();
	    for (int i = array.length - 1; i > 0; i--)
	    {
	        index = random.nextInt(i + 1);
	        temp = array[index];
	        array[index] = array[i];
	        array[i] = temp;
	    }
	}
	
	class AquaPlayer {

		private DataOutputStream out;
		
		private boolean ready = false;
		private boolean alive = true;
		private int killed = 0;
		private int won = 0;
		private int lost = 0;
		
		private int teamid = -1;
		private int playerid = -1;
		private String nick = "";
		private int dresscode = -1;

		private boolean movement_changed = false;
		private boolean directionX;
		private boolean directionY;
		private double accX;
		private double accY;
		
		private double x;
		private double y;

		int hp = Server_Settings.AQUA_PLAYER_MAX_HP;

		public AquaPlayer(DataOutputStream out, int teamid, int playerid, String nick, int dresscode) {

			/*
			 * 
			 * Now teamid is slotid temporarily alive is default true, because
			 * discriminating result based on all-death-test
			 */
			this.out = out;
			
			this.teamid = teamid;
			this.playerid = playerid;
			this.nick = nick;
			this.dresscode = dresscode;
			this.hp = Server_Settings.AQUA_PLAYER_MAX_HP;
			this.alive = true;
		}

		public boolean getReady() {
			return this.ready;
		}
		
		public boolean isAlive() {
			return this.alive;
		}

		public int getId() {
			return this.playerid;
		}

		public int getTeamid() {
			return this.teamid;
		}

		public void setTeamid(int teamid) {
			this.teamid = teamid;
		}

		public String getNick() {
			return this.nick;
		}

		public int getDresscode() {
			return this.dresscode;
		}

		public void increaseWon() {
			this.won++;
		}
		
		public int getWon() {
			return this.won;
		}
		
		public void increaseKilled() {
			this.killed++;
		}
		
		public int getKilled() {
			return this.killed;
		}
		
		public void increaseLost() {
			this.lost++;
		}
		
		public int getLost() {
			return this.lost;
		}
		
		public int getHp() {
			return hp;
		}

		public void setHp(int hp) {
			this.hp = hp;

			if (this.hp <= 0)
				this.alive = false;
		}

		public void reduceHp(int damage) {
			hp += damage;

			if (hp <= 0)
				alive = false;
		}

		public int getX() {
			return (int) x;
		}

		public int getY() {
			return (int) y;
		}

		public void setLocation(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public void playerInit() {
			ready = false;
			alive = true;
			x = -1;
			y = -1;
			hp = Server_Settings.AQUA_PLAYER_MAX_HP;
		}
		
		public void packetAquaPlayerMove(int slotid, boolean direction, double acc, long delta) {
			
			String msg = "" + Server_Settings.SET_AQUA_PLAYER_MOVE + (char) 007 + slotid + (char) 007 + direction + (char) 007 + acc + (char) 007 + delta + (char) 007;

//			System.out.println(msg);
			
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
				return false;
			}
			return true;
		}
	}

	class AquaMissile {

		int teamid;
		int type;

		public AquaMissile(int teamid, int type) {
			this.teamid = teamid;
			this.type = type;
		}
	}
	
	class AquaGameLooper implements Runnable {

		private boolean playing = true;
		private AquaPlayer[] players;
		
		public AquaGameLooper(AquaPlayer[] players) {
			this.playing = true;
			this.players = players;
		}
		
		/* Initiate game variables and stop the thread */
		public void gameEnded() {
			this.playing =false;
		}
		
		public void setPlaying(boolean playing) {
			this.playing = playing;
		}
		
		public boolean getPlaying() {
			return this.playing;
		}

		@Override
		public void run() {

			long lastLoopTime = System.currentTimeMillis();
			
			while (true) {
				
				long delta = System.currentTimeMillis() - lastLoopTime;
				lastLoopTime = System.currentTimeMillis();
				
				/* Broadcast movement change */
				for(int i = 0; i < capacity; i++) {
					
					if(players[i] == null)
						continue;

					// has change
//					if(players[i].movement_changed) {
						
						for(int j = 0; j < capacity; j++) {
							
							if(players[j] == null)
								continue;
						
							players[j].packetAquaPlayerMove(i, true, players[i].accX, delta);
							players[j].packetAquaPlayerMove(i, false, players[i].accY, delta);
						}
						
						// initiate movement change
						players[i].movement_changed = false;
//					}
				}
				
				// server-side game unit loop time
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {e.printStackTrace();
				}
			}
			
		}
		
	}


}
