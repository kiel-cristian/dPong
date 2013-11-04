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
			working = temporalState.running;
			if(!working && onGame){
				System.out.println("Client update pausado por falta de jugadores");
				onGame = false;
				work();
			}
			else if(working && !onGame){
				onGame = true;
			}
		} catch (RemoteException e) {
			System.out.println("Server no responde");
			System.exit(1); // FIXME
		}
	}

	@Override
	public void work() throws InterruptedException {
		checkWinners(temporalState);
		synchronized(self.state()){
			self.state().fullUpdate(temporalState);
		}
	}

	@Override
	public void postWork() throws InterruptedException {
		return;
	}
	
	protected void checkWinners(GameState state) {
		synchronized(self.state()){
			if(state.winner && !self.pong.scores.isAWinner()){
				self.pong.scores.setWinner(state.scores, state.winnerPlayer, state.playing);
				self.pong.showWinner();
			}
			else if(!state.winner && self.pong.scores.isAWinner()){
				self.pong.game.reMatch();
			}
			else{
				Pong.scores.setScores(self.state().scores, state.playing);
			}
		}
	}

	@Override
	public void pauseWork() throws InterruptedException {
		Thread.sleep(PongThread.UPDATE_RATE/ 60);
	}

}