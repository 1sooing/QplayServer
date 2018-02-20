// SERVER SIDE COMMAND

public final class Server_Settings {
	
	public static final int LATEST_CLIENT_VERSION = 6;
	
	public static final int SERVER_PORT = 5292;
	public static final int PLAYER_MAX_COUNT = 20;
	public static final int AQUA_PLAYER_MAX_COUNT = 2;
	public static final int AQUA_PLAYER_MAX_HP = 10;
	public static final int ROOM_MAX_COUNT = 10;
	public static final int ROOM_CAPACITY = 6;
	public static final int USER_ALIVE_TIMEOUT = 10000;
	public static final int AQUA_SYNC_COUNT = 200;
	public static final int KEEP_SYNC_COUNT = 40;
	
	/*
	 * 0 ~ 19: COMMON
	 * 20 ~ 39: LOBBY
	 * 40 ~ 59: AQUA	
	 * 60 ~   : NOT RESERVED 
	 */
	
	// COMMON
	public static final int SET_DEFAULT = 0;
	public static final int SET_ID = 1;
	public static final int SET_LOGOUT = 2;
	public static final int SET_EXIT_AQUA = 3;
	public static final int SET_EXIT_ROOM = 4;
	public static final int SET_EXIT_LOBBY = 5;
	public static final int SET_DEBUG_TIME = 6;
	public static final int SET_KEEP_ALIVE = 7;
	public static final int SET_VERSION = 8;
	
	// LOBBY
	public static final int SET_LOBBY_ENTRANCE = 20;
	public static final int SET_LOBBY_PLAYER_MOVE = 21;
	public static final int SET_LOBBY_PLAYER_POS = 22;
	public static final int SET_LOBBY_PLAYER_TEXT = 23;
	public static final int EXIT_LOBBY = 25;
	public static final int SET_AQUA_ROOM_INFO = 26;
	public static final int SET_AQUA_ROOM_CREATE_REQUEST = 27;
	public static final int SET_AQUA_ROOM_ENTER_REQUEST = 28;
	public static final int SET_AQUA_ROOM_ENTER_ACCEPT = 29;
	public static final int SET_AQUA_ROOM_PLAYER_INFO = 30;
	public static final int SET_AQUA_ROOM_REQUEST = 31;
	public static final int SET_ENABLE_START = 32;
	public static final int SET_LOBBY_ENTER_REQUEST = 33;
	public static final int SET_LOBBY_ENTER_ACCEPT = 34;
	public static final int SET_LOBBY_DRESSCODE = 35;
	public static final int SET_NOTICE = 36;
	public static final int SET_CURRENT_USER = 37;
	
	// AQUA
	public static final int SET_AQUA = 40;
	public static final int SET_AQUA_PLAYER_POS = 41;
	public static final int SET_AQUA_CHILD_POS = 43;
	public static final int SET_RESTART = 44;
	public static final int SET_START = 45;
	public static final int SET_AQUA_READY = 46;
	public static final int SET_AQUA_PLAYER_TEXT = 47;
	public static final int SET_AQUA_PLAYER_MOVE = 48;
	public static final int SET_AQUA_REQUEST = 49;
	public static final int SET_AQUA_ENTERANCE = 50;
	public static final int SET_AQUA_MISSILE_MOVE = 51;
	public static final int SET_AQUA_GAME_RESULT = 52;
	public static final int SET_AQUA_HIT = 53;
	public static final int SET_AQUA_TEAM = 54;
}
