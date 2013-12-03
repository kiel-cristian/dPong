package cl.dcc.cc5303;

import java.io.Serializable;

public class GameBar extends Rectangle implements Serializable{
	private static final long serialVersionUID = 5652143987739416407L;
	public int player;

	public GameBar(double x, double y, double w, double h, int player) {
		super(x, y, w, h);
		this.player = player;
	}
	
	public void copy(Rectangle r){
		this.player = ((GameBar)r).player;
		super.copy(r);
	}
	
	public void reset(int x, int y, int w, int h, int player){
		super.reset(x, y, w, h);
		this.player = player;
	}

}
