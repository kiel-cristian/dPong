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
	
	public ClientUpdateThread(Client client){
		self = client;
		onGame = true;
	}

	@Override
	public void preWork() throws InterruptedException {
		try {
			synchronized(self.server){
				temporalState = self.server.updatePositions(self.info.matchID, self.info.playerNum, self.getBarPosition());
			}
			if(temporalState == null){
				System.out.println("Deteniendo servicio update");
				end();
				return;
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
		} catch (RemoteException e) {
			System.out.println("Server no responde");
			System.exit(1); // TODO
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