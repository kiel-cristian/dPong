package cl.dcc.cc5303;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedHashMap;
import java.util.Map;

public class Server extends UnicastRemoteObject implements IServer, ServerFinder {
	private static final long serialVersionUID = -8181276888826913071L;
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
			int players = 2; // Numero de jugadores por default

						
			if (args.length > 0){
				players = Integer.parseInt(args[0]);
				if(players < 2){
					System.out.println("Debe introducir un número válido de jugadores para las partidas (mínimo 2)");
					return;
				}
			}
			if (args.length > 1){
				loadBalancer = (ILoadBalancer) Naming.lookup("rmi://" + args[1] + ":1099/serverfinder");
				new Server(players);
			}
			else {
				
				loadBalancer = (ILoadBalancer) Naming.lookup("rmi://localhost:1099/serverfinder");
				new Server(players);
			}			
			System.out.println("Escuchando...");
		} catch (RemoteException | MalformedURLException | NotBoundException e) {
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
			if (m.playersCount() < PongClient.MAX_PLAYERS && !m.migrating()) {
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
		Match match = matches.get(matchID);
		if(match != null){
			match.removePlayer(playerNum);
		}
		decreasePlayerNum();
	}

	@Override
	public synchronized GameState updatePositions(int matchID, int playerNum, int position) throws RemoteException {
		Match m = matches.get(matchID);
			
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
			System.out.println("Error en comunicacion al migrar\n");
			e.printStackTrace();
		}
	}

	@Override
	public synchronized int getMatchForMigration(GameState stateToMigrate) throws RemoteException {
		Match match = new Match(this, ++matchCount, stateToMigrate.numPlayers);
		match.receiveMigration(stateToMigrate);
		matches.put(match.getID(), match);
		return match.getID();
	}
	
	@Override
	public synchronized void connectPlayer(Player player, int matchID, int playerNum) throws RemoteException {
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
