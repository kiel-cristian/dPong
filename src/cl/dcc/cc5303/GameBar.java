package cl.dcc.cc5303;

public class GameBar extends Rectangle{
	public int player;

	public GameBar(double x, double y, double w, double h, int player) {
		super(x, y, w, h);
		this.player = player;
	}

}
