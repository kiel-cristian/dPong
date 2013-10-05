package cl.dcc.cc5303;

public class PongBall extends Rectangle {
	public double vx, vy;

	public PongBall(double x, double y, double w, double h) {
		super(x, y, w, h);
		vx = 0.4;
		vy = 0.3;
	}

}
