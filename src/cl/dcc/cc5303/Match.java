package cl.dcc.cc5303;


public class Match {
	public static final int MAX_PLAYERS = 4;
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
		this.matchID = matchID;
		this.minPlayers = minPlayers;
	}
	
	private void startGame() {
		simulationThread = new PongSimulation();
		lastPlayer = -1;
		simulationThread.start();
		System.out.println("Empieza el juego!");
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
		System.out.println("Jugador solicita conectarse. Se le asigna player " + (num + 1));
		if (playersReady())
			startGame();
		server.increasePlayerNum();
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
			boolean running = true;
			
			while (running) {
				try {
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
	
	private void checkPlayersActivity() {
		for (int i=0; i < lastActivity.length; i++) {
			if (playing[i] && (System.currentTimeMillis() - lastActivity[i] > INACTIVITY_TIMEOUT)) {
				removePlayer(i);
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
		
		if(!(this.playersReady())){
			System.out.println("Juego pausado por falta de jugadores");
			simulationThread.interrupt();
		}
		server.decreasePlayerNum();
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
}
