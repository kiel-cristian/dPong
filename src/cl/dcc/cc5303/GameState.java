package cl.dcc.cc5303;

import java.io.Serializable;

import cl.dcc.cc5303.PongBall;
import cl.dcc.cc5303.Rectangle;

public class GameState implements Serializable {
	private static final long serialVersionUID = -6238741276850716574L;
	public int bar1Pos;
	public int bar2Pos;
	public int ballX;
	public int ballY;
	public double vx;
	public double vy;
	
	public GameState(Rectangle bar1, Rectangle bar2, PongBall ball) {
		this.bar1Pos = (int) bar1.y;
		this.bar2Pos = (int) bar2.y;
		this.ballX = (int) ball.x;
		this.ballY = (int) ball.y;
		this.vx = ball.vx;
		this.vy = ball.vy;
	}
}
