package cl.dcc.cc5303;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Player extends Remote {
	public void migrate(IServer server, int matchID, int player) throws RemoteException;
}
