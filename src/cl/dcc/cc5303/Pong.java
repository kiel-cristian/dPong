/**
 * @author Richard Ibarra Ram�rez (richard.ibarra@gmail.com)
 * 
 *  CC5303 - Primavera 2013
 *  C�tedra. Javier Bustos.
 *  DCC. Universidad de Chile
 */

package cl.dcc.cc5303;


import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.rmi.RemoteException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class Pong implements KeyListener {

	public final static String TITLE = "Pong - CC5303";
	public final static int WIDTH = 640, HEIGHT = 480;
	public final static int UPDATE_RATE = 60;
	public final static int DX = 5;
	public final static double DV = 0.1;
	public final static int MAX_PLAYERS = 4;

	private JFrame frame;
	private JFrame scoreFrame;
	private ScoreBoard score;
	private MyCanvas canvas;
	private Client client;

	private GameBar[] bars = new GameBar[4];
	private int[] scores = new int[4];
	private int lastPlayer;
	private PongBall ball;
	private boolean[] keys;
	private static boolean[] playing = new boolean[4];

	public Pong(Client client) {
		this.client = client;
		
		int playerNum = client.getPlayerNum();

		bars[0] = new GameBar(10, HEIGHT / 2, 10, 100, 0); // jugadores
		bars[1] = new GameBar(WIDTH - 10, HEIGHT / 2, 10, 100, 1);
		bars[2] = new GameBar(WIDTH/2, HEIGHT - 10, 100, 10, 2);
		bars[3] = new GameBar(WIDTH/2, 10, 100, 10, 3);

		ball = new PongBall();
		lastPlayer = -1;
		
		for(int i = 0; i < MAX_PLAYERS; i++){
			scores[i] = 0;
			if(playerNum == i)
				playing[i] = true;
			else
				playing[i] = false;
		}
		keys = new boolean[KeyEvent.KEY_LAST];
		init();
	}

	/* Initializes window frame and set it visible */
	private void init() {
		canvas = new MyCanvas(client.getPlayerNum(), client.getBall());
		score  = new ScoreBoard();

		scoreFrame = new JFrame("Marcador");
		scoreFrame.setSize(WIDTH, HEIGHT/2);
		scoreFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame = new JFrame(TITLE);
		frame.setSize(WIDTH, HEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.add(canvas);
		scoreFrame.add(score);

		canvas.setSize(WIDTH, HEIGHT);
		scoreFrame.setSize(WIDTH, HEIGHT/2);

		for(int i = 0; i < bars.length; i++){
			if(client.getPlayerStatus(i))
				canvas.rectangles.add(bars[i]);
		}
		
		canvas.addKeyListener(this);

		frame.pack();
		frame.setVisible(true);

		scoreFrame.pack();
		scoreFrame.setVisible(true);

		canvas.init();
		frame.addKeyListener(this);
		handleScore();

		Thread game = new Thread(new Runnable() {

			@Override
			public void run() {
				while (client.isRunning()) {
					int playerNum     = client.getPlayerNum();
					playing 		  = client.getPlaying();
					scores            = client.getScores();
					lastPlayer        = client.getLastPLayer();

					try {
						if(client.playersReady()){
							handleStatus(playerNum);
							handleKeyEvents(playerNum);
							doGameIteration(playing, bars, ball, scores, lastPlayer);
						}
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}

					handlePlayerBars();
					handleScore();
					
					canvas.playerNum = playerNum;
					canvas.ball = ball;
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
	
	private void handleScore(){
		String text = "";
		for(int i = 0; i < Pong.MAX_PLAYERS; i++){
			if(playing[i]){
				text = text + "P" + i + ": " + scores[i] + " ";
			}
		}
		System.out.println(text);
//		score.setText(scores, playing);
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

	public static void doGameIteration(boolean[] playing, Rectangle[] bars, PongBall ball, int[] scores, int lastPlayer) {

		for (int i = 0; i < Pong.MAX_PLAYERS;  i++){
			if(playing[i] == true){
				handleHumanBounce(i, bars[i], ball, lastPlayer);
			}
			else{
				handleBounce(i, ball);
			}
		}

		handleBall(ball, scores, lastPlayer);
	}

	private static void handleBall(PongBall ball, int[] scores, int lastPlayer){
		// actualiza posicion
		ball.x += ball.vx * DX;
		ball.y += ball.vy * DX;

		if(ball.x > Pong.WIDTH || ball.x < 0 || ball.y < 0 || ball.y > Pong.HEIGHT){
			switch(lastPlayer){
				case(0):{
					// Punto para jugador 1 si no sale por la izquierda
					if(!(ball.x < 0)){
						scores[0]++;
						// System.out.println("jugardor 1: " + score.getScoreP1());
					}
				}
				case(1):{
					// Punto para jugador 2 si no sale por la derecha
					if( !(ball.x > Pong.WIDTH)){
						scores[1]++;
						// System.out.println("jugardor 2: " + score.getScoreP2());
					}
				}
				case(2):{
					// Punto para jugador 3 si no sale por la derecha
					if( !(ball.y > Pong.HEIGHT)){
						scores[2]++;
						// System.out.println("jugardor 3: " + score.getScoreP3());
					}
				}
				case(3):{
					// Punto para jugador 4 si no sale por la derecha
					if( !(ball.y < 0)){
						scores[3]++;
						// System.out.println("jugardor 4: " + score.getScoreP4());
					}
				}
			}
			
//			updateScore();
			lastPlayer = -1;
			ball.reset();
		}
	}

	private static void handleHumanBounce(int i, Rectangle bar, PongBall ball, int lastPlayer){
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
		// Manejo de Q
		if(keys[KeyEvent.VK_Q] || keys[KeyEvent.VK_ESCAPE]){
			try {
				client.stop();
				frame.removeAll();
				scoreFrame.removeAll();
				
				
				
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}

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
