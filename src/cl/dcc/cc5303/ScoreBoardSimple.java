package cl.dcc.cc5303;

public class ScoreBoardSimple implements ScoreBoard {
	
	//final score
	public final int WINNING_SCORE = 10;
	
	//variables
	private int scores[];
	private int winner;
	
	//constructor
	public ScoreBoardSimple(){
		scores = new int[4];
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
}
