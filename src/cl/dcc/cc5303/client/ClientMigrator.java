package cl.dcc.cc5303.client;

import java.rmi.RemoteException;

import cl.dcc.cc5303.PongThread;
import cl.dcc.cc5303.server.ServerI;

public class ClientMigrator extends PongThread{
	private Client self;
	private final ServerI targetServer;
	private final String targetMatchID;

	public ClientMigrator(Client client, ServerI targetServer, String targetMatchID) {
		this.self = client;
		this.targetServer = targetServer;
		this.targetMatchID = targetMatchID;
	}

	@Override
	public void preWork() throws InterruptedException {
		if(self.pong != null){
			synchronized(self.pong){
				self.pong.serverUpdate.startMigration();
				self.pong.serverUpdate.pause();
				self.pong.game.pause();
			}
		}
	}

	@Override
	public void work() throws InterruptedException {
		try {
			ServerI oldServer = self.server;
			String oldMatch = self.info.matchID;

			synchronized(self.server){
				self.server = targetServer;
			}

			self.info.matchID = targetMatchID;
			oldServer.disconnectPlayer(oldMatch, self.info.playerNum);
			targetServer.connectPlayerFromMigration(self, targetMatchID, self.info.playerNum);
			
			if(self.pong != null){
				synchronized(self.pong){
					self.pong.serverUpdate.stopMigration();
					self.pong.serverUpdate.unPause();
					self.pong.game.unPause();
				}
			}

		} catch (RemoteException e) {
			System.out.println("Error en servidor al migrar cliente");
			e.printStackTrace();
			System.exit(1); // FIXME;
		}
		
	}

	@Override
	public void postWork() throws InterruptedException {
		end();
	}

	@Override
	public void pauseWork() throws InterruptedException {
		return;
	}

}
