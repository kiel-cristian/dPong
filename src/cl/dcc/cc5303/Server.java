package cl.dcc.cc5303;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedHashMap;
import java.util.Map;

public class Server extends UnicastRemoteObject implements IServer, ServerFinder {
	private static final long serialVersionUID = -8181276888826913071L;
	private static ILoadBalancer loadBalancer;
	private LinkedHashMap<Integer, Match> matches;
	private LinkedHashMap<Integer, Match> incomingMatches;
	private int minPlayers;
	private volatile int matchCount;
	private volatile int playerCount;
	private int serverID;

	protected Server(int minPlayers) throws RemoteException {
		super();
		this.minPlayers = minPlayers;
		matches = new LinkedHashMap<Integer, Match>();
		incomingMatches = new LinkedHashMap<Integer, Match>();
		if (loadBalancer != null) {
			serverID = loadBalancer.connectServer(this);
			System.out.println("Server ID: " + serverID);
		}
	}

	public static void main(String[] args) {
		try {
			int players = 2; // Numero de jugadores por default

			if (args.length > 0){
				players = Integer.parseInt(args[0]);
			}
			if (args.length > 1){
				loadBalancer = (ILoadBalancer) Naming.lookup("rmi://" + args[1] + ":1099/serverfinder");
				new Server(players);
			}
			else {
				ServerFinder server = new Server(players);
				RMISocketFactory.setSocketFactory(new FixedPortRMISocketFactory());
				LocateRegistry.createRegistry(1099);
				Naming.rebind("rmi://localhost:1099/serverfinder", server);
			}			
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
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public synchronized GameInfo connectPlayer(Player player) throws RemoteException {
		Match match = getAvailableMatch();
		int playerNum = match.addPlayer(player);
		increasePlayerNum();
		return new GameInfo(match.getID(), playerNum);
	}
	
	private Match getAvailableMatch() {
		Match match = null;
		for (Match m : matches.values()) {
			if (m.playersCount() < Pong.MAX_PLAYERS && !m.migrating()) {
				match = m;
				break;
			}
		}
		if (match == null) {
			match = new Match(this, ++matchCount, minPlayers);
			matches.put(match.getID(), match);
		}
		return match;
	}

	@Override
	public synchronized void disconnectPlayer(int matchID, int playerNum) throws RemoteException {
		matches.get(matchID).removePlayer(playerNum);
		decreasePlayerNum();
	}

	@Override
	public synchronized GameState updatePositions(int matchID, int playerNum, int position) throws RemoteException {
		Match m = matches.get(matchID);
		if (m == null) {
			m = incomingMatches.get(matchID);
		}
		return m.updatePositions(playerNum, position);
	}

	@Override
	public IServer getServer() throws RemoteException {
		return this;
	}
	
	private void increasePlayerNum() {
		playerCount++;
		reportLoad();
	}
	
	private void decreasePlayerNum() {
		playerCount--;
		reportLoad();
	}
	
	private void reportLoad() {
		if (loadBalancer != null) {
			try {
				loadBalancer.reportLoad(serverID, playerCount);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	// Fijado en que el server migre 1/2 de sus matches
	private boolean needToMigrate(int migratedMatchesCount, int currentMatches){
		return migratedMatchesCount < currentMatches/2;
	}
	
	public void migrateMatches(IServer targetServer) throws RemoteException {
		try {
			Match selectedMatch;
			int migratedMatches = 0;
			int currentMatches  = matches.size();
			
			for (Map.Entry<Integer, Match> e : matches.entrySet()) {
				selectedMatch = matches.get(e.getKey());
				int targetMatch = targetServer.getMatchForMigration(selectedMatch.startMigration());System.out.println("migrando jugadores!");
				selectedMatch.migratePlayers(targetServer, targetMatch);
				migratedMatches++;
				if(!needToMigrate(migratedMatches, currentMatches)){
					break;
				}
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void migrateMatch(int matchID) {
		try {
			IServer targetServer = loadBalancer.getServerForMigration(serverID);
			if (targetServer == null) return; // No puede migrar a ninguna parte :(
			Match m = matches.get(matchID);
			int targetMatch = targetServer.getMatchForMigration(m.startMigration());
			m.migratePlayers(targetServer, targetMatch);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public int getMatchForMigration(GameState stateToMigrate)
			throws RemoteException {
		Match match = new Match(this, ++matchCount, stateToMigrate.minPlayers);
		match.receiveMigration(stateToMigrate);
		incomingMatches.put(match.getID(), match);
		return match.getID();
	}
	
	public void connectPlayer(Player player, int matchID, int playerNum)
			throws RemoteException {
		synchronized (this) {
			Match m = incomingMatches.get(matchID);
			m.addPlayer(player, playerNum);
			increasePlayerNum();
			if (m.migrationReady()) {
				m.stopMigration();
				matches.put(matchID, m);
				incomingMatches.remove(matchID);
			}
		}
	}

	public void removeMatch(int matchID) {
		matches.remove(matchID);
	}

	@Override
	public IServer getServer(int serverID) throws RemoteException {
		return getServer();
	}
	
	public void heartBeat() throws RemoteException{
		return;
	}
}
