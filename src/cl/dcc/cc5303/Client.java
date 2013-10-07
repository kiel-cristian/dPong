package cl.dcc.cc5303;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Client extends UnicastRemoteObject implements Player {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1910265532826050466L;
	private IServer server;
	private volatile int playerNum;
	private volatile int lastPlayer;
	private volatile boolean[] playing = new boolean[4];
	private volatile int[] scores = new int[4];
	private volatile int ballX;
	private volatile int ballY;
	private volatile double vx;
	private volatile double vy;
	private volatile int[] barPos = new int[4];
	private volatile boolean running;
	
	public static void main(String[] args) {
		try {
			Client client = new Client();
			String serverAddress;
			if (args.length > 0)
				serverAddress = args[0];
			else
				serverAddress = "localhost";
			client.play(serverAddress);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}
	
	protected Client() throws RemoteException {
		super();
		running = true;
	}
		
	public void play(String serverAddress) throws MalformedURLException, RemoteException, NotBoundException {
		server = (IServer) Naming.lookup("rmi://" + serverAddress + ":1099/server");
		playerNum = server.connectPlayer(this);
		playing[playerNum] = true;
		lastPlayer = -1;

		Thread serverUpdate = new Thread(new Runnable() {

			@Override
			public void run() {
				while (running) {
					try {
						GameState state = server.updatePositions(playerNum, getBarPosition(playerNum));
						for(int i = 0; i < Pong.MAX_PLAYERS ; i++){
							barPos[i] 	= state.barsPos[i];
							playing[i] 	= state.playing[i];
							scores[i]   = state.scores[i];
						}
						ballX = state.ballX;
						ballY = state.ballY;
						vx = state.vx;
						vy = state.vy;
						Thread.sleep(100);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return; // Server muerto
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		});
		serverUpdate.start();
		new Pong(this);
	}
	
	public boolean[] getPlaying(){
		boolean[] play = playing;
		return play;
	}

	public int getCurrentPlayers(){
		int players = 0;
		for(boolean p: playing){
			if(p){
				players++;
			}
		}
		return players;
	}

	public boolean playersReady() throws RemoteException{
		return server.playersReady();
	}
	
	public boolean getPlayerStatus(int playerNum){
		return playing[playerNum];
	}
	
	public int getPlayerNum() {
		return playerNum;
	}
	
	public int getBarPosition(int bar) {
		return barPos[bar];
	}
	
	public int getBallX() {
		return ballX;
	}
	
	public int getBallY() {
		return ballY;
	}
	
	public double getVelX() {
		return vx;
	}
	
	public double getVelY() {
		return vy;
	}
	
	public int getLastPLayer(){
		return lastPlayer;
	}
	
	public int[] getScores(){
		return scores;
	}
	
	public synchronized void setBarPosition(int bar, int position) {
		barPos[bar] = position;
	}

	public boolean isRunning() {
		return running;
	}

	public void stop() throws RemoteException {
		running = false;
		server.disconnectPlayer(playerNum);
	}

	public PongBall getBall() {
		return new PongBall(ballX, ballY, vx, vy);
	}
}
