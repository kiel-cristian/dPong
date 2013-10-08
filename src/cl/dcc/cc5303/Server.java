package cl.dcc.cc5303;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;


public class Server extends UnicastRemoteObject implements IServer {
	private static final long serialVersionUID = -8181276888826913071L;
	private static final long INACTIVITY_TIMEOUT = 3000;
	private boolean[] playing;
	private Rectangle[] bars;
	private int lastPlayer;
	private PongBall ball;
	private Player[] players;
	private long[] lastActivity;
	private int playersNum;
	private Thread simulationThread;
	private ScoreBoardSimple score;
	private boolean winner;

	protected Server(int numPlayers) throws RemoteException {
		super();
		bars = new Rectangle[4];
		bars[0] = new Rectangle(10, Pong.HEIGHT / 2, 10, 100);
		bars[1] = new Rectangle(Pong.WIDTH - 10, Pong.HEIGHT / 2, 10, 100);
		bars[2] = new Rectangle(Pong.WIDTH/2, Pong.HEIGHT - 10, 100, 10);
		bars[3] = new Rectangle(Pong.WIDTH/2, 10, 100, 10);

		ball = new PongBall();
		playing = new boolean[4];
		players = new Player[4];
		lastActivity = new long[4];
		playersNum = numPlayers;
		score      = new ScoreBoardSimple();
		winner     = false;
	}
	
	private void resetGame(){
		bars[0] = new Rectangle(10, Pong.HEIGHT / 2, 10, 100);
		bars[1] = new Rectangle(Pong.WIDTH - 10, Pong.HEIGHT / 2, 10, 100);
		bars[2] = new Rectangle(Pong.WIDTH/2, Pong.HEIGHT - 10, 100, 10);
		bars[3] = new Rectangle(Pong.WIDTH/2, 10, 100, 10);
		ball    = new PongBall();
		lastPlayer = -1;
		winner     = false;
		score.reset();
		
		for(int i = 0; i < Pong.MAX_PLAYERS; i++){
			// Restaurar juego en cliente
			if(playing[i]){
				try {
					players[i].reMatch();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		try {
			int players = 2; // Numero de jugadores por default

			if (args.length > 0){
				players = Integer.parseInt(args[0]);
			}

			RMISocketFactory.setSocketFactory(new FixedPortRMISocketFactory());
			IServer server = new Server(players);
			LocateRegistry.createRegistry(1099);
			Naming.rebind("rmi://localhost:1099/server", server);
			
			System.out.println("Escuchando...");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public int connectPlayer(Player player) throws RemoteException {
		int playerNum = 666;
		if (!playing[0])
			playerNum = addPlayer(player, 0);
		else if (!playing[1])		
			playerNum = addPlayer(player, 1);
		else if (!playing[2])
			playerNum = addPlayer(player, 2);
		else if (!playing[3])
			playerNum = addPlayer(player, 3);
		return playerNum;
	}
	
	@Override
	public synchronized void disconnectPlayer(int playerNum) throws RemoteException {
		removePlayer(playerNum);
	}
	
	private void removePlayer(int playerNum) {
		playing[playerNum] = false;
		players[playerNum] = null;
		
		// Se pone en 0 el puntaje del jugador que se fue
		int[] scores = score.getScores();
		scores[playerNum]  = 0;
		score.setScores(scores);
		
		if(!(this.playersReady())){
			System.out.println("Juego pausado por falta de jugadores");
			simulationThread.interrupt();
		}
	}
	
	private int addPlayer(Player player, int num) {
		players[num] = player;
		playing[num] = true;
		lastActivity[num] = System.currentTimeMillis();
		System.out.println("Jugador solicita conectarse. Se le asigna player " + (num + 1));
		if (playersReady())
			startGame();
		return num;
	}

	public int currentPlayers(){
		int readyPlayers = 0;
		for(boolean p : playing){
			if(p){
				readyPlayers++;
			}
		}
		return readyPlayers;
	}
	
	public boolean playersReady() {
		return this.currentPlayers() >= this.playersNum;
	}
	
	private void startGame() {
		simulationThread = new PongSimulation();
		lastPlayer = -1;
		simulationThread.start();
		System.out.println("Empieza el juego!");
	}

	@Override
	public synchronized GameState updatePositions(int playerNum, int position) throws RemoteException {
		if(playerNum == 0 || playerNum == 1){
			bars[playerNum].y = position;
		}
		else{
			bars[playerNum].x = position;
		}

		lastActivity[playerNum] = System.currentTimeMillis();
		return new GameState(playing, bars, ball, score.getScores(), winner);
	}
	
	private void checkForWinnerServer(){
		if(score.getWinner() >= 0){
			winner = true;
		}
	}

	private class PongSimulation extends Thread {

		public void run() {
			boolean running = true;
			
			while (running) {
				try {
					lastPlayer = Pong.doGameIteration(playing, bars, ball, score, lastPlayer);

					checkForWinnerServer();
					checkPlayersActivity();
					
					if(winner){
						Thread.sleep(3000 / Pong.UPDATE_RATE);
						resetGame();
					}
					else{
						Thread.sleep(1000 / Pong.UPDATE_RATE); // milliseconds
					}
				} catch (InterruptedException ex) {
					running = false;
				}
			}
		}
	}
	
	private void checkPlayersActivity() {
		for (int i=0; i < lastActivity.length; i++) {
			if (playing[i] && (System.currentTimeMillis() - lastActivity[i] > INACTIVITY_TIMEOUT)) {
				removePlayer(i);
			}
		}
	}
}
