package cl.dcc.cc5303;

public interface HistoricalScoreBoard {
	public void addWinner(int winner);
	public void removePlayer(int player);
	public int[] getScores();
}
