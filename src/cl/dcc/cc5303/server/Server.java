package cl.dcc.cc5303.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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

	private LinkedHashMap<String, ServerMatch> matches;
	private LinkedHashMap<String, Boolean> inmigratedMatches;
	private List<ServerMatchLoad> matchPriority;

	private int minPlayers;
	private int matchCount;
	private int playerCount;
	private int serverID;
	private MigrationHandler migrationHandler;
	private boolean migrating;
	private int inmigrations;
	
	protected Server(int minPlayers) throws RemoteException {
		super();
		this.minPlayers        = minPlayers;
		this.matches           = new LinkedHashMap<String, ServerMatch>();
		this.inmigratedMatches = new LinkedHashMap<String, Boolean>();
		this.matchPriority     = new ArrayList<ServerMatchLoad>();

		if (loadBalancer != null) {
			this.serverID = loadBalancer.connectServer(this);
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
		parser.addStringOption("b", "localhost"); 	// Balanceador: localhost por defecto
		parser.addIntegerOption("n");
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
	
	private ArrayList<ServerMatchLoad> getMatchPriorityList(){
		ArrayList<ServerMatchLoad> matchesPriority = new ArrayList<ServerMatchLoad>();
		for(ServerMatch sm : matches.values()){
			matchesPriority.add(new ServerMatchLoad(sm.playersCount(), sm.getID()));
		}
		Collections.sort(matchesPriority);
		return matchesPriority;
	}
	
	private ServerMatch getAvailableMatch() {
		matchPriority = getMatchPriorityList();
		
		ServerMatchLoad matchLoad = null;
		ServerMatch serverMatch   = null;
		int i;
		
		// Busca el match con mas jugadores del servidor
		for (i = 0; i < matchPriority.size(); i++) {
			
			if ((matchLoad = matchPriority.get(i)).left() < ClientPong.MAX_PLAYERS && !matches.get(matchLoad.right()).migrating()) {
				serverMatch = matches.get(matchLoad.right());
				break;
			}
		}

		// Si no hay , crea uno nuevo
		if (serverMatch == null) {
			String matchID = generateNewMatchKey();
			serverMatch = new ServerMatch(this, matchID, minPlayers);
			matches.put(serverMatch.getID(), serverMatch);
		}
		return serverMatch;
	}
	
	private String generateNewMatchKey(){
		return "sID:" + serverID + " mC:" + (++matchCount);
	}

	@Override
	public synchronized void disconnectPlayer(String matchID, int playerNum) throws RemoteException {
		ServerMatch serverMatch = null;
		synchronized(matches){
			serverMatch = matches.get(matchID);
		}
		if(serverMatch != null){
			int players = serverMatch.removePlayer(playerNum);
			if (players == 0){
				removeMatch(serverMatch.getID());
			}
			decreasePlayerNum();
		}
	}
	
	public synchronized void disconnectPlayerByTimeout(String matchID, int playerCount) {
		ServerMatch serverMatch = null;
		synchronized(matches){
			serverMatch = matches.get(matchID);
		}
		if(serverMatch != null && playerCount == 0){
			removeMatch(serverMatch.getID());
		} else if(serverMatch != null){
			decreasePlayerNum();
		}
	}

	@Override
	public synchronized GameStateInfo updatePositions(String matchID, int playerNum, int position) throws RemoteException {
		ServerMatch m = matches.get(matchID);
			
		if(m!= null && !m.migrating()){
			return m.updatePositions(playerNum, position);
		}
		else {
			return m.lastPositions();
		}
	}

	@Override
	public ServerI getServer() throws RemoteException {
		return this;
	}
	
	private void increasePlayerNum() {
		playerCount++;
		reportLoad(true);
	}
	
	private void decreasePlayerNum() {
		playerCount--;
		reportLoad(false);
	}
	
	private void reportLoad(boolean increasing) {
		if (loadBalancer != null) {
			try {
				boolean acceptableLoad = loadBalancer.reportLoad(serverID, playerCount);
				if (!acceptableLoad && increasing) {
					migrateMatches();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void migrateMatches() throws RemoteException {
		if (!migrating) {
			migrating = true;
			migrationHandler = new MigrationHandler();
			migrationHandler.start();
		}
	}
	
	// Fijado en que el server migre 1/2 de sus matches
	private boolean needToMigrate(int migratedMatchesCount){
		return migratedMatchesCount < matchCount/2;
	}
	
	private void migrateMatches(ServerI targetServer) {
		try {
			ServerMatch selectedMatch;
			int migratedMatches = 0;
			
			synchronized(matches){
				for (Map.Entry<String, ServerMatch> e : matches.entrySet()) {
					selectedMatch = matches.get(e.getKey());
					
					// Si el match acaba de ser migrado, no es escogible en la primera ronda de migracion
					if (inmigratedMatches.remove(selectedMatch.getID()) != null) {
						continue;
					}
					
					String targetMatch = targetServer.getMatchForMigration(selectedMatch.startMigration());
					selectedMatch.migratePlayers(targetServer, targetMatch);
					migratedMatches++;
	
					if(!needToMigrate(migratedMatches)){
						break;
					}
				}
			}
		} catch (RemoteException e) {
			System.out.println("Error en comunicacion al migrar\n");
			e.printStackTrace();
		}
	}

	@Override
	public synchronized String getMatchForMigration(ServerMatchMigrationInfo originalMatch) throws RemoteException {
		GameStateInfo stateToMigrate = originalMatch.state;
		++matchCount;
		++inmigrations;
		ServerMatch serverMatch = new ServerMatch(this, originalMatch.matchID, stateToMigrate.numPlayers);
		serverMatch.receiveMigration(stateToMigrate);
		matches.put(serverMatch.getID(), serverMatch);
		inmigratedMatches.put(serverMatch.getID(), true);
		return serverMatch.getID();
	}
	
	@Override
	public synchronized void connectPlayer(PlayerI playerI, String matchID, int playerNum) throws RemoteException {
		ServerMatch m = matches.get(matchID);
		m.addPlayer(playerI, playerNum);
		increasePlayerNum();
		if (m.migrationReady()) {
			m.stopMigration();
		}
	}

	public void removeMatch(String matchID) {
		matches.remove(matchID);
		inmigratedMatches.remove(matchID);
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
}
