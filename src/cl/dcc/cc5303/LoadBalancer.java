package cl.dcc.cc5303;

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

import cl.dcc.cc5303.Utils.Pair;

public class LoadBalancer extends UnicastRemoteObject implements ILoadBalancer, ServerFinder {
	private static final long serialVersionUID = 8410514211761367368L;
	private HashMap<Integer, IServer> servers;		// (ID, Server)
	private HashMap<Integer, Integer> serversLoad;	// (ID, Load)
	private List<ServerLoad> serverPriority;
	private int lastServerID;
	private ServerLoad lastTargetServer;
	static final int MAX_LOAD = Pong.MAX_PLAYERS*3; // MAXIMA CARGA DE JUGADORES

	protected LoadBalancer() throws RemoteException {
		super();
		servers = new HashMap<Integer, IServer>();
		serversLoad = new HashMap<Integer, Integer>();
		serverPriority = new ArrayList<ServerLoad>();
		lastTargetServer = null;
	}
	
	public static void main(String[] args) {
		try {
			RMISocketFactory.setSocketFactory(new FixedPortRMISocketFactory());
			ILoadBalancer balancer = new LoadBalancer();
			LocateRegistry.createRegistry(1099);
			Naming.rebind("rmi://localhost:1099/serverfinder", balancer);
			
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
	public synchronized int connectServer(IServer server) throws RemoteException {
		servers.put(++lastServerID, server);
		serversLoad.put(lastServerID, 0);
		serverPriority = getPriorityList();
		try {
			System.out.println("Servidor conectado: " + getClientHost());
		} catch (ServerNotActiveException e) {
			e.printStackTrace();
		}
		return lastServerID;
	}
	
	private List<ServerLoad> getPriorityList() {
		List<ServerLoad> p = new ArrayList<ServerLoad>();
		for (Map.Entry<Integer, Integer> e : serversLoad.entrySet()) {
			p.add(new ServerLoad(e.getValue(), servers.get(e.getKey())));
		}
		Collections.sort(p);
		return p;
	}
	
	private ServerLoad getBestCandidateServer(){
		ServerLoad bestServer = null;
		for(ServerLoad sl : serverPriority){
			// Busco un server que tenga una carga inferior al 70% y con al menos una partida no llena
			if(sl.left() < MAX_LOAD*0.7 && sl.left() % Pong.MAX_PLAYERS >= 0){
				bestServer = sl;
				break;
			}
		}
		if(bestServer == null){
			bestServer = serverPriority.get(0);
		}
		return bestServer;
	}
	
	@Override
	public synchronized IServer getServer() {
		if(!(lastTargetServer != null && lastTargetServer.left() % Pong.MAX_PLAYERS > 0)){
			// Es necesario obtener un nuevo candidato para conectar
			lastTargetServer = getBestCandidateServer();
		}
		
		// Se agrega un jugador al servidor en cuestion escogido
		int load = lastTargetServer.left();
		lastTargetServer = new ServerLoad(++load, lastTargetServer.right());
		
		return lastTargetServer.right();
	}
	
	@Override
	public synchronized void reportLoad(int serverID, int load) throws RemoteException {
		int lastLoad = serversLoad.get(serverID);
		
		// Si la carga del servidor es mayor o igual al 70%
		if(load > lastLoad && load >= MAX_LOAD*0.7){
			// Migrar matches
			IServer server          = servers.get(serverID);
			IServer migrationServer = getServerForMigration(serverID);
			
			// Si es que hay donde migrar
			if(migrationServer != null){
				// TODO: Algo asi, como el metodo para migrar varios matches del server, en vez de uno solo desde aca, donde falta info ahora
				server.migrateMatches(migrationServer);
			}
		}
		else{
			serversLoad.put(serverID, load);
			serverPriority = getPriorityList();	
		}
	}
	
	private class ServerLoad extends Pair<Integer, IServer> implements Comparable<ServerLoad> {

		public ServerLoad(Integer load, IServer server) {
			super(load, server);
		}

		@Override
		public int compareTo(ServerLoad s) {
			return this.left().compareTo(s.left());
		}
	}

	@Override
	public IServer getServerForMigration(int sourceServerID)
			throws RemoteException {
		
		IServer sourceServer = servers.get(sourceServerID);
		ServerLoad bestLoad  = null;
		
		for(ServerLoad s: serverPriority){
			if(s.right().equals(sourceServer)){
				continue;
			}
			else{
				bestLoad = s;
				break;
			}
		}
		
		if(bestLoad.left() < MAX_LOAD){
			return bestLoad.right();
		}
		else{
			return null;
		}
	}
}
