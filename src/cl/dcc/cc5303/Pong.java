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
	private JFrame scoreFrame;
	private MyCanvas canvas;
	private Client client;

	private Rectangle[] bars = new Rectangle[4];
	private PongBall ball;

	public static int lastPlayer;
	public static ScoreBoard score;
	
	private boolean[] keys;

	public Pong(Client client) {
		this.client = client;

		bars[0] = new Rectangle(10, HEIGHT / 2, 10, 100); // jugadores
		bars[1] = new Rectangle(WIDTH - 10, HEIGHT / 2, 10, 100);
		bars[2] = new Rectangle(WIDTH/2, HEIGHT - 10, 100, 10);
		bars[3] = new Rectangle(WIDTH/2, 10, 100, 10);

		ball = new PongBall();
		lastPlayer = 0;
		
		
		keys = new boolean[KeyEvent.KEY_LAST];
		init();
	}

	/* Initializes window frame and set it visible */
	private void init() {

		score = new ScoreBoard();
		canvas = new MyCanvas(client.getPlayerNum());

		scoreFrame = new JFrame("Marcador");
		scoreFrame.setSize(WIDTH, HEIGHT/2);
		scoreFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame = new JFrame(TITLE);
		frame.setSize(WIDTH, HEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.add(canvas);
		scoreFrame.add(score);

		canvas.setSize(WIDTH, HEIGHT);
		score.setSize(WIDTH, HEIGHT/2);

		for(int i = 0; i < bars.length; i++){
			if(client.getPlayerStatus(i))
				canvas.rectangles.add(bars[i]);
		}
		
		canvas.rectangles.add(ball);
		canvas.addKeyListener(this);

		frame.pack();
		frame.setVisible(true);

		scoreFrame.pack();
		scoreFrame.setVisible(true);

		canvas.init();
//		score.init();
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
			switch(lastPlayer){
				case(1):{
					// Punto para jugador 1 si no sale por la izquierda
					if(!(ball.x < 0)){
						score.pointP1();
						// System.out.println("jugardor 1: " + score.getScoreP1());
					}
				}
				case(2):{
					// Punto para jugador 2 si no sale por la derecha
					if( !(ball.x > Pong.WIDTH)){
						score.pointP2();
						// System.out.println("jugardor 2: " + score.getScoreP2());
					}
				}
				case(3):{
					// Punto para jugador 3 si no sale por la derecha
					if( !(ball.y > Pong.HEIGHT)){
						score.pointP3();
						// System.out.println("jugardor 3: " + score.getScoreP3());
					}
				}
				case(4):{
					// Punto para jugador 4 si no sale por la derecha
					if( !(ball.y < 0)){
						score.pointP4();
						// System.out.println("jugardor 4: " + score.getScoreP4());
					}
				}
			}
			
			lastPlayer = 0;
			ball.reset();
		}
	}

	private static void handleHumanBounce(int i, Rectangle bar, PongBall ball){
		double step = 1+DV;
    switch(i){
     // jugador a la izquierda
     case(0):{
       if(ball.willCrashWithBarByRight(bar, step)){
         ball.changeXDir(step);
         lastPlayer = 1;
       }
     }
     // jugador a la derecha
     case(1):{ 
       if(ball.willCrashWithBarByLeft(bar, step)){
         ball.changeXDir(step);
         lastPlayer = 2;
       }
     }
     //jugador inferior
     case(2):{
       if(ball.willCrashWithBarByTop(bar, step)){
         ball.changeYDir(step);
         lastPlayer = 3;
       }
     }
     // jugador superior
     case(3):{
       if(ball.willCrashWithBarByBottom(bar, step)){
         ball.changeYDir(step);
         lastPlayer = 4;
       }
     }
    }
	}

	private static void handleBounce(int i, PongBall ball){
		switch(i){
			// izquierda
			case(0):{
				if(ball.checkIfGoesLeft(0, DX))
					ball.changeXDir(1);
			}
			// derecha
			case(1):{
				if(ball.checkIfGoesRight(WIDTH, DX)){
					ball.changeXDir(1);
				}
			}
			// abajo
			case(2):{
				if(ball.checkIfGoesDown(HEIGHT, DX)){
					ball.changeYDir(1);
				}

			}
			// arriba
			case(3):{
				if(ball.checkIfGoesUp(0, DX))
					ball.changeYDir(1);
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
