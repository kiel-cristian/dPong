package cl.dcc.cc5303;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedHashMap;


public class Server extends UnicastRemoteObject implements IServer {
	private static final long serialVersionUID = -8181276888826913071L;
	private LinkedHashMap<Integer, Match> matches;
	private int numPlayers;
	private int matchCount;

	protected Server(int numPlayers) throws RemoteException {
		super();
		this.numPlayers = numPlayers;
		matches = new LinkedHashMap<Integer, Match>();
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
	public synchronized GameInfo connectPlayer(Player player) throws RemoteException {
		Match match = getAvailableMatch();
		int playerNum = match.addPlayer(player);
		return new GameInfo(match.getID(), playerNum);
	}
	
	private Match getAvailableMatch() {
		Match match = null;
		for (Match m : matches.values()) {
			if (m.playersCount() < Match.MAX_PLAYERS) {
				match = m;
				break;
			}
		}
		if (match == null) {
			match = new Match(++matchCount, numPlayers);
			matches.put(match.getID(), match);
		}
		return match;
	}

	@Override
	public synchronized void disconnectPlayer(int matchID, int playerNum) throws RemoteException {
		matches.get(matchID).removePlayer(playerNum);
	}

	@Override
	public synchronized GameState updatePositions(int matchID, int playerNum, int position) throws RemoteException {
		return matches.get(matchID).updatePositions(playerNum, position);
	}
}
