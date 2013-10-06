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

	public List<Rectangle> rectangles = new ArrayList<Rectangle>();
	public int playerNum;

	public MyCanvas(int _playerNum){
		super();
		playerNum = _playerNum + 1;
	}
	
	public void init() {
		image = ((Graphics2D) getGraphics()).getDeviceConfiguration()
				.createCompatibleImage(getWidth(), getHeight());
		g2d = image.createGraphics();
	}
	
	public void update(Graphics g) {
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, getWidth(), getHeight());

		int p = 0;
		for (Rectangle rectangle : rectangles) {
			if(p++ == playerNum){
				g2d.setColor(Color.RED);
			}
			else{
				g2d.setColor(Color.WHITE);
			}
			rectangle.draw(g2d);
		}

		g.drawImage(image, 0, 0, this);
	}
}
