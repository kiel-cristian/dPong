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
	private int playerNum;
	private volatile int ballX;
	private volatile int ballY;
	private volatile double vx;
	private volatile double vy;
	private volatile int[] barPos;
	
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
	}
	
	public void play(String serverAddress) throws MalformedURLException, RemoteException, NotBoundException {
		server = (IServer) Naming.lookup("rmi://" + serverAddress + ":1099/server");
		playerNum = server.connectPlayer(this);
		barPos = new int[2];
		Thread serverUpdate = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						GameState state = server.updatePositions(playerNum, getBarPosition(playerNum));
						barPos[0] = state.bar1Pos;
						barPos[1] = state.bar2Pos;
						ballX = state.ballX;
						ballY = state.ballY;
						vx = state.vx;
						vy = state.vy;
						Thread.sleep(100);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		});
		serverUpdate.start();
		Pong pong = new Pong(this);
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
	
	public synchronized void setBarPosition(int bar, int position) {
		barPos[bar] = position;
	}
}
