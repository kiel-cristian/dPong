package cl.dcc.cc5303;

public class ScoreBoardSimple implements ScoreBoard {
	
	//final score
	public final int WINNING_SCORE = 10;
	
	//variables
	private int score[];
	private int winner;
	
	//constructor
	public ScoreBoardSimple(){
		score = new int[4];
	}
	
	public void sumPoint(int playerNum) {
		score[playerNum]++;
		if (score[playerNum] == WINNING_SCORE)
			winner = playerNum;
	}
	
	public int getScoreP1(){
		return score[0];
	}
	
	public int getScoreP2(){
		return score[1];
	}
	
	public int getScoreP3(){
		return score[2];
	}
	
	public int getScoreP4(){
		return score[3];
	}
	
	
	public boolean p1Wins(){
		return winner == 0;
	}
	
	public boolean p2Wins(){
		return winner == 1;
	}
	
	public boolean p3Wins(){
		return winner == 2;
	}
	
	public boolean p4Wins(){
		return winner == 3;
	}
}
