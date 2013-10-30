package cl.dcc.cc5303;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IServer extends Remote {
  public GameInfo connectPlayer(Player player) throws RemoteException;
  public void connectPlayer(Player player, int matchID, int playerNum) throws RemoteException;
  public GameState updatePositions(int matchID, int playerNum, int position) throws RemoteException;
  public void disconnectPlayer(int matchID, int playerNum) throws RemoteException;
  
  /**
   * Creates a match in this server to hold a migrating match represented by its game state
   * 
   * @param stateToMigrate the game state of the migrating match
   * @return the ID of the match
   * @throws RemoteException
   */
  public int getMatchForMigration(GameState stateToMigrate) throws RemoteException;
  
  /**
   * Initiates migration for the current server to the migrationServer
   * 
   * @param migrationServer The server for the migration
   */
  public void migrateMatches(IServer migrationServer) throws RemoteException;
  
  /**
   * Simple returns. Used for load balancer to check if a server is still alive.
   */
  public void heartBeat() throws RemoteException;
}
