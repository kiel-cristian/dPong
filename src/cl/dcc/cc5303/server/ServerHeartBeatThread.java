package cl.dcc.cc5303.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cl.dcc.cc5303.PongThread;

public class ServerHeartBeatThread extends PongThread {
	private ServerLoadBalancer balancer;
	private List<Integer> disconnectedServers; 
	
	public ServerHeartBeatThread(ServerLoadBalancer balancer){
		this.balancer = balancer;
	}

	public void run() {
		System.out.println("Running Heart Beat");
		super.run();
	}

	@Override
	public void preWork() throws InterruptedException {
		disconnectedServers = new ArrayList<Integer>();
	}

	@Override
	public void work() throws InterruptedException {
		for(Map.Entry<Integer, ServerI> e : balancer.getServers().entrySet()) {
			ServerI server = e.getValue();
			try {
				ServerInfo info = server.heartBeat();
				balancer.updateServerInfo(info);
				
			} catch (RemoteException e1) {
				disconnectedServers.add(e.getKey());
			}
		}
		
		synchronized(this.balancer){
			for(int serverID : disconnectedServers){
				System.out.println("Disconnecting server: " + serverID);
				balancer.removeServer(serverID);
			}
			balancer.updatePriorityList();
		}
	}

	@Override
	public void postWork() throws InterruptedException {
		return;
	}

	@Override
	public void pauseWork() throws InterruptedException {
		Thread.sleep(2000);
	}
}