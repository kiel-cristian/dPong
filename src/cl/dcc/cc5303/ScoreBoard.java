package cl.dcc.cc5303;

public interface ScoreBoard {
	public void sumPoint(int playerNum, boolean[] playing);
	public int[] getScores();
	public void setScores(int scores[], boolean[] playing);
	public int getWinner();
	public void reset(boolean[] playing);
	public boolean isAWinner();
}