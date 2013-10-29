package cl.dcc.cc5303;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ILoadBalancer extends Remote {
	/**
	 * Connects a server to the load balancer
	 * 
	 * @param server the remote interface of the server to connect
	 * @return the ID of the server in the load balancer
	 * @throws RemoteException
	 */
	public int connectServer(IServer server) throws RemoteException;
	
	public void reportLoad(int serverID, int load) throws RemoteException;
}
