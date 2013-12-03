package cl.dcc.cc5303.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.dcc.cc5303.FixedPortRMISocketFactory;
import cl.dcc.cc5303.Utils;
import cl.dcc.cc5303.client.ClientGameInfo;
import cl.dcc.cc5303.client.ClientPong;
import cl.dcc.cc5303.client.PlayerI;

public class ServerLoadBalancer extends UnicastRemoteObject implements ServerLoadBalancerI, ServerFinderI {
	private static final long serialVersionUID = 8410514211761367368L;
	protected HashMap<Integer, ServerI> servers;		// (ID, Server)
	private HashMap<Integer, Integer> serversLoad;	// (ID, Load)
	private HashMap<Integer, Integer> serverMatches;
	private HashMap<Integer, Integer> serverMinPlayers;
	
	private List<ServerLoad> serverPriority;
	private int lastServerID;
	static final int MAX_LOAD = ClientPong.MAX_PLAYERS*2; // MAXIMA CARGA DE JUGADORES
	private ServerHeartBeatThread heartBeat;

	protected ServerLoadBalancer() throws RemoteException {
		super();
		servers          = new HashMap<Integer, ServerI>();
		serversLoad      = new HashMap<Integer, Integer>();
		serverMatches	 = new HashMap<Integer, Integer>();
		serverMinPlayers	 = new HashMap<Integer, Integer>();
		serverPriority   = new ArrayList<ServerLoad>();
		heartBeat        = new ServerHeartBeatThread(this);
	}
	
	public static void main(String[] args) {
		try {
			RMISocketFactory.setSocketFactory(new FixedPortRMISocketFactory());
			ServerLoadBalancerI balancer = new ServerLoadBalancer();
			LocateRegistry.createRegistry(1099);
			Naming.rebind("rmi://localhost:1099/serverfinder", balancer);
			balancer.initHeartBeat();
			
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
	
	public HashMap<Integer, ServerI> getServers() {
		return servers;
	}

	public HashMap<Integer, Integer> getServersLoad() {
		return serversLoad;
	}

	public List<ServerLoad> getServerPriority() {
		return serverPriority;
	}
	
	public void updateServerInfo(ServerInfo info) {
		synchronized(serverMatches){
			serverMatches.put(info.serverID, info.matches);
		}
	}
	
	public synchronized void updatePriorityList(){
		serverPriority = getPriorityList();
	}
	
	public void addServer(ServerI server, int minPlayers) throws RemoteException{
		servers.put(++lastServerID, server);
		serversLoad.put(lastServerID, 0);
		serverMinPlayers.put(lastServerID, minPlayers);
	}
	public void removeServer(int serverID){
		servers.remove(serverID);
		serversLoad.remove(serverID);
		serverMatches.remove(serverID);
		serverMinPlayers.remove(serverID);
	}

	@Override
	public void initHeartBeat(){
		heartBeat.run();
	}

	@Override
	public synchronized int connectServer(ServerI server, int minPlayers) throws RemoteException {
		addServer(server, minPlayers);
		updatePriorityList();

		try {
			System.out.println("Servidor conectado: " + getClientHost());
		} catch (ServerNotActiveException e) {
			e.printStackTrace();
		}
		return lastServerID;
	}
	
	private List<ServerLoad> getPriorityList() {
		List<ServerLoad> p = new ArrayList<ServerLoad>();
		for (Map.Entry<Integer, Integer> e : getServersLoad().entrySet()) {
			p.add(new ServerLoad(e.getValue(), e.getKey()));
		}
		Collections.sort(p);
		return p;
	}
	
	private ServerLoad getBestCandidateServer(){
		for(ServerLoad sl : serverPriority){
			// Busco un server que tenga una carga inferior al 70% y con al menos una partida no llena
			if( sl.left() < MAX_LOAD*0.7 && serverMatches.get(sl.right()) > 0 && sl.left() % serverMinPlayers.get(sl.right()) > 0){
				return sl;
			}
		}
		return getBestPriorityServer();
	}
	
	private ServerLoad getBestPriorityServer(){
		updatePriorityList();
		for(ServerLoad sl : serverPriority){
			return sl;
		}
		return null;
	}
	
	@Override
	public synchronized ServerI getServer() throws RemoteException{
		return servers.get(getBestCandidateServer().right());
	}
	
	@Override
	public synchronized ServerI getServer(int serverID) throws RemoteException {
		return servers.get(serverID);
	}
	
	@Override
	public boolean reportLoad(int serverID, int load) throws RemoteException {
		synchronized (this) {
			int lastLoad = getServersLoad().get(serverID);
			serversLoad.put(serverID, load);
			serverPriority = getPriorityList();
			
			// Si la carga del servidor es mayor o igual al 70%
			if (load > lastLoad && load >= MAX_LOAD*0.7) {
				return false;
			}
			else {
				return true;
			}
		}
	}

	@Override
	public ServerI getServerForMigration(int sourceServerID) throws RemoteException {
		synchronized(this.servers) {
			for(ServerLoad s: getServerPriority()) {
				if (s.right() != sourceServerID &&  s.left() < MAX_LOAD) {
					return servers.get(s.right());
				}
			}
			return null;
		}
	}

	@Override
	public ServerI connectToServerAndRestoreClient(PlayerI player, ClientGameInfo info) throws RemoteException {
		ServerI server = null;
		synchronized(this.servers) {
			for(ServerI s : servers.values()){
				if(s.hasMatch(info.matchID)) {
					server = s.connectToServerAndRestoreClient(player, info);
					break;
				}
			}
		}
		if (server != null){
			return server;
		} else {
			return getServer();
		}
	}
}
