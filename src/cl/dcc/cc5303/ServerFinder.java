package cl.dcc.cc5303;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerFinder extends Remote {
	public IServer getServer() throws RemoteException;
	public IServer getServer(int serverID) throws RemoteException;
}
