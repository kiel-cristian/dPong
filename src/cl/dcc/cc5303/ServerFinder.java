package cl.dcc.cc5303;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerFinder extends Remote {
	public ServerI getServer() throws RemoteException;
	public ServerI getServer(int serverID) throws RemoteException;
}
