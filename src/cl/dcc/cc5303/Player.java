package cl.dcc.cc5303;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Player extends Remote {
	public void migrate(ServerI server, int matchID) throws RemoteException;
}
