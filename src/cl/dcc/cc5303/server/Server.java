package cl.dcc.cc5303.server;

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

import cl.dcc.cc5303.CommandLineParser;
import cl.dcc.cc5303.FixedPortRMISocketFactory;
import cl.dcc.cc5303.GameStateInfo;
import cl.dcc.cc5303.CommandLineParser.ParserException;
import cl.dcc.cc5303.client.ClientGameInfo;
import cl.dcc.cc5303.client.PlayerI;
import cl.dcc.cc5303.client.ClientPong;

public class Server extends UnicastRemoteObject implements ServerI, ServerFinderI {
	private static final long serialVersionUID = -8181276888826913071L;
	private static ServerOptions options;
	private static ServerLoadBalancerI loadBalancer;
	private LinkedHashMap<Integer, ServerMatch> matches;
	private int minPlayers;
	private volatile int matchCount;
	private volatile int playerCount;
	private int serverID;
	private MigrationHandler migrationHandler;
	private boolean migrating;
	private int inmigrations;
	
	protected Server(int minPlayers) throws RemoteException {
		super();
		this.minPlayers = minPlayers;
		matches = new LinkedHashMap<Integer, ServerMatch>();
		if (loadBalancer != null) {
			serverID = loadBalancer.connectServer(this);
			System.out.println("Server ID: " + serverID);
		}
	}

	public static void main(String[] args) {
		try {
			options = parseOptions(args);
			
			if (options.loadBalancer){
				loadBalancer = (ServerLoadBalancerI) Naming.lookup("rmi://" + options.balancerUrl + ":1099/serverfinder");
				new Server(options.minPlayers);
			}
			else {
				ServerFinderI server = new Server(options.minPlayers);
				RMISocketFactory.setSocketFactory(new FixedPortRMISocketFactory());
				LocateRegistry.createRegistry(1099);
				Naming.rebind("rmi://localhost:1099/serverfinder", server);
			}			
			System.out.println("Escuchando...");
		} catch (RemoteException | MalformedURLException | NotBoundException e) {
			e.printStackTrace();
		} catch (ParserException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static ServerOptions parseOptions(String[] args) throws ParserException {
		ServerOptions options = new ServerOptions();
		CommandLineParser parser = new CommandLineParser(args);
		parser.addOption("b", "string", "localhost"); 	// Balanceador: localhost por defecto
		parser.addOption("n", "int");
		parser.addOption("m", "int", "1"); //Marcar si un servidor puede recibir migraciones o no
		parser.greaterThanRule("n", 1, "Debe introducir un número válido de jugadores para las partidas (mínimo 2)");
		parser.lessThanRule("n", 5, "Debe introducir un número válido de jugadores para las partidas (máximo 4)");
		parser.parse();
		options.minPlayers = parser.getInt("n", 2); // Minimo de jugadores: 2 por defecto
		options.loadBalancer = parser.containsString("b");
		options.balancerUrl = parser.getString("b", null);
		options.inmigration = parser.containsString("m");
		return options;
	}
	
	private static class ServerOptions {
		public boolean loadBalancer;
		public String balancerUrl;
		public int minPlayers;
		public boolean inmigration;
	}
	
	public int getServerID(){
		return serverID;
	}

	@Override
	public synchronized ClientGameInfo connectPlayer(PlayerI playerI) throws RemoteException {
		ServerMatch serverMatch = getAvailableMatch();
		int playerNum = serverMatch.addPlayer(playerI);
		increasePlayerNum();
		return new ClientGameInfo(serverMatch.getID(), playerNum);
	}
	
	private ServerMatch getAvailableMatch() {
		ServerMatch serverMatch = null;
		for (ServerMatch m : matches.values()) {
			if (m.playersCount() < ClientPong.MAX_PLAYERS && !m.migrating()) {
				serverMatch = m;
				break;
			}
		}
		if (serverMatch == null) {
			serverMatch = new ServerMatch(this, ++matchCount, minPlayers);
			matches.put(serverMatch.getID(), serverMatch);
		}
		return serverMatch;
	}

	@Override
	public synchronized void disconnectPlayer(int matchID, int playerNum) throws RemoteException {
		ServerMatch serverMatch = matches.get(matchID);
		if(serverMatch != null){
			serverMatch.removePlayer(playerNum);
		}
		decreasePlayerNum();
	}

	@Override
	public synchronized GameStateInfo updatePositions(int matchID, int playerNum, int position) throws RemoteException {
		ServerMatch m = matches.get(matchID);
			
		if(m!= null && !m.migrating()){
			return m.updatePositions(playerNum, position);
		}
		else if(m!= null){
			return m.lastPositions();
		}
		else{
			return null;
		}
	}

	@Override
	public ServerI getServer() throws RemoteException {
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
	
	private void migrateMatches(ServerI targetServer) {
		try {
			ServerMatch selectedMatch;
			int migratedMatches = 0;
			int currentMatches  = matches.size();
			
			for (Map.Entry<Integer, ServerMatch> e : matches.entrySet()) {
				selectedMatch = matches.get(e.getKey());
				int targetMatch = targetServer.getMatchForMigration(selectedMatch.startMigration());
				selectedMatch.migratePlayers(targetServer, targetMatch);
				migratedMatches++;
				if(!needToMigrate(migratedMatches, currentMatches)){
					break;
				}
			}
		} catch (RemoteException e) {
			System.out.println("Error en comunicacion al migrar\n");
			e.printStackTrace();
		}
	}

	@Override
	public synchronized int getMatchForMigration(GameStateInfo stateToMigrate) throws RemoteException {
		ServerMatch serverMatch = new ServerMatch(this, ++matchCount, stateToMigrate.numPlayers);
		serverMatch.receiveMigration(stateToMigrate);
		matches.put(serverMatch.getID(), serverMatch);
		inmigrations++;
		return serverMatch.getID();
	}
	
	@Override
	public synchronized void connectPlayer(PlayerI playerI, int matchID, int playerNum) throws RemoteException {
		ServerMatch m = matches.get(matchID);
		m.addPlayer(playerI, playerNum);
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
	public ServerI getServer(int serverID) throws RemoteException {
		return getServer();
	}
	
	@Override
	public ServerInfo heartBeat() throws RemoteException{
		return new ServerInfo(matches.size(), inmigrations, serverID);
	}
	
	private class MigrationHandler extends Thread {
		
		@Override
		public void run() {
			try {
				ServerI migrationServer = loadBalancer.getServerForMigration(serverID);
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

	@Override
	public boolean inMigratable() {
		return options.inmigration;
	}
}
