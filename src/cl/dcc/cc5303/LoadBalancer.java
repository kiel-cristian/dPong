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
import java.util.List;

import cl.dcc.cc5303.Utils.MutablePair;

public class LoadBalancer extends UnicastRemoteObject implements ILoadBalancer, ServerFinder {
	private static final long serialVersionUID = 8410514211761367368L;
	private List<ServerEntry> servers;

	protected LoadBalancer() throws RemoteException {
		super();
		servers = new ArrayList<ServerEntry>();
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
	public synchronized void connectServer(IServer server) throws RemoteException {
		servers.add(new ServerEntry(0, server));
		sortServers();
		try {
			System.out.println("Servidor conectado: " + getClientHost());
		} catch (ServerNotActiveException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public synchronized IServer getServer() {
		return servers.get(0).right;
	}
	
	private void sortServers() {
		Collections.sort(servers);
	}
	
	private class ServerEntry extends MutablePair<Integer, IServer> implements Comparable<ServerEntry> {

		public ServerEntry(Integer left, IServer right) {
			super(left, right);
		}

		@Override
		public int compareTo(ServerEntry s) {
			return this.left.compareTo(s.left);
		}
	}
}
