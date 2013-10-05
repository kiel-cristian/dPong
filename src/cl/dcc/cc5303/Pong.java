/**
 * @author Richard Ibarra Ram�rez (richard.ibarra@gmail.com)
 * 
 *  CC5303 - Primavera 2013
 *  C�tedra. Javier Bustos.
 *  DCC. Universidad de Chile
 */

package cl.dcc.cc5303;


import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;


public class Pong implements KeyListener {

	public final static String TITLE = "Pong - CC5303";
	public final static int WIDTH = 640, HEIGHT = 480;
	public final static int UPDATE_RATE = 60;
	public final static int DX = 5;
	public final static double DV = 0.1;

	private JFrame frame;
	private MyCanvas canvas;
	private Client client;

	private Rectangle bars[];
	private PongBall ball;

	private boolean[] keys;

	public Pong(Client client) {
		this.client = client;
		bars = new Rectangle[2];
		bars[0] = new Rectangle(10, HEIGHT / 2, 10, 100);
		bars[1] = new Rectangle(WIDTH - 10, HEIGHT / 2, 10, 100);
		ball = new PongBall(WIDTH * 0.5, HEIGHT * 0.5, 10, 10);
		keys = new boolean[KeyEvent.KEY_LAST];
		init();
	}

	/* Initializes window frame and set it visible */
	private void init() {

		frame = new JFrame(TITLE);
		// frame.setSize(WIDTH, HEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvas = new MyCanvas();
		frame.add(canvas);

		canvas.setSize(WIDTH, HEIGHT);
		canvas.rectangles.add(bars[0]);
		canvas.rectangles.add(bars[1]);
		canvas.rectangles.add(ball);
		canvas.addKeyListener(this);

		frame.pack();
		frame.setVisible(true);
		canvas.init();
		frame.addKeyListener(this);

		Thread game = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					handleKeyEvents(client.getPlayerNum());
					bars[0].y = client.getBarPosition(0);
					bars[1].y = client.getBarPosition(1);
					ball.x = client.getBallX();
					ball.y = client.getBallY();
					ball.vx = client.getVelX();
					ball.vy = client.getVelY();
					doGameIteration(bars, ball);
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
	
	public static void doGameIteration(Rectangle[] bars, PongBall ball) {
		// actualiza posicion
		ball.x += ball.vx * DX;
		ball.y += ball.vy * DX;

		// rebote en y
		if (ball.y + ball.h * 0.5 >= HEIGHT
				|| ball.y - ball.h * 0.5 <= 0) {
			ball.vy = -ball.vy;
		}

		// rebote con paletas
		for (int i = 0; i < bars.length; i++) {
			Rectangle bar = bars[i];
			if (ball.bottom() < bar.top()
					&& ball.top() > bar.bottom()) { // esta dentro
													// en
													// Y
				if ((ball.vx > 0 && ball.left() <= bar.left() && ball
						.right() >= bar.left()) // esta a la
												// izquierda y se
												// mueve a la
												// derecha
						// o esta a la derecha y se mueve hacia la
						// izquierda
						|| (ball.vx < 0 && ball.right() >= bar.right() && ball
								.left() <= bar.right())) {

					ball.vx = -ball.vx * (1 + DV);
					break;
				}
			}
		}
	}
	
	private void handleKeyEvents(int playerPos) {
		Rectangle bar = bars[0];
		if (playerPos == 1) {
			bar = bars[1];
		}
		if (keys[KeyEvent.VK_UP] || keys[KeyEvent.VK_W]) {
			if (bar.y - bar.h * 0.5 - DX >= 0)
				bar.y -= DX;
		}
		if (keys[KeyEvent.VK_DOWN] || keys[KeyEvent.VK_S]) {
			if (bar.y + bar.h * 0.5 + DX < HEIGHT)
				bar.y += DX;
		}
		client.setBarPosition(playerPos, (int) bar.y);
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
