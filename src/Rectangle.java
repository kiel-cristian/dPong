/**
 * @author Richard Ibarra Ram’rez (richard.ibarra@gmail.com)
 * 
 *  CC5303 - Primavera 2013
 *  C‡tedra. Javier Bustos.
 *  DCC. Universidad de Chile
 */

package cl.dcc.cc5303;

import java.awt.Graphics;

public class Rectangle {

	double x, y;
	double w, h;

	public Rectangle(double x, double y, double w, double h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public void draw(Graphics graphics) {
		graphics.fillRect(left(), bottom(), (int) w, (int) h);
	}

	public int top() {
		return (int) (y + h * 0.5);
	}

	public int left() {
		return (int) (x - w * 0.5);
	}

	public int bottom() {
		return (int) (y - h * 0.5);
	}

	public int right() {
		return (int) (x + w * 0.5);
	}

}
