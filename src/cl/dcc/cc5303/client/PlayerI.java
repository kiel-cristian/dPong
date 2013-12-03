package cl.dcc.cc5303.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

import cl.dcc.cc5303.server.ServerI;

public interface PlayerI extends Remote {
	public void migrate(ServerI server, String targetMatch) throws RemoteException;
}
