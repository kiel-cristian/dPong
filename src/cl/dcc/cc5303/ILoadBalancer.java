package cl.dcc.cc5303;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ILoadBalancer extends Remote {
	public void connectServer(IServer server) throws RemoteException;
}
