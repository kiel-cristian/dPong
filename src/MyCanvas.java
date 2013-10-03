/**
 * @author Richard Ibarra Ram’rez (richard.ibarra@gmail.com)
 * 
 *  CC5303 - Primavera 2013
 *  C‡tedra. Javier Bustos.
 *  DCC. Universidad de Chile
 */

package cl.dcc.cc5303;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.util.List;
import java.util.ArrayList;

public class MyCanvas extends Canvas {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2915397754067560840L;

	public List<Rectangle> rectangles = new ArrayList<Rectangle>();

	public void paint(Graphics graphics) {

		graphics.setColor(Color.BLACK);
		graphics.fillRect(0, 0, getWidth(), getHeight());

		graphics.setColor(Color.WHITE);
		for (Rectangle rectangle : rectangles) {
			rectangle.draw(graphics);
		}

	}
}
