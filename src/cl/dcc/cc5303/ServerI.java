package cl.dcc.cc5303;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerI extends Remote {
  public GameInfo connectPlayer(Player player) throws RemoteException;
  public void connectPlayer(Player player, int matchID, int playerNum) throws RemoteException;
  public GameStateInfo updatePositions(int matchID, int playerNum, int position) throws RemoteException;
  public void disconnectPlayer(int matchID, int playerNum) throws RemoteException;
  
  /**
   * Creates a match in this server to hold a migrating match represented by its game state
   * 
   * @param stateToMigrate the game state of the migrating match
   * @return the ID of the match
   * @throws RemoteException
   */
  public int getMatchForMigration(GameStateInfo stateToMigrate) throws RemoteException;
  
  /**
   * Returns ServerInfo. Used for load balancer to check if a server is still alive.
 * @return 
   */
  public ServerInfo heartBeat() throws RemoteException;
  
  /**
   * Returns true when a Server allows incoming migrations
   * 
   * @return true when can migrate to this server
   */
  public boolean inMigratable() throws RemoteException;
  
  /**
   * Returns current server ID (for migration purposes)
   * 
   * @return an integer with ID value
   */
  public int getServerID() throws RemoteException;
}
