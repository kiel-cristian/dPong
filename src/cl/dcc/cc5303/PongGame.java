package cl.dcc.cc5303;

public class PongGame extends PongThread{
	public Pong pong;
	private GameState state;
	public boolean onGame;
	
	public PongGame(Pong game){
		this.pong = game;
		this.onGame = true;
		this.state  = new GameState();
	}
	
	@Override
	public void preWork() {
		synchronized(state){
			working = !state.winner && state.running;
			if(onGame && !working){
				System.out.println("Client game pausado por falta de jugadores");
				onGame = false;
				pong.showPauseMessage("Esperando jugadores ...");
			}
		}
	}

	@Override
	public void work() {
		int playerNum = pong.client.getPlayerNum();
		pong.handleKeyEvents(playerNum);
		Pong.doGameIteration(state);
		pong.handlePlayerBars();
	}

	@Override
	public void postWork() {
		synchronized(state){
			if(working && !onGame){
				onGame = true;
				pong.showPauseMessage("");
			}
		}
		pong.rePaint();
		pong.handleQuitEvent();
	}

	@Override
	public void pauseWork() throws InterruptedException {
		Thread.sleep(PongThread.UPDATE_RATE/ 60);
	}

	public void reMatch() {
		synchronized(state){
			state.resetGame();
		}
		pong.scores.reset(state.playing);
	}

	public void enablePlayer(int playerNum) {
		synchronized(state){
			state.enablePlayer(playerNum);
		}
	}

	public GameState state() {
		return state;
	}
}