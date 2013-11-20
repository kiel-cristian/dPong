package cl.dcc.cc5303.server;

import java.rmi.RemoteException;

import cl.dcc.cc5303.GameStateInfo;
import cl.dcc.cc5303.Utils;
import cl.dcc.cc5303.client.PlayerI;


public class ServerMatch {
	private Server server;
	private int matchID;
	private PlayerI[] playerIs;
	private ServerGameThread game;
	public MigrationInfo migration;
	
	public ServerMatch(Server server, int matchID, int minPlayers) {
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
		if (game.playersCount() == 0) {
			game.end();
			server.removeMatch(matchID);
		}
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
	
	protected synchronized void removePlayer(int playerNum) {
		playerIs[playerNum] = null;
		game.removePlayer(playerNum);
		
		System.out.println("Se ha desconectado el jugador " + playerNum + " de la partida " + matchID);
		if(!(this.game.playersReady())){
			stopGame();
		}
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

	public int getID() {
		return matchID;
	}

	public boolean migrationReady() {
		return migration.migratingPlayers <= playersCount();
	}

	public GameStateInfo startMigration(){
		migration.emigrating = true;
		game.updateServerScores();
		return game.state();
	}
	
	public void stopMigration(){
		migration.emigrating = false;
		migration.immigrating = false;
		resetGameDueMigration();
	}
	
	public boolean migrating() {
		return migration.emigrating || migration.immigrating;
	}

	public void migratePlayers(ServerI targetServer, int targetMatch) throws RemoteException {
		for (int i=0; i<playerIs.length; i++) {
			if (playerIs[i] != null) {
				try {
					playerIs[i].migrate(targetServer, targetMatch);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Migracion lista");
	}

	class MigrationInfo {
		public boolean emigrating;
		public boolean immigrating;
		public int migratingPlayers;
	}
}
