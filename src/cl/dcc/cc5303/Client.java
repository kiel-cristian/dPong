package cl.dcc.cc5303;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Client extends UnicastRemoteObject implements Player {
	private static final long serialVersionUID = -1910265532826050466L;
	private static int REFRESH_TIME = 50;
	private ServerFinder serverFinder;
	private IServer server;
	private volatile int matchID;
	private volatile int playerNum;
	private volatile int lastPlayer;
	private volatile boolean[] playing = new boolean[4];
	private volatile int[] scores = new int[4];
	private volatile int ballX;
	private volatile int ballY;
	private volatile double vx;
	private volatile double vy;
	private volatile int[] barPos = new int[4];
	private volatile Pong pong;
	private volatile boolean winner;
	private volatile int minPlayers;
	private ServerUpdateThread serverUpdate;

	public static void main(String[] args) {
		try {
			Client client = new Client();
			String serverFinderAddress;
			int serverID = -1;
			if (args.length > 0) {
				serverFinderAddress = args[0];
				if (args.length > 1) {
					serverID = Integer.parseInt(args[1]);
				}
			}
			else
				serverFinderAddress = "localhost";
			client.play(serverFinderAddress, serverID);
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
	}

	public void play(String serverFinderAddress, int serverID)
			throws MalformedURLException, RemoteException, NotBoundException {
		serverFinder = (ServerFinder) Naming.lookup("rmi://" + serverFinderAddress + ":1099/serverfinder");
		if (serverID == -1) {
			server = serverFinder.getServer();
		}
		else {
			server = serverFinder.getServer(serverID);
		}
		GameInfo info = server.connectPlayer(this);
		matchID = info.matchID;
		playerNum = info.playerNum;
		playing[playerNum] = true;
		lastPlayer = -1;

		serverUpdate = new ServerUpdateThread();
		pong = new Pong(this, serverUpdate);
		pong.startGame();
	}

	public boolean getWinner(){
		return winner;
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

	public boolean playersReady() {
		return Utils.countTrue(playing) >= minPlayers;
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

	public void stop(int playerNum) {
		try {
			server.disconnectPlayer(matchID, playerNum);
		} catch (RemoteException e1) {
			e1.printStackTrace();
			System.out.println("Error al desconectarse del servidor");
		}
	}

	public PongBall getBall() {
		return new PongBall(ballX, ballY, vx, vy);
	}

	@Override
	public void migrate(IServer server, int targetMatchID)
			throws RemoteException {
		serverUpdate.pause();
		this.server.disconnectPlayer(this.matchID, playerNum);
		// Se reemplaza referencia al nuevo server
		this.server = server;
		this.matchID = targetMatchID;
		// conexion
		this.server.connectPlayer(this, targetMatchID, playerNum);
		serverUpdate.unPause();
	}
	
	protected synchronized void updateScores(int[] scores2) {
		for(int i = 0; i < Pong.MAX_PLAYERS ; i++){
			scores[i]   = scores2[i];
		}
		pong.scores.setScores(scores);
	}

	protected synchronized void checkWinners(GameState state) {
		updateScores(state.scores);
		if(winner == false && state.winner){
			pong.scores.setWinner(getScores(), state.winnerPlayer);
			pong.showWinner();
			winner = true;
		}
		if(winner == true && !state.winner){
			winner = false;
			pong.reMatch();
		}
	}
	
	private class ServerUpdateThread extends Thread {
		private boolean running = true;

		@Override
		public void run() {

			while (running) {
				try {
					GameState state = server.updatePositions(matchID, playerNum, getBarPosition(playerNum));
					minPlayers = state.minPlayers;
					checkWinners(state);

					if(!getWinner()){
						for(int i = 0; i < Pong.MAX_PLAYERS ; i++){
							barPos[i] 	= state.barsPos[i];
							playing[i] 	= state.playing[i];
						}
						ballX = state.ballX;
						ballY = state.ballY;
						vx = state.vx;
						vy = state.vy;
					}
					Thread.sleep(REFRESH_TIME);
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					System.out.println("Server Update:" + playerNum + " muriendo");
					running = false;
					return;
				}
			}
		}
		
		public void pause() {
			running = false;
		}
		
		public void unPause() {
			running = true;
		}
	}
}