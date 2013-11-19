package cl.dcc.cc5303;

import java.rmi.RemoteException;


public class Match {
	private Server server;
	private int matchID;
	private Player[] players;
	private PongSimulation game;
	public MigrationInfo migration;
	
	public Match(Server server, int matchID, int minPlayers) {
		this.server = server;
		this.players = new Player[4];
		this.migration = new MigrationInfo();
		this.matchID = matchID;
		
		this.game = new PongSimulation(this);
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
	
	protected int addPlayer(Player player) {
		int playerNum = 666;
		if (!game.isPlaying(0))
			playerNum = addPlayer(player, 0);
		else if (!game.isPlaying(1))		
			playerNum = addPlayer(player, 1);
		else if (!game.isPlaying(2))
			playerNum = addPlayer(player, 2);
		else if (!game.isPlaying(3))
			playerNum = addPlayer(player, 3);
		return playerNum;
	}
	
	protected int addPlayer(Player player, int num) {
		players[num] = player;
		game.addPlayer(num);
		System.out.println("Se ha conectado el jugador " + num + " a la partida " + matchID);
		if (this.game.playersReady()){
			startGame();
		}
		return num;
	}
	
	protected synchronized void removePlayer(int playerNum) {
		players[playerNum] = null;
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
		for (int i=0; i<players.length; i++) {
			if (players[i] != null) {
				try {
					players[i].migrate(targetServer, targetMatch);
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
