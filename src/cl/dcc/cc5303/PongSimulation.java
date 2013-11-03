package cl.dcc.cc5303;

public class PongSimulation extends PongThread {
	private static final long INACTIVITY_TIMEOUT = 3000;
	private Match match;
	public GameState state;
	
	public PongSimulation(Match match){
		this.match = match;
		this.state = new GameState();
	}
	
	public void setPlayers(int numPlayers){
		this.state.numPlayers = numPlayers;
	}

	@Override
	public void preWork() throws InterruptedException {
		if (match.migration.emigrating) {
			running = false;
			return;
		}
	}

	@Override
	public void work() throws InterruptedException {
		Pong.doGameIteration(state);
	}

	@Override
	public void postWork() throws InterruptedException {
		checkForWinnerServer();
		checkPlayersActivity();
	}
	
	@Override
	public void pauseWork() throws InterruptedException{
		if(state.winner){
			Thread.sleep(600000 /50);
			match.resetGame();
		}
		else{
			Thread.sleep(PongThread.UPDATE_RATE/ 60);
		}
	}
	
	private void checkForWinnerServer(){
		if(match.score.isAWinner()){
			match.historical.addWinner(state.winnerPlayer);
			state.setMatchWinner(match.score.getWinner(), match.historical.getScores());
		}
	}
	
	private synchronized void checkPlayersActivity() {
		for (int i=0; i < match.lastActivity.length; i++) {
			if (state.isPlaying(i) && (System.currentTimeMillis() - match.lastActivity[i] > INACTIVITY_TIMEOUT)) {
				match.removePlayer(i);
			}
		}
	}
}