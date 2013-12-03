package cl.dcc.cc5303.client;

import java.rmi.RemoteException;

import cl.dcc.cc5303.GameStateInfo;
import cl.dcc.cc5303.HistoricalScoreBoardGUI;
import cl.dcc.cc5303.PongThread;
import cl.dcc.cc5303.ScoreBoardGUI;

public class ClientUpdateThread extends PongThread{
	private Client self;
	private GameStateInfo temporalState;
	public boolean onGame;
	public boolean onMigration;

	public ClientUpdateThread(Client client){
		this.self = client;
		this.onGame = true;
		this.temporalState = null;
		this.onMigration = false;
	}

	public void startMigration(){
		this.onMigration = true;
	}
	public void stopMigration(){
		this.onMigration = false;
	}

	@Override
	public void preWork() throws InterruptedException {
		synchronized(self.server){
			try {
				if(!onMigration){
					temporalState = self.server.updatePositions(self.info.matchID, self.info.playerNum, self.getBarPosition());
				}
			} catch (RemoteException e) {
				System.out.println("Error en server al actualizar cliente");
				e.printStackTrace();
				System.exit(1); // TODO
			}
		}

		working = !temporalState.winner && temporalState.running;
		if (working) self.userUnPaused();
		if(!working && onGame){
			if (temporalState.userPaused) {
				self.userPaused();
				System.out.println("Un jugador ha pausado el juego");
			}
			else if(!temporalState.winner){
				System.out.println("Client update pausado por falta de jugadores");
			}
			else{
				((ScoreBoardGUI) self.pong.scores).setWinner(temporalState.scores, temporalState.winnerPlayer, temporalState.playing);
				((HistoricalScoreBoardGUI)self.pong.historical).setScores(temporalState.historicalScores);
				self.pong.showWinner();
			}
			onGame = false;
			work();
		}
	}

	@Override
	public void work() throws InterruptedException {
		synchronized(self.state()){
			self.state().updateFromInfo(temporalState);
			self.pong.scores.setScores(temporalState.scores, temporalState.playing);
		}
	}

	@Override
	public void postWork() throws InterruptedException {
		if(working){
			//System.out.println("Nuevo match");
			//self.pong.game.reMatch();
			onGame = true;
		}
	}

	@Override
	public void pauseWork() throws InterruptedException {
		Thread.sleep(PongThread.UPDATE_RATE/ 60);
	}

}