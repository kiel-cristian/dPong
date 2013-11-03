package cl.dcc.cc5303;

public class PongGame extends PongThread{
	public Pong pong;
	public GameState state;
	public boolean winner;
	public boolean onGame;
	public boolean ready;
	
	public PongGame(Pong game){
		this.pong = game;
		this.winner = false;
		this.ready = false;
		this.onGame = false;
		this.state  = new GameState();
	}
	
	@Override
	public void preWork() {
		winner = state.winner;
		ready  = state.playersReady();
		working = ready && !winner;
		
		if(!onGame && ready){
			onGame = true;
			pong.showPauseMessage("");
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
	public void freeWork() {
		if(!ready && !winner){
			onGame = false;
			pong.showPauseMessage("Esperando jugadores ...");
		}
		pong.rePaint();
		pong.handleQuitEvent();
	}
	
	public void reMatch() {
		state.ball = new PongBall();
		state.ball.reset();
		state.lastPlayer = -1;
		pong.scores.reset(state.playing);
	}
}