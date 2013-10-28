package cl.dcc.cc5303;

import java.util.List;
import java.util.ArrayList;

public class ScoreBoardSimple implements ScoreBoard {
	//historical winners
	List<Integer> winners = new ArrayList<Integer>();
	
	//final score
	public final int WINNING_SCORE = 3;
	
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
	
	public void sumPoint(int playerNum) {
		scores[playerNum]++;
		if (scores[playerNum] == WINNING_SCORE)
			winner = playerNum;
	}
	
	public int getScore(int playerNum){
		return scores[playerNum];
	}

	@Override
	public int[] getScores() {
		return scores;
	}

	@Override
	public void setScores(int[] scores) {
		this.scores = scores;
	}

	@Override
	public int getWinner() {
		return winner;
	}

	@Override
	public void reset() {
		winners.add(winner);
		scores = new int[4];
		winner = -1;
	}
}
