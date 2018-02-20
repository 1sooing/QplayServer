/*
 * Server side LobbyPlayer
 */

public class LobbyPlayer {

	private int playerid = -1;
	private String nick = "";
	private int dresscode = -1;
	
	private int x = -1;
	private int y = -1;
	
	private int direction = -1;
	
	public LobbyPlayer(int pid, String nick, int dresscode, int x, int y) {
		this.playerid = pid;
		this.nick = nick;
		this.dresscode = dresscode;
		this.x = x;
		this.y = y;
	}

	public int getId() {
		return this.playerid;
	}
	
	public String getNick() {
		return this.nick;
	}
	
	public int getDresscode() {
		return this.dresscode;
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public int getDirection() {
		return this.direction;
	}
	
	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void setDresscode(int dresscode) {
		this.dresscode = dresscode;
	}
	
	public void playerClear() {
		playerid = -1;
		nick = "";
	}
	
}
