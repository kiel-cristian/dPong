package cl.dcc.cc5303;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IServer extends Remote {
	public int connectPlayer(Player player) throws RemoteException;
	public boolean playersReady() throws RemoteException;
	public GameState updatePositions(int playerNum, int position) throws RemoteException;
}
