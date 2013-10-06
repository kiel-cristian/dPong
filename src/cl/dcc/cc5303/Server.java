package cl.dcc.cc5303;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class Server extends UnicastRemoteObject implements IServer {
	private static final long serialVersionUID = -8181276888826913071L;
	private boolean[] playing;
	private Rectangle[] bars;
	private PongBall ball;
	private Player[] players;
	private boolean running;
	private Thread simulationThread;

	protected Server() throws RemoteException {
		super();
		bars = new Rectangle[4];
		bars[0] = new Rectangle(10, Pong.HEIGHT / 2, 10, 100);
		bars[1] = new Rectangle(Pong.WIDTH - 10, Pong.HEIGHT / 2, 10, 100);
		bars[2] = new Rectangle(Pong.WIDTH/2, Pong.HEIGHT - 10, 10, 100);
		bars[3] = new Rectangle(Pong.WIDTH/2, 10, 10, 100);

		ball = new PongBall(Pong.WIDTH * 0.5, Pong.HEIGHT * 0.5, 10, 10);

		playing = new boolean[4];
		players = new Player[4];
	}

	public static void main(String[] args) {
		try {
			IServer server = new Server();
			Naming.rebind("rmi://localhost:1099/server", server);
			System.out.println("Escuchando...");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
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
	
	private int addPlayer(Player player, int num) {
		players[num] = player;
		playing[num] = true;
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
	
	private boolean playersReady() {
		return this.currentPlayers() >= 2;
	}
	
	private void startGame() {
		simulationThread = new PongSimulation(this.currentPlayers());
		running = true;
		simulationThread.start();
		System.out.println("Empieza el juego!");
	}

	@Override
	public GameState updatePositions(int playerNum, int position)
			throws RemoteException {
		bars[playerNum].y = position;
		return new GameState(playing, bars, ball);
	}
	
	private class PongSimulation extends Thread {
		private int players;

		public PongSimulation(int currentPlayers){
			this.players = currentPlayers;
		}

		public void run() {
			while (running) {
				
				if(this.players > 2){
					Pong.doGameIteration(playing, bars, ball);
				}
				else{
					Pong.doGameIteration(bars, ball);
				}
				
				try {
					Thread.sleep(1000 / Pong.UPDATE_RATE); // milliseconds
				} catch (InterruptedException ex) {
				}
			}
		}
	}
}
