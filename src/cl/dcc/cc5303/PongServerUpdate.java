package cl.dcc.cc5303;

import java.rmi.RemoteException;

public class PongServerUpdate extends PongThread{
	private Client self;
	private GameState temporalState;
	public boolean onGame;
	
	public PongServerUpdate(Client client){
		self = client;
		onGame = true;
	}

	@Override
	public void preWork() throws InterruptedException {
		try {
			temporalState = self.server.updatePositions(self.info.matchID, self.info.playerNum, self.getBarPosition());
			working = !temporalState.winner && temporalState.running;
			if(!working && onGame){
				if(!temporalState.winner){
					System.out.println("Client update pausado por falta de jugadores");
				}
				else{
					((ScoreBoardGUI) self.pong.scores).setWinner(temporalState.scores, temporalState.winnerPlayer, temporalState.playing);
					self.pong.showWinner();
				}
				onGame = false;
				work();
			}
			else if(working && !onGame){
				self.pong.game.reMatch();
				onGame = true;
			}
		} catch (RemoteException e) {
			System.out.println("Server no responde");
			System.exit(1); // FIXME
		}
	}

	@Override
	public void work() throws InterruptedException {
		synchronized(self.state()){
			self.state().fullUpdate(temporalState);
			self.pong.scores.setScores(temporalState.scores, temporalState.playing);
		}
		
	}

	@Override
	public void postWork() throws InterruptedException {
		return;
	}

	@Override
	public void pauseWork() throws InterruptedException {
		Thread.sleep(PongThread.UPDATE_RATE/ 60);
	}

}