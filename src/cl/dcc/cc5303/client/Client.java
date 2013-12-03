package cl.dcc.cc5303.client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import cl.dcc.cc5303.CommandLineParser;
import cl.dcc.cc5303.GameState;
import cl.dcc.cc5303.CommandLineParser.ParserException;
import cl.dcc.cc5303.server.ServerFinderI;
import cl.dcc.cc5303.server.ServerI;

public class Client extends UnicastRemoteObject implements PlayerI {
	private static final long serialVersionUID = -1910265532826050466L;
	private volatile ServerFinderI serverFinderI;
	public volatile ServerI server;
	public volatile ClientGameInfo info;
	public volatile ClientPong pong;

	public static void main(String[] args) {
		try {
			Client client = new Client();
			CommandLineParser parser = new CommandLineParser(args);
			parser.addStringOption("a");
			parser.addIntegerOption("n", 1);
			parser.parse();
			String serverFinderAddress = parser.getString("a", "localhost");
			int serverID = parser.getInt("n", -1);
			client.play(serverFinderAddress, serverID);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (ParserException e) {
			System.out.println(e.getMessage());
		}
	}
	protected Client() throws RemoteException {
		super();
	}
	
	public void userPaused() {
		pong.userPaused();
	}
	
	public void userUnPaused() {
		pong.userUnPaused();
	}

	public void play(String serverFinderAddress, int serverID) throws MalformedURLException, RemoteException, NotBoundException {
		serverFinderI = (ServerFinderI) Naming.lookup("rmi://" + serverFinderAddress + ":1099/serverfinder");
		if (serverID == -1) {
			server = serverFinderI.getServer();
		}
		else {
			server = serverFinderI.getServer(serverID);
		}
		info = server.connectPlayer(this);
		pong = new ClientPong(this, info.playerNum);
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
	public void migrate(final ServerI targetServer, final String targetMatchID) throws RemoteException {
		pong.serverUpdate.pause();
		pong.game.pause();
		final Client self = this;

		Thread migrator = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					ServerI oldServer = self.server;
					String oldMatch = self.info.matchID;
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
	
	public void togglePause() {
		try {
			server.togglePause(info.matchID);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}