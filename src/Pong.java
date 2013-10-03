/**
 * @author Richard Ibarra Ram’rez (richard.ibarra@gmail.com)
 * 
 *  CC5303 - Primavera 2013
 *  C‡tedra. Javier Bustos.
 *  DCC. Universidad de Chile
 */

package cl.dcc.cc5303;

import java.util.List;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;

import javax.swing.JFrame;

public class Pong implements KeyListener {

	public final static String TITLE = "Pong - CC5303";
	public final static int WIDTH = 640, HEIGHT = 480;
	public final static int UPDATE_RATE = 60;
	public final static int DX = 5;
	public final static double DV = 0.1;

	public JFrame frame;
	public MyCanvas canvas;

	public Rectangle bar1, bar2;
	public Rectangle ball;

	private double vx = 0.4, vy = 0.3;

	public boolean[] keys;

	public Pong() {

		bar1 = new Rectangle(10, HEIGHT / 2, 10, 100);
		bar2 = new Rectangle(WIDTH - 10, HEIGHT / 2, 10, 100);
		ball = new Rectangle(WIDTH * 0.5, HEIGHT * 0.5, 10, 10);

		keys = new boolean[KeyEvent.KEY_LAST];

		init();

	}

	/* Initializes window frame and set it visible */
	public void init() {

		frame = new JFrame(TITLE);
		// frame.setSize(WIDTH, HEIGHT);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		canvas = new MyCanvas();
		frame.add(canvas);

		canvas.setSize(WIDTH, HEIGHT);
		canvas.rectangles.add(bar1);
		canvas.rectangles.add(bar2);
		canvas.rectangles.add(ball);

		frame.pack();

		frame.addKeyListener(this);

		Thread game = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					if (keys[KeyEvent.VK_UP]) {
						if (bar1.y - bar1.h * 0.5 - DX >= 0)
							bar1.y -= DX;
					}
					if (keys[KeyEvent.VK_DOWN]) {
						if (bar1.y + bar1.h * 0.5 + DX < HEIGHT)
							bar1.y += DX;
					}
					if (keys[KeyEvent.VK_W]) {
						if (bar2.y - bar2.h * 0.5 - DX >= 0)
							bar2.y -= DX;
					}
					if (keys[KeyEvent.VK_S]) {
						if (bar2.y + bar2.h * 0.5 + DX < HEIGHT)
							bar2.y += DX;
					}

					// actualiza posicion
					ball.x += vx * DX;
					ball.y += vy * DX;

					// rebote en y
					if (ball.y + ball.h * 0.5 >= HEIGHT
							|| ball.y - ball.h * 0.5 <= 0) {
						vy = -vy;
					}

					// rebote con paletas
					List<Rectangle> bars = Arrays.asList(bar1, bar2);

					for (int i = 0; i < bars.size(); i++) {
						Rectangle bar = bars.get(i);
						if (ball.bottom() < bar.top()
								&& ball.top() > bar.bottom()) { // esta dentro
																// en
																// Y
							if ((vx > 0 && ball.left() <= bar.left() && ball
									.right() >= bar.left()) // esta a la
															// izquierda y se
															// mueve a la
															// derecha
									// o esta a la derecha y se mueve hacia la
									// izquierda
									|| (vx < 0 && ball.right() >= bar.right() && ball
											.left() <= bar.right())) {

								vx = -vx * (1 + DV);
								break;
							}
						}
					}

					/*
					 * for (Rectangle bar : bars) { if (ball.x + ball.w * 0.5 >
					 * bar.x - bar.w * 0.5 && ball.x - ball.w * 0.5 > bar.x +
					 * bar.w * 0.5) { if ((vy > 0 && ball.y + ball.h * 0.5 >=
					 * bar.y - bar.h * 0.5) || (vy < 0 && ball.y - ball.h * 0.5
					 * <= bar.y + bar.h * 0.5)) { vy = -vy; break; } } }
					 */

					canvas.repaint();

					try {
						Thread.sleep(1000 / UPDATE_RATE); // milliseconds
					} catch (InterruptedException ex) {
					}
				}
			}
		});
		game.start();

	}

	@Override
	public void keyPressed(KeyEvent event) {
		keys[event.getKeyCode()] = true;

	}

	@Override
	public void keyReleased(KeyEvent event) {
		keys[event.getKeyCode()] = false;

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}
}
