package cl.dcc.cc5303.server;

import cl.dcc.cc5303.Pong;
import cl.dcc.cc5303.PongThread;
import cl.dcc.cc5303.client.ClientGameState;

public class ServerGameSaver extends PongThread{
	private ServerMatch self;
	private String[] files;
	private String[] data;
	
	public ServerGameSaver(ServerMatch match){
		this.self = match;
	}

	@Override
	public void preWork() throws InterruptedException {
		String[] files = new String[Pong.MAX_PLAYERS];
		ClientGameState[] data  = new ClientGameState[Pong.MAX_PLAYERS];
		
		for(int i = 0; i < Pong.MAX_PLAYERS; i++){
			if(self.game.isPlaying(i)){
				files[i] = self.matchID + self.game.state.getPlayerID(i);
				data[i]  = self.game.state.packClientInfo(i);
			}
		}
	}

	@Override
	public void work() throws InterruptedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postWork() throws InterruptedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pauseWork() throws InterruptedException {
		Thread.sleep(PongThread.UPDATE_RATE*2);
	}

}
