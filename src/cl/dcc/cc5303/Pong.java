/**
 * @author Richard Ibarra Ram�rez (richard.ibarra@gmail.com)
 * 
 *  CC5303 - Primavera 2013
 *  C�tedra. Javier Bustos.
 *  DCC. Universidad de Chile
 */

package cl.dcc.cc5303;


import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;


public class Pong implements KeyListener {

	public final static String TITLE = "Pong - CC5303";
	public final static int WIDTH = 640, HEIGHT = 480;
	public final static int UPDATE_RATE = 60;
	public final static int DX = 5;
	public final static double DV = 0.3;
	public final static int MAX_PLAYERS = 4;
	public final static int WINNING_SCORE = 2;

	private JFrame frame;
	private MyCanvas canvas;
	private Client client;

	private GameBar[] bars = new GameBar[4];
	private int lastPlayer;
	private PongBall ball;

	public ScoreBoardGUI scores;
	public HistoricalScoreBoardGui historical;

	private boolean[] keys;
	private boolean[] playing = new boolean[4];
	private int playerNum;
	
	public Thread serverUpdate;
	public Thread game;

	public Pong(Client client, Thread serverUpdate) {
		this.client    = client;
		this.playerNum = client.getPlayerNum();
		this.serverUpdate = serverUpdate;

		bars[0] = new GameBar(10, HEIGHT / 2, 10, 100, 0); // jugadores
		bars[1] = new GameBar(WIDTH - 10, HEIGHT / 2, 10, 100, 1);
		bars[2] = new GameBar(WIDTH/2, HEIGHT - 10, 100, 10, 2);
		bars[3] = new GameBar(WIDTH/2, 10, 100, 10, 3);

		ball = new PongBall();
		lastPlayer = -1;
		
		for(int i = 0; i < MAX_PLAYERS; i++){
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
		canvas = new MyCanvas(this.playerNum, client.getBall());
		scores = new ScoreBoardGUI(this.client.getPlaying(), this.playerNum);
		historical = new HistoricalScoreBoardGui(this.playerNum);
		
		frame = new JFrame(TITLE);
		frame.setLayout(new BorderLayout());
		frame.setSize(WIDTH, HEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(canvas, BorderLayout.CENTER);
		frame.add(scores, BorderLayout.NORTH);
		frame.add(historical, BorderLayout.SOUTH);
		
		canvas.setSize(WIDTH, HEIGHT);
		for(int i = 0; i < bars.length; i++){
			if(client.getPlayerStatus(i))
				canvas.rectangles.add(bars[i]);
		}
		canvas.addKeyListener(this);
		
		frame.pack();
		frame.setVisible(true);
		canvas.init();
		frame.addKeyListener(this);
		
		historical.showScores();
		game = new PongGame(this);
	}
	
	public void startGame() {
		serverUpdate.start();
		game.start();
	}
	
	private class PongGame extends Thread{
		Pong pong;
		public PongGame(Pong game){
			this.pong = game;
		}

		public void run() {
			boolean running = true;
			boolean winner = false;
			boolean ready = false;
			boolean onGame = false;
			
			while (running) {
				try {
					winner = pong.client.getWinner();
					ready  = pong.client.playersReady();
					
					if(!onGame && ready){
						onGame = true;
						pong.showPauseMessage("");
					}
					if(ready && !winner){
						int playerNum     = pong.client.getPlayerNum();
						pong.playing 	  = pong.client.getPlaying();
						pong.lastPlayer   = pong.client.getLastPLayer();

						pong.handleStatus(playerNum);
						pong.handleKeyEvents(playerNum);
						Pong.doGameIteration(pong.playing, pong.bars, pong.ball, pong.scores, pong.lastPlayer);
						pong.handlePlayerBars();
						pong.canvas.playerNum = playerNum;
						pong.canvas.ball = pong.ball;
					}
					else if(!ready && !winner){
						onGame = false;
						pong.showPauseMessage("Esperando jugadores ...");
					}

					pong.canvas.repaint();
					handleQuitEvent();
					Thread.sleep(1000 / UPDATE_RATE); // milliseconds
					 
				}
				catch (InterruptedException ex) {
					System.out.println("Pong: " + pong.playerNum + " muriendo");
					running = false;
					return;
				}
			}
		}
	}
	
	public void stopClient() {
		frame.dispose();
		serverUpdate.interrupt();
		game.interrupt();
		
		System.exit(0);// FIXME
	}
	
	private void stop(){
		client.stop(this.playerNum); // Notificacion al server
		stopClient();
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

	public static synchronized int doGameIteration(boolean[] playing, Rectangle[] bars, PongBall ball, ScoreBoard score, int lastPlayer) {

		for (int i = 0; i < 4;  i++){
			if(playing[i] == true){
				lastPlayer = handleHumanBounce(i, bars[i], ball, lastPlayer);
			}
			else{
				handleBounce(i, ball);
			}
		}

		lastPlayer = handleBall(ball, score, lastPlayer, playing);
		return lastPlayer;
	}

	private static int handleBall(PongBall ball, ScoreBoard score, int lastPlayer, boolean[] playing) {
		// actualiza posicion
		ball.x += ball.vx * DX;
		ball.y += ball.vy * DX;

		if(ball.x > Pong.WIDTH || ball.x < 0 || ball.y < 0 || ball.y > Pong.HEIGHT){
			switch(lastPlayer){
				case(0):{
					// Punto para jugador 1 si no sale por la izquierda
					if(!(ball.x < 0)){
						score.sumPoint(0, playing);
					}
				} break;
				case(1):{
					// Punto para jugador 2 si no sale por la derecha
					if( !(ball.x > Pong.WIDTH)){
						score.sumPoint(1, playing);
					}
				} break;
				case(2):{
					// Punto para jugador 3 si no sale abjo
					if( !(ball.y > Pong.HEIGHT)){
						score.sumPoint(2, playing);
					}
				}break;
				case(3):{
					// Punto para jugador 4 si no sale arriba
					if( !(ball.y < 0)){
						score.sumPoint(3, playing);
					}
				}
			}
			
			lastPlayer = -1;
			ball.reset();
		}
		return lastPlayer;
	}

	private static int handleHumanBounce(int i, Rectangle bar, PongBall ball, int lastPlayer){
		double step = 1+DV;
	    switch(i){
	    	// jugador a la izquierda
	    	case(0):{
	    		if(ball.willCrashWithBarByRight(bar, step)){
	    			ball.changeXDir(step);
	    			return 0;
	    		}
	    	}
	    	// jugador a la derecha
	    	case(1):{ 
	    		if(ball.willCrashWithBarByLeft(bar, step)){
	    			ball.changeXDir(step);
	    			return 1;
	    		}
	    	}
	    	//jugador inferior
	    	case(2):{
	    		if(ball.willCrashWithBarByTop(bar, step)){
	    			ball.changeYDir(step);
	    			return 2;
	    		}
	    	}
	    	// jugador superior
	    	case(3):{
	    		if(ball.willCrashWithBarByBottom(bar, step)){
	    			ball.changeYDir(step);
	    			return 3;
	    		}
	    	}
	    }
	    return lastPlayer;
	}

	private static void handleBounce(int i, PongBall ball){
		switch(i){
			// izquierda
			case(0):{
				if(ball.checkIfGoesLeft(0, DX))
					ball.changeXDir(1);
			} break;
			// derecha
			case(1):{
				if(ball.checkIfGoesRight(WIDTH, DX)){
					ball.changeXDir(1);
				}
			} break;
			// abajo
			case(2):{
				if(ball.checkIfGoesDown(HEIGHT, DX)){
					ball.changeYDir(1);
				}

			} break;
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
	
	private void handleQuitEvent(){
		// Manejo de Q, ESC
		if(keys[KeyEvent.VK_Q] || keys[KeyEvent.VK_ESCAPE]){
			stop();
			return;
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

	public void reMatch() {
		ball = new PongBall();
		ball.reset();
		lastPlayer = -1;
		scores.reset(client.getPlaying());
	}

	public void showWinner() {
		scores.showWinner();
		historical.addWinner(scores.getWinner());
		historical.showScores();
	}
	
	private void showPauseMessage(String message){
		scores.showPause(message);
	}
}