package cl.dcc.cc5303;

import cl.dcc.cc5303.client.ClientPong;

public class ScoreBoardSimple implements ScoreBoard {
	//variables
	private int scores[];
	private int winner;
	
	//constructor
	public ScoreBoardSimple(){
		scores = new int[4];
		winner = -1;
	}
	
	public void setWinner(int winner){
		this.winner = winner;
	}
	
	public void sumPoint(int playerNum, boolean[] playing) {
		if(playing[playerNum]){
			scores[playerNum]++;
			if (scores[playerNum] == ClientPong.WINNING_SCORE){
				winner = playerNum;
			}
		}
	}
	
	public int getScore(int playerNum){
		return scores[playerNum];
	}

	@Override
	public int[] getScores() {
		return scores;
	}

	@Override
	public void setScores(int[] scores, boolean[] playing) {
		for(int i = 0; i < ClientPong.MAX_PLAYERS; i++){
			if(playing[i]){
				this.scores[i] = scores[i];
			}
		}
	}

	@Override
	public int getWinner() {
		return winner;
	}
	
	@Override
	public boolean isAWinner() {
		return winner >= 0;
	}

	@Override
	public void reset(boolean[] playing) {
		if(winner != -1 && playing[winner]){
			scores = new int[4];
			winner = -1;
		}
	}
}
