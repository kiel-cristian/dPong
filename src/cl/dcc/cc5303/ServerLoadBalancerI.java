package cl.dcc.cc5303;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerLoadBalancerI extends Remote {
    /**
     * Connects a server to the load balancer
     * 
     * @param server the remote interface of the server to connect
     * @return the ID of the server in the load balancer
     * @throws RemoteException
     */
    public int connectServer(ServerI server) throws RemoteException;
    
    /**
     * Reports server's current load to the load balancer
     * 
     * @param serverID reporting server's ID
     * @param load server's load
     * @return true if load is acceptable, false if server needs to migrate
     * @throws RemoteException
     */
    public boolean reportLoad(int serverID, int load) throws RemoteException;
    
    /**
     * Load balancer gets a server that can receive migrations
     * 
     * @param sourceServerID the ID of the server that requests the migration
     * @return the target server of the migration
     * @throws RemoteException
     */
    public ServerI getServerForMigration(int sourceServerID) throws RemoteException;
    
    /**
     * Runs heartBeat service for load balancer server
     */
    public void initHeartBeat() throws RemoteException;
}