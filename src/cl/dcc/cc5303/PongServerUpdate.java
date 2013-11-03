package cl.dcc.cc5303;

import java.rmi.RemoteException;

public class PongServerUpdate extends PongThread{
	private Client self;
	
	public PongServerUpdate(Client client){
		self = client;
	}

	@Override
	public void preWork() throws InterruptedException {
		return;
	}

	@Override
	public void work() throws InterruptedException {
		try {
			GameState state = self.server.updatePositions(self.info.matchID, self.info.playerNum, self.getBarPosition());
			checkWinners(state);
			self.getState().fullUpdate(state);
			
		} catch (RemoteException e) {
			System.out.println("Server no responde");
			System.exit(1); // FIXME
		}
	}

	@Override
	public void postWork() throws InterruptedException {
		return;
	}
	
	protected synchronized void checkWinners(GameState state) {
		if(state.winner && !self.pong.scores.isAWinner()){
			self.pong.scores.setWinner(state.scores, state.winnerPlayer, state.playing);
			self.pong.showWinner();
		}
		else if(!state.winner && self.pong.scores.isAWinner()){
			self.pong.game.reMatch();
		}
		else{
			Pong.scores.setScores(self.getState().scores, state.playing);
		}
	}

	@Override
	public void pauseWork() throws InterruptedException {
		Thread.sleep(PongThread.UPDATE_RATE/ 60);
	}

}