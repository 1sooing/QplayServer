public class Server_Looper implements Runnable {

	Server_Handler handler;
	private boolean serverRunning = true;
	
	private int keep_sync_count = 0;
	private int aqua_sync_count = 0;
	
	
	public Server_Looper() {
		this.handler = Server_Handler.getInstance();
	}
	
	/*
	 * Deals with synchronization
	 * In every loop, everything server manages is
	 * server loop time is 500 ms
	 * 
	 * 1. user status
	 * 2. lobby sync
	 * 3. aqua sync
	 */

	@Override
	public void run() {

		long lastLoopTime = System.currentTimeMillis();

		while (serverRunning) {

			long current_time = System.currentTimeMillis();
//			aqua_sync_count++;
//			keep_sync_count++;
			
			long delta = System.currentTimeMillis() - lastLoopTime;
			lastLoopTime = System.currentTimeMillis();
			
			/* 
			 * 1. User Sync 
			 *
			 * Check keep alive
			 */
			
//			if (keep_sync_count > Server_Settings.KEEP_SYNC_COUNT) {
//				keep_sync_count = 0;
//				handler.broadcastKeepAlive();
////				handler.checkKeepAlive(current_time);
//			}
			
			/*
			 *  2. Lobby Sync 
			 * 
			 */
//			handler.broadcastLobbyPos();
			
			
			/*
			 * 3. AquaGame Sync 
			 * every 2000 sec
			 */
//			if (aqua_sync_count > Server_Settings.AQUA_SYNC_COUNT) {
//				aqua_sync_count = 0;
//				handler.broadcastAquaPos(delta);
//			}
			
			try {
				Thread.sleep(30);
			} catch (Exception e) {
			}
		}
		
	}
}
