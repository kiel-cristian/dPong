package cl.dcc.cc5303.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import cl.dcc.cc5303.client.ClientGameInfo;
import cl.dcc.cc5303.client.PlayerI;

public interface ServerFinderI extends Remote {
	public ServerI getServer() throws RemoteException;
	public ServerI getServer(int serverID) throws RemoteException;
	public ServerI connectToServerAndRestoreClient(PlayerI player, ClientGameInfo info) throws RemoteException;
}
