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
	
	public int workRate(){
		return 50;
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
		state.lastPlayer = Pong.doGameIteration(state);	
	}

	@Override
	public void freeWork() throws InterruptedException {
		checkForWinnerServer();
		checkPlayersActivity();
		
		if(state.winner){
			Thread.sleep(600000 / workRate());
			match.resetGame();
		}
		else{
			Thread.sleep(1000 / workRate()); // milliseconds
		}
	}
	
	private void checkForWinnerServer(){
		state.winnerPlayer = match.score.getWinner();
		if(match.score.getWinner() >= 0){
			state.winner = true;
			match.historical.addWinner(state.winnerPlayer);
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