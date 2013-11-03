package cl.dcc.cc5303;

import java.io.Serializable;

import cl.dcc.cc5303.PongBall;
import cl.dcc.cc5303.Rectangle;

public class GameState implements Serializable {
	private static final long serialVersionUID = -6238741276850716574L;
	public boolean[] playing = new boolean[4];
	public int[] scores = new int[4];
	public int[] historicalScores = new int[4];
	public boolean winner = false;
	public int winnerPlayer;
	public int numPlayers;
	public int lastPlayer;
	public PongBall ball = new PongBall();
	public Rectangle[] bars = new Rectangle[4];

	public GameState(){
		resetGame();
	}
	
	public GameState(GameState state){
		fullUpdate(state);
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
	
	public void fullUpdate(GameState state){
		for( int i = 0; i < Pong.MAX_PLAYERS; i++){
			this.scores[i] = state.scores[i];
			this.playing[i] = state.playing[i];
			this.historicalScores[i] = state.historicalScores[i];
			this.bars[i].copy(state.bars[i]);
		}
		this.ball.copy(state.ball);
		this.winner = state.winner;
		this.winnerPlayer = state.winnerPlayer;
		this.numPlayers = state.numPlayers;
	}

	public void clientUpdate(GameState state) {
		if(state.winner){
			for(int i = 0; i < Pong.MAX_PLAYERS ; i++){
				this.bars[i].copy(state.bars[i]);
				this.playing[i] 	= state.playing[i];
			}
		}
		this.numPlayers = state.numPlayers;
	}
	
	public void resetGame(){
		bars[0] = new GameBar(10, Pong.HEIGHT / 2, 10, 100, 0); // jugadores
		bars[1] = new GameBar(Pong.WIDTH - 10, Pong.HEIGHT / 2, 10, 100, 1);
		bars[2] = new GameBar(Pong.WIDTH/2, Pong.HEIGHT - 10, 100, 10, 2);
		bars[3] = new GameBar(Pong.WIDTH/2, 10, 100, 10, 3);
		ball = new PongBall();
		lastPlayer = -1;
		winner     = false;
		for(int i = 0; i < Pong.MAX_PLAYERS; i++){
			playing[i] = false;
		}
		numPlayers = Pong.MAX_PLAYERS;
	}
	
	public void resetPlayerBars(){
		bars[0] = new GameBar(10, Pong.HEIGHT / 2, 10, 100, 0); // jugadores
		bars[1] = new GameBar(Pong.WIDTH - 10, Pong.HEIGHT / 2, 10, 100, 1);
		bars[2] = new GameBar(Pong.WIDTH/2, Pong.HEIGHT - 10, 100, 10, 2);
		bars[3] = new GameBar(Pong.WIDTH/2, 10, 100, 10, 3);
	}

	public void updatePlayerPosition(int playerNum, int position) {
		if(playerNum == 0 || playerNum == 1){
			bars[playerNum].y = position;
		}
		else{
			bars[playerNum].x = position;
		}
		
	}

	public void updateScores(int[] scores2) {
		for( int i = 0; i < Pong.MAX_PLAYERS; i++){
			scores[i] = scores2[i];
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
}