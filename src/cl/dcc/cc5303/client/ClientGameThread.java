package cl.dcc.cc5303.client;

import cl.dcc.cc5303.GameState;
import cl.dcc.cc5303.PongThread;

public class ClientGameThread extends PongThread{
	public ClientPong pong;
	private GameState state;
	public boolean onGame;
	
	public ClientGameThread(ClientPong game){
		this.pong = game;
		this.onGame = true;
		this.state  = new GameState();
	}
	
	@Override
	public void preWork() {
		synchronized(state){
			working = !state.winner && state.running;
			if(onGame && !working){
				if(!state.winner){
					System.out.println("Client game pausado por falta de jugadores");
					pong.showPauseMessage("Esperando jugadores ...");
				}
				onGame = false;
			}
		}
	}

	@Override
	public void work() {
		int playerNum = pong.client.getPlayerNum();
		pong.handleKeyEvents(playerNum);
		pong.doGameIteration(state);
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