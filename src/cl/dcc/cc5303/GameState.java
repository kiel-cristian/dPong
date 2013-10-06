package cl.dcc.cc5303;

import java.io.Serializable;

import cl.dcc.cc5303.PongBall;
import cl.dcc.cc5303.Rectangle;

public class GameState implements Serializable {
	private static final long serialVersionUID = -6238741276850716574L;
	public int[] barsPos = new int[4];
	public int currentPlayers;
	public boolean[] playing = new boolean[4];
	public int ballX;
	public int ballY;
	public double vx;
	public double vy;

	public GameState(boolean[] playing, Rectangle[] bars, PongBall ball) {
		this.currentPlayers = 0;
		boolean p;
		for( int i = 0; i < playing.length; i++){
			p = playing[i];
			if(p){
				this.playing[i] = true;
				this.currentPlayers++;
				if(i == 0 || i == 1)
					this.barsPos[i] = (int) bars[i].y;
				else
					this.barsPos[i] = (int) bars[i].x;
			}
			else{
				this.playing[i] = false;
			}
		}
		this.ballX = (int) ball.x;
		this.ballY = (int) ball.y;
		this.vx = ball.vx;
		this.vy = ball.vy;
	}
}