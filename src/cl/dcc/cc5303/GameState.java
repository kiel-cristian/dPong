package cl.dcc.cc5303;

import java.io.Serializable;
import cl.dcc.cc5303.PongBall;

public class GameState implements Serializable {
	private static final long serialVersionUID = -6238741276850716574L;
	public boolean[] playing = new boolean[4];
	public int[] scores = new int[4];
	public int[] historicalScores = new int[4];
	public boolean winner = false;
	public int winnerPlayer;
	public int numPlayers;
	public int lastPlayer;
	public boolean running;
	public PongBall ball = new PongBall();
	public GameBar[] bars = new GameBar[4];

	public GameState(){
		initGame();
		if(!playersReady()){
			pause();
		}
	}
	
	public GameState(GameState state){
		fullUpdate(state);
		if(!playersReady()){
			pause();
		}
	}
	
	public void pause(){
		this.running = false;
	}
	public void unPause(){
		this.running = true;
		ball = new PongBall();
	}
	
	public boolean isPlaying(int playerNum){
		return this.playing[playerNum];
	}
	
	public void enablePlayer(int playerNum){
		this.playing[playerNum] = true;
	}
	
	public void disablePlayer(int playerNum){
		this.playing[playerNum] = false;
	}
	
	public void setMatchWinner(int winnerPlayer, int[] historicalScores2) {
		this.winnerPlayer = winnerPlayer;
		this.winner = true;
		for(int i = 0; i < PongClient.MAX_PLAYERS; i++){
			this.historicalScores[i] = historicalScores2[i];
		}
	}
	
	public void serverUpdate(int[] scores, int[] historical, int playerNum, int pos){
		updatePlayerPosition(playerNum, pos);
		for( int i = 0; i < PongClient.MAX_PLAYERS; i++){
			this.scores[i] = scores[i];
			this.historicalScores[i] = historical[i];
		}
	}
	
	public void updateServerScores(int[] scores2, int[] historical){
		for( int i = 0; i < PongClient.MAX_PLAYERS; i++){
			this.scores[i] = scores2[i];
			this.historicalScores[i] = historical[i];
		}
	}
	
	public void fullUpdate(GameState state){
		for( int i = 0; i < PongClient.MAX_PLAYERS; i++){
			this.scores[i] = state.scores[i];
			this.playing[i] = state.playing[i];
			this.historicalScores[i] = state.historicalScores[i];
			this.bars[i].copy(state.bars[i]);
		}
		this.ball.copy(state.ball);
		this.winner = state.winner;
		this.winnerPlayer = state.winnerPlayer;
		this.numPlayers = state.numPlayers;
		this.running = state.running;
	}
	
	private void initGame(){
		bars[0] = new GameBar(10, PongClient.HEIGHT / 2, 10, 100, 0); // jugadores
		bars[1] = new GameBar(PongClient.WIDTH - 10, PongClient.HEIGHT / 2, 10, 100, 1);
		bars[2] = new GameBar(PongClient.WIDTH/2, PongClient.HEIGHT - 10, 100, 10, 2);
		bars[3] = new GameBar(PongClient.WIDTH/2, 10, 100, 10, 3);
		ball = new PongBall();
		lastPlayer = -1;
		winner     = false;
		for(int i = 0; i < PongClient.MAX_PLAYERS; i++){
			playing[i] = false;
		}
		numPlayers = PongClient.MAX_PLAYERS;
		running = true;
	}
	
	public void resetGame(){
		resetPlayerBars();
		ball.reset();
		lastPlayer = -1;
		winner     = false;
		running    = true;
	}
	
	public void resetPlayerBars(){
		bars[0].reset(10, PongClient.HEIGHT / 2, 10, 100, 0); // jugadores
		bars[1].reset(PongClient.WIDTH - 10, PongClient.HEIGHT / 2, 10, 100, 1);
		bars[2].reset(PongClient.WIDTH/2, PongClient.HEIGHT - 10, 100, 10, 2);
		bars[3].reset(PongClient.WIDTH/2, 10, 100, 10, 3);
	}

	private void updatePlayerPosition(int playerNum, int position) {
		if(playerNum == 0 || playerNum == 1){
			bars[playerNum].y = position;
		}
		else{
			bars[playerNum].x = position;
		}
	}
	
	public boolean playersReady() {
		return Utils.countTrue(playing) >= numPlayers;
	}
	
	public int playersCount(){
		return Utils.countTrue(playing);
	}
	
	public int getPlayerPosition(int playerNum) {
		if(playerNum == 0 || playerNum == 1){
			return (int) bars[playerNum].y;
		}
		else{
			return (int) bars[playerNum].x;
		}
	}

	public void setLastPlayer(int nextPlayer) {
		this.lastPlayer = nextPlayer;
		
	}

}