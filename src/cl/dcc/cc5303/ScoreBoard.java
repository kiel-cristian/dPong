package cl.dcc.cc5303;

public interface ScoreBoard {
	public void sumPoint(int playerNum);
	public int[] getScores();
	public void setScores(int scores[]);
	public int getWinner();
	public void reset();
}