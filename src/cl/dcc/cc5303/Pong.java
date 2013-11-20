package cl.dcc.cc5303;

import cl.dcc.cc5303.client.ClientPong;

public class Pong implements PongI{
	public final static int WIDTH = 640, HEIGHT = 480;
	public final static int DX = 5;
	public final static double DV = 0.3;
	public final static int MAX_PLAYERS = 2;
	public final static int WINNING_SCORE = 2;
	public ScoreBoard scores;
	public HistoricalScoreBoard historical;
	protected GameState temporalState;
	
	public Pong(){
		scores = new ScoreBoardSimple();
		historical = new HistoricalScoreBoardSimple();
	}

	public GameState doGameIteration(GameState state) {
		temporalState = state;
		for (int i = 0; i < 4;  i++){
			if(temporalState.isPlaying(i)){
				handleHumanBounce(i);
			}
			else{
				handleBounce(i);
			}
		}
		handleBall();
		return temporalState;
	}
	
	public void handleHumanBounce(int i){
		double step = 1+DV;
	    switch(i){
	    	// jugador a la izquierda
	    	case(0):{
	    		if(temporalState.ball.willCrashWithBarByRight(temporalState.bars[i], step)){
	    			temporalState.ball.changeXDir(step);
	    			temporalState.setLastPlayer(0);
	    		}
	    	}
	    	// jugador a la derecha
	    	case(1):{ 
	    		if(temporalState.ball.willCrashWithBarByLeft(temporalState.bars[i], step)){
	    			temporalState.ball.changeXDir(step);
	    			temporalState.setLastPlayer(1);
	    		}
	    	}
	    	//jugador inferior
	    	case(2):{
	    		if(temporalState.ball.willCrashWithBarByTop(temporalState.bars[i], step)){
	    			temporalState.ball.changeYDir(step);
	    			temporalState.setLastPlayer(2);
	    		}
	    	}
	    	// jugador superior
	    	case(3):{
	    		if(temporalState.ball.willCrashWithBarByBottom(temporalState.bars[i], step)){
	    			temporalState.ball.changeYDir(step);
	    			temporalState.setLastPlayer(3);
	    		}
	    	}
	    }
	}
	
	public void handleBounce(int i){
		switch(i){
			// izquierda
			case(0):{
				if(temporalState.ball.checkIfGoesLeft(0, DX))
					temporalState.ball.changeXDir(1);
			} break;
			// derecha
			case(1):{
				if(temporalState.ball.checkIfGoesRight(WIDTH, DX)){
					temporalState.ball.changeXDir(1);
				}
			} break;
			// abajo
			case(2):{
				if(temporalState.ball.checkIfGoesDown(HEIGHT, DX)){
					temporalState.ball.changeYDir(1);
				}

			} break;
			// arriba
			case(3):{
				if(temporalState.ball.checkIfGoesUp(0, DX))
					temporalState.ball.changeYDir(1);
			}
		}
	}
	
	public void handleBall(){
		// Actualiza posicion
		temporalState.ball.move(temporalState.ball.vx * DX, temporalState.ball.vy * DX);
		if(temporalState.ball.x > ClientPong.WIDTH || temporalState.ball.x < 0 || temporalState.ball.y < 0 || temporalState.ball.y > ClientPong.HEIGHT){
			switch(temporalState.lastPlayer){
				case(0):{
					// Punto para jugador 1 si no sale por la izquierda
					if(!(temporalState.ball.x < 0) && temporalState.isPlaying(0)){
						scores.sumPoint(0, temporalState.playing);
					}
				} break;
				case(1):{
					// Punto para jugador 2 si no sale por la derecha
					if( !(temporalState.ball.x > ClientPong.WIDTH) && temporalState.isPlaying(1)){
						scores.sumPoint(1, temporalState.playing);
					}
				} break;
				case(2):{
					// Punto para jugador 3 si no sale abajo
					if( !(temporalState.ball.y > ClientPong.HEIGHT) && temporalState.isPlaying(2)){
						scores.sumPoint(2, temporalState.playing);
					}
				}break;
				case(3):{
					// Punto para jugador 4 si no sale arriba
					if( !(temporalState.ball.y < 0) && temporalState.isPlaying(3)){
						scores.sumPoint(3, temporalState.playing);
					}
				}
			}
			if(scores.isAWinner() && temporalState.lastPlayer != -1){
				historical.addWinner(scores.getWinner());
				temporalState.winner = true;
				temporalState.winnerPlayer = scores.getWinner();
			}
			temporalState.setLastPlayer(-1);
			temporalState.ball.reset();
		}
	}

}
