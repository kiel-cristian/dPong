package cl.dcc.cc5303;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IServer extends Remote {
  public GameInfo connectPlayer(Player player) throws RemoteException;
  public GameState updatePositions(int matchID, int playerNum, int position) throws RemoteException;
  public void disconnectPlayer(int matchID, int playerNum) throws RemoteException;
}
