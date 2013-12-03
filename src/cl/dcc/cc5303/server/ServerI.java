package cl.dcc.cc5303.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import cl.dcc.cc5303.GameStateInfo;
import cl.dcc.cc5303.client.ClientGameInfo;
import cl.dcc.cc5303.client.PlayerI;

public interface ServerI extends Remote {
  public ClientGameInfo connectPlayer(PlayerI playerI) throws RemoteException;
  public void connectPlayer(PlayerI playerI, String targetMatchID, int playerNum) throws RemoteException;
  public GameStateInfo updatePositions(String matchID, int playerNum, int position) throws RemoteException;
  public void disconnectPlayer(String matchID, int playerNum) throws RemoteException;
  
  /**
   * Generates a copy of the original match on target server
   *
   * @param originalMatch Original ServerMatchInfo
   * @return the id of the new match (same as original match) as string
   * @throws RemoteException
   */
  public String getMatchForMigration(ServerMatchMigrationInfo originalMatch) throws RemoteException;
  
  /**
   * Returns ServerInfo. Used for load balancer to check if a server is still alive.
 * @return 
   */
  public ServerInfo heartBeat() throws RemoteException;
  
  /**
   * Returns current server ID (for migration purposes)
   * 
   * @return an integer with ID value
   */
  public int getServerID() throws RemoteException;
  
  public void togglePause(String matchID) throws RemoteException;
}
