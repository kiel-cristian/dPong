package cl.dcc.cc5303;

import java.rmi.RemoteException;


public class Match {
	private static final long INACTIVITY_TIMEOUT = 3000;
	private Server server;
	private int matchID;
	private boolean[] playing;
	private Rectangle[] bars;
	private int lastPlayer;
	private PongBall ball;
	private Player[] players;
	private long[] lastActivity;
	private int minPlayers;
	private Thread simulationThread;
	private ScoreBoardSimple score;
	private boolean winner;
	private int winnerPlayer;
	private boolean running;
	private MigrationInfo migration;
	
	public Match(Server server, int matchID, int minPlayers) {
		this.server = server;
		bars = new Rectangle[4];
		bars[0] = new Rectangle(10, Pong.HEIGHT / 2, 10, 100);
		bars[1] = new Rectangle(Pong.WIDTH - 10, Pong.HEIGHT / 2, 10, 100);
		bars[2] = new Rectangle(Pong.WIDTH/2, Pong.HEIGHT - 10, 100, 10);
		bars[3] = new Rectangle(Pong.WIDTH/2, 10, 100, 10);

		ball = new PongBall();
		playing = new boolean[4];
		players = new Player[4];
		lastActivity = new long[4];
		score      = new ScoreBoardSimple();
		winner     = false;
		winnerPlayer = -1;
		migration = new MigrationInfo();
		this.matchID = matchID;
		this.minPlayers = minPlayers;
	}

	public void receiveMigration(GameState migratingState) {
		setGameState(migratingState);
		migration.migratingPlayers = Utils.countTrue(migratingState.playing);
		Utils.setFalse(playing); // todo false para esperar jugadores
	}
	
	private void setGameState(GameState migratedGameState){
		this.ball.x = migratedGameState.ballX;
		this.ball.y = migratedGameState.ballY;
		this.ball.vx = migratedGameState.vx;
		this.ball.vy = migratedGameState.vy;
		
		for(int i = 0; i < Pong.MAX_PLAYERS; i++){
			this.playing[i] = migratedGameState.playing[i];
			if(migratedGameState.playing[i]){
				if(i == 0 || i == 1){
					bars[i].x = migratedGameState.barsPos[i];
				}
				else{
					bars[i].y = migratedGameState.barsPos[i];
				}
			}
		}
	}
	
	private void startGame() {
		simulationThread = new PongSimulation();
		running = true;
		lastPlayer = -1;
		simulationThread.start();
		System.out.println("Empieza el juego! (" + matchID + ")");
	}
	
	private void resetGame() {
		bars[0] = new Rectangle(10, Pong.HEIGHT / 2, 10, 100);
		bars[1] = new Rectangle(Pong.WIDTH - 10, Pong.HEIGHT / 2, 10, 100);
		bars[2] = new Rectangle(Pong.WIDTH/2, Pong.HEIGHT - 10, 100, 10);
		bars[3] = new Rectangle(Pong.WIDTH/2, 10, 100, 10);
		ball    = new PongBall();
		lastPlayer = -1;
		winner     = false;
		score.reset();
	}
	
	private void resetGameDueMigration() {
		bars[0] = new Rectangle(10, Pong.HEIGHT / 2, 10, 100);
		bars[1] = new Rectangle(Pong.WIDTH - 10, Pong.HEIGHT / 2, 10, 100);
		bars[2] = new Rectangle(Pong.WIDTH/2, Pong.HEIGHT - 10, 100, 10);
		bars[3] = new Rectangle(Pong.WIDTH/2, 10, 100, 10);
		ball    = new PongBall();
	}
	
	protected int addPlayer(Player player) {
		int playerNum = 666;
		if (!playing[0])
			playerNum = addPlayer(player, 0);
		else if (!playing[1])		
			playerNum = addPlayer(player, 1);
		else if (!playing[2])
			playerNum = addPlayer(player, 2);
		else if (!playing[3])
			playerNum = addPlayer(player, 3);
		return playerNum;
	}
	
	protected int addPlayer(Player player, int num) {
		players[num] = player;
		playing[num] = true;
		lastActivity[num] = System.currentTimeMillis();
		System.out.println("Se ha conectado el jugador " + num + " a la partida " + matchID);
		if (playersReady() && !running)
			startGame();
		return num;
	}
	
	private void checkForWinnerServer(){
		winnerPlayer = score.getWinner();
		if(score.getWinner() >= 0){
			winner = true;
		}
	}

	private class PongSimulation extends Thread {

		public void run() {
			
			while (running) {
				try {
					if (migration.playersReady) {
						running = false;
						doPlayerMigration(migration.targetServer, migration.targetMatch);
						return;
					}

					lastPlayer = Pong.doGameIteration(playing, bars, ball, score, lastPlayer);

					checkForWinnerServer();
					checkPlayersActivity();
					
					if(winner){
						Thread.sleep(600000 / Pong.UPDATE_RATE);
						resetGame();
					}
					else{
						Thread.sleep(1000 / Pong.UPDATE_RATE); // milliseconds
					}
				} catch (InterruptedException ex) {
					running = false;
				}
			}
		}
	}
	
	private synchronized void checkPlayersActivity() {
		for (int i=0; i < lastActivity.length; i++) {
			if (playing[i] && (System.currentTimeMillis() - lastActivity[i] > INACTIVITY_TIMEOUT)) {
				System.out.println("echando al jugador: " + i + "estado:"+ playing[i]);
				//removePlayer(i);
			}
		}
	}
	
	protected void removePlayer(int playerNum) {
		playing[playerNum] = false;
		players[playerNum] = null;
		
		// Se pone en 0 el puntaje del jugador que se fue
		int[] scores = score.getScores();
		scores[playerNum]  = 0;
		score.setScores(scores);
		
		System.out.println("Se ha desconectado el jugador " + playerNum + " de la partida " + matchID);
		if(!(this.playersReady())){
			System.out.println("Juego " + matchID + " pausado por falta de jugadores");
			simulationThread.interrupt();
			running = false;
		}
		if (Utils.countTrue(playing) == 0) {
			server.removeMatch(matchID);
		}
	}
	
	protected GameState updatePositions(int playerNum, int position) {
		if(playerNum == 0 || playerNum == 1){
			bars[playerNum].y = position;
		}
		else{
			bars[playerNum].x = position;
		}

		lastActivity[playerNum] = System.currentTimeMillis();
		return new GameState(playing, bars, ball, score.getScores(), winner, winnerPlayer, minPlayers);
	}
	
	private boolean playersReady() {
		return playersCount() >= minPlayers;
	}
	
	public int playersCount() {
		return Utils.countTrue(playing);
	}
	
	public int getID() {
		return matchID;
	}

	public boolean migrationReady() {
		return migration.migratingPlayers <= Utils.countTrue(playing);
	}

	public GameState startMigration() {
		migration.migrating = true;
		return new GameState(playing, bars, ball, score.getScores(), winner, winnerPlayer, minPlayers);
	}
	
	public void stopMigration(){
		migration.migrating = false;
		resetGameDueMigration();
	}
	
	public boolean migrating() {
		return migration.migrating;
	}

	public void migratePlayers(IServer targetServer, int targetMatch) throws RemoteException {
		migration.targetServer = targetServer;
		migration.targetMatch = targetMatch;
		migration.playersReady = true;
	}
	
	private void doPlayerMigration(IServer targetServer, int targetMatch) {
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
	
	private class MigrationInfo {
		public boolean migrating;
		public boolean playersReady;
		public int migratingPlayers;
		public IServer targetServer;
		public int targetMatch;
	}
}
