package cl.dcc.cc5303;

import java.io.Serializable;

public class GameStateInfo implements Serializable{
	private static final long serialVersionUID = -3300818399361838878L;
	public volatile boolean[] playing;
	public volatile int[] iBars;
	public volatile int winnerPlayer;
	public volatile int lastPlayer;
	public volatile boolean winner;
	public volatile boolean running;
	public volatile int ballX;
	public volatile int ballY;
	public volatile int[] scores;
	public volatile int[] historicalScores;
	public volatile int numPlayers;
	public volatile boolean migrating;

	public GameStateInfo(boolean[] playing, int[] iBars, int winnerPlayer,
			int lastPlayer, boolean winner, boolean running, int ballX,
			int ballY, int[] scores, int[] historicalScores, int numPlayers, boolean migrating) {
		this.playing = playing;
		this.iBars = iBars;
		this.winnerPlayer = winnerPlayer;
		this.lastPlayer = lastPlayer;
		this.winner = winner;
		this.running = running;
		this.ballX = ballX;
		this.ballY = ballY;
		this.scores = scores;
		this.historicalScores = historicalScores;
		this.numPlayers = numPlayers;
		this.migrating  = migrating;
	}

}
