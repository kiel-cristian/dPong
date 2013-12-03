package cl.dcc.cc5303.server;

import java.rmi.RemoteException;

import cl.dcc.cc5303.GameStateInfo;
import cl.dcc.cc5303.Utils;
import cl.dcc.cc5303.client.PlayerI;


public class ServerMatch {
	private Server server;
	private String matchID;
	private PlayerI[] playerIs;
	public ServerGameThread game;
	public MigrationInfo migration;
	
	public ServerMatch(Server server, String matchID, int minPlayers) {
		this.server = server;
		this.playerIs = new PlayerI[4];
		this.migration = new MigrationInfo();
		this.matchID = matchID;
		
		this.game = new ServerGameThread(this);
		this.game.setPlayers(minPlayers);
	}

	public void receiveMigration(GameStateInfo migratingState) {
		setGameState(migratingState);
		migration.immigrating = true;
		migration.migratingPlayers = Utils.countTrue(migratingState.playing);
		game.disableAllPlayers();
	}
	
	private void setGameState(GameStateInfo migratedGameState){
		game.fullUpdate(migratedGameState);
		game.setScores(migratedGameState.scores, game.getPlaying());
	}
	
	public void togglePause() {
		game.togglePause();
	}
	
	private void startGame() {
		if(game.alreadyStarted()){
			game.unPause();
		}
		else{
			game.unPause();
			game.start();
		}
		System.out.println("Empieza el juego! (" + matchID + ")");
	}
	
	private void stopGame(){
		System.out.println("Juego " + matchID + " pausado por falta de jugadores");
		game.pause();
	}
	
	private void resetGameDueMigration() {
       game.resetPlayerBars();
	}
	
	public int playersCount(){
		return game.playersCount();
	}
	
	protected int addPlayer(PlayerI playerI) {
		int playerNum = 666;
		if (!game.isPlaying(0))
			playerNum = addPlayer(playerI, 0);
		else if (!game.isPlaying(1))		
			playerNum = addPlayer(playerI, 1);
		else if (!game.isPlaying(2))
			playerNum = addPlayer(playerI, 2);
		else if (!game.isPlaying(3))
			playerNum = addPlayer(playerI, 3);
		return playerNum;
	}
	
	protected int addPlayer(PlayerI playerI, int num) {
		playerIs[num] = playerI;
		game.addPlayer(num);
		System.out.println("Se ha conectado el jugador " + num + " a la partida " + matchID);
		if (this.game.playersReady()){
			startGame();
		}
		return num;
	}
	
	protected synchronized int removePlayer(int playerNum) {
		synchronized(playerIs){
			if (playerIs[playerNum] == null) {
				return this.game.playersCount();
			}
			playerIs[playerNum] = null;
		}
		
		synchronized(game){
			game.removePlayer(playerNum);
		}
		
		System.out.println("Se ha desconectado el jugador " + playerNum + " de la partida " + matchID);
		if(!(this.game.playersReady())){
			stopGame();
		}
		return this.game.playersCount();
	}
	
	protected synchronized void removePlayerByTimeout(int playerNum) {
		if (migrating()){
			return;
		}

		synchronized(playerIs){
			if (playerIs[playerNum] == null) {
				return;
			}
			playerIs[playerNum] = null;
		}
		synchronized(game){
			game.removePlayer(playerNum);
		}
		
		System.out.println("Se ha desconectado el jugador " + playerNum + " de la partida " + matchID + " por inactividad");
		if(!(this.game.playersReady())){
			stopGame();
		}
		server.disconnectPlayerByTimeout(matchID, this.game.playersCount());
	}
	
	
	protected GameStateInfo updatePositions(int playerNum, int position) {
		if(game.working){
			game.serverUpdate(playerNum, position);
		}
		return game.state();
	}
	
	protected GameStateInfo lastPositions() {
		game.updateServerScores();
		return game.state();
	}

	public String getID() {
		return matchID;
	}

	public boolean migrationReady() {
		return migration.migratingPlayers <= playersCount();
	}

	public ServerMatchMigrationInfo startMigration(){
		migration.emigrating = true;
		game.updateServerScores();
		game.startMigration();
		return new ServerMatchMigrationInfo(game.state(), matchID);
	}
	
	public void stopMigration(){
		migration.emigrating = false;
		migration.immigrating = false;
		game.stopMigration();
		resetGameDueMigration();
	}
	
	public boolean migrating() {
		return migration.emigrating || migration.immigrating;
	}

	public void migratePlayers(ServerI targetServer, String targetMatch) throws RemoteException {
		try{
			for (int i=0; i< playerIs.length; i++) {
				if (playerIs[i] != null) {
					playerIs[i].migrate(targetServer, targetMatch);
				}
			}
			System.out.println("Migracion lista");
		} catch (RemoteException e) {
			System.out.println("Error en migración");
		}
	}

	class MigrationInfo {
		public boolean emigrating;
		public boolean immigrating;
		public int migratingPlayers;
	}
}
