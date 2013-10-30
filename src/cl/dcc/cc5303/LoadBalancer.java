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
	static final int MAX_LOAD = 12; // MAXIMA CARGA DE JUGADORES

	protected LoadBalancer() throws RemoteException {
		super();
		servers = new HashMap<Integer, IServer>();
		serversLoad = new HashMap<Integer, Integer>();
		serverPriority = new ArrayList<ServerLoad>();
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
	
	@Override
	public synchronized IServer getServer() {
		return serverPriority.get(0).right();
	}
	
	@Override
	public synchronized void reportLoad(int serverID, int load) throws RemoteException {
		serversLoad.put(serverID, load);
		serverPriority = getPriorityList();
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
		ServerLoad bestLoad = serverPriority.get(0);
		
		if(bestLoad.left() <= MAX_LOAD){
			return bestLoad.right();
		}
		else{
			return null;
		}
	}
}
