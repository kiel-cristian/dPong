package cl.dcc.cc5303;

import java.rmi.RemoteException;


public class Match {
	private Server server;
	private int matchID;
	private Player[] players;
	public long[] lastActivity;
	
	private PongSimulation game;
	public ScoreBoardSimple score;
	public HistoricalScoreBoardSimple historical;
	public MigrationInfo migration;
	
	public Match(Server server, int matchID, int minPlayers) {
		this.server = server;
		this.players = new Player[4];
		this.lastActivity = new long[4];
		this.score      = new ScoreBoardSimple();
		this.historical = new HistoricalScoreBoardSimple();
		this.migration = new MigrationInfo();
		this.game = new PongSimulation(this);
		this.matchID = matchID;
		
		this.game.setPlayers(minPlayers);
	}

	public void receiveMigration(GameState migratingState) {
		setGameState(migratingState);
		migration.immigrating = true;
		migration.migratingPlayers = Utils.countTrue(migratingState.playing);
		Utils.setFalse(game.state.playing); // todo false para esperar jugadores
	}
	
	private void setGameState(GameState migratedGameState){
		game.state.fullUpdate(migratedGameState);
		score.setScores(migratedGameState.scores, game.state.playing);
	}
	
	private void startGame() {
		game.state.lastPlayer = -1;
		game.start();
		System.out.println("Empieza el juego! (" + matchID + ")");
	}
	
	public void resetGame() {
		game.state.resetGame();
		score.reset(game.state.playing);
	}
	
	private void resetGameDueMigration() {
       game.state.resetPlayerBars();
	}
	
	protected int addPlayer(Player player) {
		int playerNum = 666;
		if (!game.state.isPlaying(0))
			playerNum = addPlayer(player, 0);
		else if (!game.state.isPlaying(1))		
			playerNum = addPlayer(player, 1);
		else if (!game.state.isPlaying(2))
			playerNum = addPlayer(player, 2);
		else if (!game.state.isPlaying(3))
			playerNum = addPlayer(player, 3);
		return playerNum;
	}
	
	protected int addPlayer(Player player, int num) {
		players[num] = player;
		game.state.enablePlayer(num);
		lastActivity[num] = System.currentTimeMillis();
		System.out.println("Se ha conectado el jugador " + num + " a la partida " + matchID);
		if (playersReady() && !game.running)
			startGame();
		return num;
	}
	
	protected void removePlayer(int playerNum) {
		game.state.disablePlayer(playerNum);
		players[playerNum] = null;
		
		// Remuevo del score historico el jugador
		historical.removePlayer(playerNum);
		
		// Se pone en 0 el puntaje del jugador que se fue
		int[] scores = score.getScores();
		scores[playerNum]  = 0;
		score.setScores(scores, game.state.playing);
		
		System.out.println("Se ha desconectado el jugador " + playerNum + " de la partida " + matchID);
		if(!(this.playersReady())){
			System.out.println("Juego " + matchID + " pausado por falta de jugadores");
			game.end();
		}
		if (Utils.countTrue(game.state.playing) == 0) {
			server.removeMatch(matchID);
		}
	}
	
	protected GameState updatePositions(int playerNum, int position) {
		game.state.updatePlayerPosition(playerNum, position);
		game.state.updateScores(score.getScores());
		lastActivity[playerNum] = System.currentTimeMillis();
		return game.state;
	}
	
	protected GameState lastPositions() {
		game.state.updateScores(score.getScores());
		return game.state;
	}
	
	private boolean playersReady() {
		return playersCount() >= game.state.numPlayers;
	}
	
	public int playersCount() {
		return Utils.countTrue(game.state.playing);
	}
	
	public int getID() {
		return matchID;
	}

	public boolean migrationReady() {
		return migration.migratingPlayers <= Utils.countTrue(game.state.playing);
	}

	public GameState startMigration() {
		migration.emigrating = true;
		game.state.updateScores(score.getScores());
		return game.state;
	}
	
	public void stopMigration(){
		migration.emigrating = false;
		migration.immigrating = false;
		resetGameDueMigration();
	}
	
	public boolean migrating() {
		return migration.emigrating || migration.immigrating;
	}

	public void migratePlayers(IServer targetServer, int targetMatch) throws RemoteException {
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
