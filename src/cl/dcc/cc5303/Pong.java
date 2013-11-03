package cl.dcc.cc5303;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;


public class Pong implements KeyListener {

	public final static String TITLE = "Pong - CC5303";
	public final static int WIDTH = 640, HEIGHT = 480;
	public final static int DX = 5;
	public final static double DV = 0.3;
	public final static int MAX_PLAYERS = 4;
	public final static int WINNING_SCORE = 2;

	private JFrame frame;
	private MyCanvas canvas;
	public Client client;

	public static ScoreBoardGUI scores;
	public HistoricalScoreBoardGui historical;

	private boolean[] keys;
	
	public PongThread serverUpdate;
	public PongGame game;

	public Pong(Client client) {
		this.client    = client;
		this.serverUpdate = new PongServerUpdate(this.client);
		this.game         = new PongGame(this);
		this.keys = new boolean[KeyEvent.KEY_LAST];
		init();
	}

	/* Initializes window frame and set it visible */
	private void init() {
		canvas = new MyCanvas(client.info.playerNum, game.state.ball);
		scores = new ScoreBoardGUI(game.state.playing, client.info.playerNum);
		historical = new HistoricalScoreBoardGui(client.info.playerNum);
		
		frame = new JFrame(TITLE);
		frame.setLayout(new BorderLayout());
		frame.setSize(WIDTH, HEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(canvas, BorderLayout.CENTER);
		frame.add(scores, BorderLayout.NORTH);
		frame.add(historical, BorderLayout.SOUTH);
		
		canvas.setSize(WIDTH, HEIGHT);
		for(int i = 0; i < game.state.bars.length; i++){
			if(game.state.isPlaying(i))
				canvas.rectangles.add((GameBar)game.state.bars[i]);
		}
		canvas.addKeyListener(this);
		
		frame.pack();
		frame.setVisible(true);
		canvas.init();
		frame.addKeyListener(this);
		
		historical.showScores();
	}
	
	public void startGame() {
		serverUpdate.start();
		game.start();
	}
	
	public void stopPongThreads() {
		serverUpdate.end();
		game.end();
		
		System.exit(0);// FIXME
	}
	
	private void stop(){
		frame.dispose();
		client.stop(); // Notificacion al server
		stopPongThreads();
	}
	
	public static synchronized int doGameIteration(GameState state) {
		for (int i = 0; i < 4;  i++){
			if(state.playing[i] == true){
				state.lastPlayer = handleHumanBounce(i, state);
			}
			else{
				handleBounce(i, state);
			}
		}

		state.lastPlayer = handleBall(state);
		return state.lastPlayer;
	}
	
	private static int handleBall(GameState state) {
		// actualiza posicion
		state.ball.x += state.ball.vx * DX;
		state.ball.y += state.ball.vy * DX;

		if(state.ball.x > Pong.WIDTH || state.ball.x < 0 || state.ball.y < 0 || state.ball.y > Pong.HEIGHT){
			switch(state.lastPlayer){
				case(0):{
					// Punto para jugador 1 si no sale por la izquierda
					if(!(state.ball.x < 0)){
						scores.sumPoint(0, state.playing);
					}
				} break;
				case(1):{
					// Punto para jugador 2 si no sale por la derecha
					if( !(state.ball.x > Pong.WIDTH)){
						scores.sumPoint(1, state.playing);
					}
				} break;
				case(2):{
					// Punto para jugador 3 si no sale abajo
					if( !(state.ball.y > Pong.HEIGHT)){
						scores.sumPoint(2, state.playing);
					}
				}break;
				case(3):{
					// Punto para jugador 4 si no sale arriba
					if( !(state.ball.y < 0)){
						scores.sumPoint(3, state.playing);
					}
				}
			}
			
			state.lastPlayer = -1;
			state.ball.reset();
		}
		return state.lastPlayer;
	}
	
	private static int handleHumanBounce(int i, GameState state){
		double step = 1+DV;
	    switch(i){
	    	// jugador a la izquierda
	    	case(0):{
	    		if(state.ball.willCrashWithBarByRight(state.bars[i], step)){
	    			state.ball.changeXDir(step);
	    			return 0;
	    		}
	    	}
	    	// jugador a la derecha
	    	case(1):{ 
	    		if(state.ball.willCrashWithBarByLeft(state.bars[i], step)){
	    			state.ball.changeXDir(step);
	    			return 1;
	    		}
	    	}
	    	//jugador inferior
	    	case(2):{
	    		if(state.ball.willCrashWithBarByTop(state.bars[i], step)){
	    			state.ball.changeYDir(step);
	    			return 2;
	    		}
	    	}
	    	// jugador superior
	    	case(3):{
	    		if(state.ball.willCrashWithBarByBottom(state.bars[i], step)){
	    			state.ball.changeYDir(step);
	    			return 3;
	    		}
	    	}
	    }
	    return state.lastPlayer;
	}
	
	private static void handleBounce(int i, GameState state){
		switch(i){
			// izquierda
			case(0):{
				if(state.ball.checkIfGoesLeft(0, DX))
					state.ball.changeXDir(1);
			} break;
			// derecha
			case(1):{
				if(state.ball.checkIfGoesRight(WIDTH, DX)){
					state.ball.changeXDir(1);
				}
			} break;
			// abajo
			case(2):{
				if(state.ball.checkIfGoesDown(HEIGHT, DX)){
					state.ball.changeYDir(1);
				}

			} break;
			// arriba
			case(3):{
				if(state.ball.checkIfGoesUp(0, DX))
					state.ball.changeYDir(1);
			}
		}
	}
	
	public void handlePlayerBars(){
		for(int i = 0; i < MAX_PLAYERS; i++){
			canvas.rectangles.remove(game.state.bars[i]);

			if(this.game.state.isPlaying(i)){
				canvas.rectangles.add((GameBar) game.state.bars[i]);
			}
		}
	}

	public void handleQuitEvent(){
		// Manejo de Q, ESC
		if(keys[KeyEvent.VK_Q] || keys[KeyEvent.VK_ESCAPE]){
			stop();
			return;
		}
	}
	
	public void handleKeyEvents(int playerPos) {
		Rectangle bar = game.state.bars[playerPos];
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

			game.state.bars[playerPos].y = (int) bar.y;
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

			game.state.bars[playerPos].x = (int) bar.x;
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

	public void showWinner() {
		scores.showWinner();
		historical.addWinner(scores.getWinner());
		historical.showScores();
	}
	
	public void showPauseMessage(String message){
		scores.showPause(message);
	}

	public void rePaint() {
		canvas.repaint();
	}

}