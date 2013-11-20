package cl.dcc.cc5303.client;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;

import cl.dcc.cc5303.GameBar;
import cl.dcc.cc5303.HistoricalScoreBoardGUI;
import cl.dcc.cc5303.MyCanvas;
import cl.dcc.cc5303.Pong;
import cl.dcc.cc5303.PongI;
import cl.dcc.cc5303.Rectangle;
import cl.dcc.cc5303.ScoreBoardGUI;

public class ClientPong extends Pong implements KeyListener, PongI {
	public final static String TITLE = "Pong - CC5303";
	private JFrame frame;
	private MyCanvas canvas;
	public Client client;
	private boolean[] keys;
	
	public ClientUpdateThread serverUpdate;
	public ClientGameThread game;

	public ClientPong(Client client, int playerNum) {
		super();
		this.client    = client;
		this.serverUpdate = new ClientUpdateThread(this.client);
		this.game         = new ClientGameThread(this);
		this.keys = new boolean[KeyEvent.KEY_LAST];
		this.scores = new ScoreBoardGUI(this.game.state().playing, playerNum);
		this.historical = new HistoricalScoreBoardGUI(playerNum, game.state().playing);
		init(playerNum);
	}

	/* Initializes window frame and set it visible */
	private void init(int playerNum) {
		canvas = new MyCanvas(playerNum, game.state().ball);
		frame = new JFrame(TITLE);
		frame.setLayout(new BorderLayout());
		frame.setSize(WIDTH, HEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(canvas, BorderLayout.CENTER);
		frame.add((ScoreBoardGUI) scores, BorderLayout.NORTH);
		frame.add((HistoricalScoreBoardGUI) historical, BorderLayout.SOUTH);
		
		canvas.setSize(WIDTH, HEIGHT);
		for(int i = 0; i < game.state().bars.length; i++){
			if(game.state().isPlaying(i))
				canvas.rectangles.add((GameBar)game.state().bars[i]);
		}
		canvas.addKeyListener(this);
		
		frame.pack();
		frame.setVisible(true);
		canvas.init();
		frame.addKeyListener(this);
		
		((HistoricalScoreBoardGUI)historical).showScores(game.state().playing);
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
	
	public void handlePlayerBars(){
		for(int i = 0; i < MAX_PLAYERS; i++){
			canvas.rectangles.remove(game.state().bars[i]);

			if(this.game.state().isPlaying(i)){
				canvas.rectangles.add((GameBar) game.state().bars[i]);
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
		Rectangle bar = game.state().bars[playerPos];
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

			game.state().bars[playerPos].y = (int) bar.y;
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

			game.state().bars[playerPos].x = (int) bar.x;
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
		return;
	}

	public void showWinner() {
		((ScoreBoardGUI) scores).showWinner();
		((HistoricalScoreBoardGUI) historical).showScores(game.state().playing);
	}
	
	public void showPauseMessage(String message){
		((ScoreBoardGUI) scores).showPause(message);
	}

	public void rePaint() {
		canvas.repaint();
	}

}