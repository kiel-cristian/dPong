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

import cl.dcc.cc5303.CommandLineParser.ParserException;

public class Server extends UnicastRemoteObject implements IServer, ServerFinder {
	private static final long serialVersionUID = -8181276888826913071L;
	private static ServerOptions options;
	private static ILoadBalancer loadBalancer;
	private LinkedHashMap<Integer, Match> matches;
	private int minPlayers;
	private volatile int matchCount;
	private volatile int playerCount;
	private int serverID;
	private MigrationHandler migrationHandler;
	private boolean migrating;
	
	protected Server(int minPlayers) throws RemoteException {
		super();
		this.minPlayers = minPlayers;
		matches = new LinkedHashMap<Integer, Match>();
		if (loadBalancer != null) {
			serverID = loadBalancer.connectServer(this);
			System.out.println("Server ID: " + serverID);
		}
	}

	public static void main(String[] args) {
		try {
			options = parseOptions(args);
			
			if (options.loadBalancer){
				loadBalancer = (ILoadBalancer) Naming.lookup("rmi://" + options.balancerUrl + ":1099/serverfinder");
				new Server(options.minPlayers);
			}
			else {
				ServerFinder server = new Server(options.minPlayers);
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
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private static ServerOptions parseOptions(String[] args) throws ParserException {
		ServerOptions options = new ServerOptions();
		CommandLineParser parser = new CommandLineParser(args);
		parser.addOption("b", "string", "localhost"); 	// Balanceador: localhost por defecto
		parser.addOption("n", "int");
		parser.greaterThanRule("n", 1, "Debe introducir un número válido de jugadores para las partidas (mínimo 2)");
		parser.lessThanRule("n", 5, "Debe introducir un número válido de jugadores para las partidas (máximo 4)");
		parser.parse();
		options.minPlayers = parser.getInt("n", 2); // Minimo de jugadores: 2 por defecto
		options.loadBalancer = parser.containsString("b");
		options.balancerUrl = parser.getString("b", null);
		return options;
	}
	
	private static class ServerOptions {
		public boolean loadBalancer;
		public String balancerUrl;
		public int minPlayers;
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
			
		if(!m.migrating()){
			return m.updatePositions(playerNum, position);
		}
		else {
			return m.lastPositions();
		}
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
				boolean acceptableLoad = loadBalancer.reportLoad(serverID, playerCount);
				if (!acceptableLoad) {
					migrateMatches();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void migrateMatches() throws RemoteException {
		if (!migrating) {
			migrating = true;
			migrationHandler = new MigrationHandler();
			migrationHandler.start();
		}
	}
	
	// Fijado en que el server migre 1/2 de sus matches
	private boolean needToMigrate(int migratedMatchesCount, int currentMatches){
		return migratedMatchesCount < currentMatches/2;
	}
	
	private void migrateMatches(IServer targetServer) {
		try {
			Match selectedMatch;
			int migratedMatches = 0;
			int currentMatches  = matches.size();
			
			for (Map.Entry<Integer, Match> e : matches.entrySet()) {
				selectedMatch = matches.get(e.getKey());
				int targetMatch = targetServer.getMatchForMigration(selectedMatch.startMigration());
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

	@Override
	public synchronized int getMatchForMigration(GameState stateToMigrate)
			throws RemoteException {
		Match match = new Match(this, ++matchCount, stateToMigrate.minPlayers);
		match.receiveMigration(stateToMigrate);
		matches.put(match.getID(), match);
		return match.getID();
	}
	
	@Override
	public synchronized void connectPlayer(Player player, int matchID, int playerNum)
			throws RemoteException {
		Match m = matches.get(matchID);
		m.addPlayer(player, playerNum);
		increasePlayerNum();
		if (m.migrationReady()) {
			m.stopMigration();
		}
	}

	public void removeMatch(int matchID) {
		matches.remove(matchID);
		System.out.println("Partida " + matchID + " eliminada por falta de jugadores");
	}

	@Override
	public IServer getServer(int serverID) throws RemoteException {
		return getServer();
	}
	
	@Override
	public void heartBeat() throws RemoteException{
		return;
	}
	
	private class MigrationHandler extends Thread {
		
		@Override
		public void run() {
			try {
				IServer migrationServer = loadBalancer.getServerForMigration(serverID);
				if (migrationServer != null) {
					migrateMatches(migrationServer);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			} finally {
				migrating = false;
			}
		}
	}
}
