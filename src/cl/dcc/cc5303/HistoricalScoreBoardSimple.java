package cl.dcc.cc5303;

import java.util.LinkedHashMap;

public class HistoricalScoreBoardSimple implements HistoricalScoreBoard{
	LinkedHashMap<Integer, Integer> playersHistorical;
	
	public HistoricalScoreBoardSimple(){
		playersHistorical = new LinkedHashMap<Integer,Integer>();
	}

	@Override
	public void addWinner(int winner) {
		Object winsForPlayer = playersHistorical.get(winner);
		if( winsForPlayer == null){
			playersHistorical.put(winner, 1);
		}
		else{
			int wins = (Integer)winsForPlayer;
			playersHistorical.put(winner, ++wins);
		}
	}

	@Override
	public void removePlayer(int player) {
		playersHistorical.remove(player);
	}

	public int[] getScores(){
		Object playerScore;
		int[] scores = new int[Pong.MAX_PLAYERS];
		
		for(int i = 1; i <= Pong.MAX_PLAYERS; i++){
			playerScore = playersHistorical.get(i-1);
			if(playerScore != null){
				scores[i-1] = (int)playerScore;
			}
			else{
				scores[i-1] = 0;
			}
		}
		return scores;
	}
	
	public int getPlayerScore(int player){
		Object score = playersHistorical.get(player - 1);
		if(score == null){
			return 0;
		}
		else{
			return (int) score;
		}
	}

}
