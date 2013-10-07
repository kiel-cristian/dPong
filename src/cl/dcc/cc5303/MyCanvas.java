/**
 * @author Richard Ibarra Ram�rez (richard.ibarra@gmail.com)
 * 
 *  CC5303 - Primavera 2013
 *  C�tedra. Javier Bustos.
 *  DCC. Universidad de Chile
 */

package cl.dcc.cc5303;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class MyCanvas extends Canvas {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2915397754067560840L;
	private BufferedImage image;
	private Graphics2D g2d;

	public List<GameBar> rectangles = new ArrayList<GameBar>();
	public PongBall ball;
	public int playerNum;

	public MyCanvas(int playerNum, PongBall ball){
		super();
		this.playerNum = playerNum;
		this.ball      = ball;
	}
	
	public void init() {
		image = ((Graphics2D) getGraphics()).getDeviceConfiguration()
				.createCompatibleImage(getWidth(), getHeight());
		g2d = image.createGraphics();
	}
	
	public void update(Graphics g) {
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, getWidth(), getHeight());

		for (GameBar bar : rectangles) {
			if(bar.player == playerNum){
				g2d.setColor(Color.RED);
			}
			else{
				g2d.setColor(Color.WHITE);
			}
			bar.draw(g2d);
		}
		
		g2d.setColor(Color.WHITE);
		ball.draw(g2d);

		g.drawImage(image, 0, 0, this);
	}
}
