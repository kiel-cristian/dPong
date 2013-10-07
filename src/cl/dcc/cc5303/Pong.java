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
import java.rmi.RemoteException;

import javax.swing.JFrame;


public class Pong implements KeyListener {

	public final static String TITLE = "Pong - CC5303";
	public final static int WIDTH = 640, HEIGHT = 480;
	public final static int UPDATE_RATE = 60;
	public final static int DX = 5;
	public final static double DV = 0.1;
	public final static int MAX_PLAYERS = 4;

	private JFrame frame;
	private MyCanvas canvas;
	private Client client;

	private Rectangle[] bars = new Rectangle[4];
	private PongBall ball;

	private ScoreBoard sb;
	
	private boolean[] keys;

	public Pong(Client client) {
		this.client = client;

		bars[0] = new Rectangle(10, HEIGHT / 2, 10, 100); // jugadores
		bars[1] = new Rectangle(WIDTH - 10, HEIGHT / 2, 10, 100);
		bars[2] = new Rectangle(WIDTH/2, HEIGHT - 10, 100, 10);
		bars[3] = new Rectangle(WIDTH/2, 10, 100, 10);

		ball = new PongBall(WIDTH * 0.5, HEIGHT * 0.5, 10, 10);
		
		sb = new ScoreBoard();
		
		keys = new boolean[KeyEvent.KEY_LAST];
		init();
	}

	/* Initializes window frame and set it visible */
	private void init() {

		frame = new JFrame(TITLE);
		// frame.setSize(WIDTH, HEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvas = new MyCanvas(client.getPlayerNum());
		frame.add(canvas);

		canvas.setSize(WIDTH, HEIGHT);

		for(int i = 0; i < bars.length; i++){
			if(client.getPlayerStatus(i))
				canvas.rectangles.add(bars[i]);
		}
		
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
					int playerNum     = client.getPlayerNum();
					boolean[] playing = client.getPlaying();

					try {
						if(client.playersReady()){
							handleKeyEvents(playerNum);
							doGameIteration(playing, bars, ball);
						}
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}

					handlePlayerBars();
					handleStatus(playerNum);
					
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

	private void handleStatus(int playerNum){
		for(int i = 0; i < MAX_PLAYERS ; i++){
			if(i == playerNum)
				continue;
			if(client.getPlayerStatus(i)){
				if(i == 0 || i == 1){
					bars[i].y = client.getBarPosition(i);
				}
				else{
					bars[i].x = client.getBarPosition(i);
				}
			}
		}
		ball.x = client.getBallX();
		ball.y = client.getBallY();
		ball.vx = client.getVelX();
		ball.vy = client.getVelY();
	}

	public static void doGameIteration(boolean[] playing, Rectangle[] bars, PongBall ball) {

		for (int i = 0; i < Pong.MAX_PLAYERS;  i++){
			if(playing[i] == true){
				handleHumanBounce(i, bars[i], ball);
			}
			else{
				handleBounce(i, ball);
			}
		}

		handleBall(ball);
	}

	private static void handleBall(PongBall ball){
		// actualiza posicion
		ball.x += ball.vx * DX;
		ball.y += ball.vy * DX;

		if(ball.x > Pong.WIDTH || ball.x < 0 || ball.y < 0 || ball.y > Pong.HEIGHT){
			// TODO : MANEJAR SCORE
			ball.reset();
		}
	}

	private static void handleHumanBounce(int i, Rectangle bar, PongBall ball){
		// rebote con paletas (verticales, jugadores 0 y 1)
		if(i == 0 || i == 1){
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
				}
			}
		}
		// rebote con paletas (horizontales, jugadores 2 y 3)
		else{
			if(ball.left() < bar.left() && ball.right() < bar.right()){
				if((ball.vy > 0 && ball.top() <= bar.bottom()) // se mueve hacia arriba y esta abajo de la barra
					||
					((ball.vy < 0 && ball.bottom() >= bar.top()))){ // se mueve hacia abajo y esta arriba de la barra

					ball.vy = -ball.vy * (1 + DV);
				}
			}
		}
	}

	private static void handleBounce(int i, PongBall ball){
		switch(i){
			// izquierda
			case(0):{
				if(ball.checkIfGoesLeft(0, DX))
					ball.vx = -ball.vx;
			}
			// derecha
			case(1):{
				if(ball.checkIfGoesRight(WIDTH, DX)){
					ball.vx = -ball.vx;
				}
			}
			// abajo
			case(2):{
				if(ball.checkIfGoesDown(HEIGHT, DX)){
					ball.vy = -ball.vy;
				}

			}
			// arriba
			case(3):{
				if(ball.checkIfGoesUp(0, DX))
					ball.vy = -ball.vy;
			}
		}
	}
	
	private void handlePlayerBars(){
		for(int i = 0; i < MAX_PLAYERS; i++){
			canvas.rectangles.remove(bars[i]);

			if(this.client.getPlayerStatus(i)){
				canvas.rectangles.add(bars[i]);
			}
		}
	}
	
	private void handleKeyEvents(int playerPos) {
		Rectangle bar = bars[playerPos];
		// Jugador posee una barra vertical
		if(playerPos == 0 || playerPos == 1){
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
		// Jugador posee una barra horizontal
		else{
			if (keys[KeyEvent.VK_LEFT] || keys[KeyEvent.VK_A]) {
				if (bar.x - bar.w * 0.5 - DX >= 0)
					bar.x -= DX;
			}
			if (keys[KeyEvent.VK_RIGHT] || keys[KeyEvent.VK_D]) {
				if (bar.x + bar.w * 0.5 + DX < WIDTH)
					bar.x += DX;
			}

			client.setBarPosition(playerPos, (int) bar.x);
		}
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
