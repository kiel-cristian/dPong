package cl.dcc.cc5303.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerFinderI extends Remote {
	public ServerI getServer() throws RemoteException;
	public ServerI getServer(int serverID) throws RemoteException;
}
