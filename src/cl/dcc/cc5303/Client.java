package cl.dcc.cc5303;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Client extends UnicastRemoteObject implements Player {
	private static final long serialVersionUID = -1910265532826050466L;
	private volatile ServerFinder serverFinder;
	public volatile IServer server;
	public volatile GameInfo info;
	public volatile PongClient pong;

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
			{
				serverFinderAddress = "localhost";
				serverID = Integer.parseInt("1"); // por default se va siempre al primer server
			}
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

	public void play(String serverFinderAddress, int serverID) throws MalformedURLException, RemoteException, NotBoundException {
		serverFinder = (ServerFinder) Naming.lookup("rmi://" + serverFinderAddress + ":1099/serverfinder");
		if (serverID == -1) {
			server = serverFinder.getServer();
		}
		else {
			server = serverFinder.getServer(serverID);
		}
		info = server.connectPlayer(this);
		pong = new PongClient(this, info.playerNum);
		pong.game.enablePlayer(info.playerNum);
		pong.startGame();
	}

	public int getPlayerNum() {
		return info.playerNum;
	}

	public int getBarPosition() {
		return pong.game.state().getPlayerPosition(info.playerNum);
	}

	public void stop() {
		try {
			server.disconnectPlayer(info.matchID, info.playerNum);
		} catch (RemoteException e1) {
			e1.printStackTrace();
			System.out.println("Error al desconectarse del servidor");
		}
	}
	
	public GameState state(){
		return this.pong.game.state();
	}

	@Override
	public void migrate(final IServer targetServer, final int targetMatchID) throws RemoteException {
		pong.serverUpdate.pause();
		pong.game.pause();
		final Client self = this;

		Thread migrator = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					IServer oldServer = self.server;
					int oldMatch = self.info.matchID;
					synchronized(self.server){
						self.server = targetServer;
					}
					self.info.matchID = targetMatchID;
					oldServer.disconnectPlayer(oldMatch, self.info.playerNum);
					targetServer.connectPlayer(self, targetMatchID, self.info.playerNum);
					pong.serverUpdate.unPause();
					pong.game.unPause();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			
		});
		migrator.start();
	}
}