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
			GameState state = self.server.updatePositions(self.info.matchID, self.info.playerNum, self.getBarPosition(self.info.playerNum));
			self.getState().clientUpdate(state);	
			checkWinners(self.getState());
		} catch (RemoteException e) {
			System.out.println("Server no responde");
			System.exit(1); // FIXME
		}
	}

	@Override
	public void freeWork() throws InterruptedException {
		return;
	}
	
	@Override
	public int workRate(){
		return 50;
	}
	
	protected synchronized void checkWinners(GameState state) {
		self.getState().updateScores(state.scores);
		Pong.scores.setScores(self.getState().scores, self.getState().playing);
		
		if(self.getState().winner == false && state.winner){
			Pong.scores.setWinner(self.getState().scores, state.winnerPlayer, state.playing);
			self.pong.showWinner();
			self.getState().winner = true;
		}
		if(self.getState().winner == true && !state.winner){
			self.getState().winner = false;
			self.pong.game.reMatch();
		}
	}

}