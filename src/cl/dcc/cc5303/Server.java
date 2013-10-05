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
		bars = new Rectangle[2];
		bars[0] = new Rectangle(10, Pong.HEIGHT / 2, 10, 100);
		bars[1] = new Rectangle(Pong.WIDTH - 10, Pong.HEIGHT / 2, 10, 100);
		ball = new PongBall(Pong.WIDTH * 0.5, Pong.HEIGHT * 0.5, 10, 10);
		playing = new boolean[2];
		players = new Player[2];
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
		return playerNum;
	}
	
	private int addPlayer(Player player, int num) {
		players[num] = player;
		playing[num] = true;
		System.out.println("Jugador solicita conectarse. Se le asigna player " + (num + 1));
		if (allPlayersReady())
			startGame();
		return num;
	}
	
	private boolean allPlayersReady() {
		return playing[0] && playing[1];
	}
	
	private void startGame() {
		simulationThread = new PongSimulation();
		running = true;
		simulationThread.start();
		System.out.println("Empieza el juego!");
	}

	@Override
	public GameState updatePositions(int playerNum, int position)
			throws RemoteException {
		bars[playerNum].y = position;
		return new GameState(bars[0], bars[1], ball);
	}
	
	private class PongSimulation extends Thread {
		public void run() {
			while (running) {
				
				Pong.doGameIteration(bars, ball);
				
				try {
					Thread.sleep(1000 / Pong.UPDATE_RATE); // milliseconds
				} catch (InterruptedException ex) {
				}
			}
		}
	}
}
