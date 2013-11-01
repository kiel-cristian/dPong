package cl.dcc.cc5303;

import java.util.LinkedHashMap;
import java.util.Map;

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

	public String getScores(){
		String score = "Resumen : ";
		for(Map.Entry<Integer, Integer> w : playersHistorical.entrySet()){
			score += "P" + w.getKey() + ": " + w.getValue() + " ";
		}
		return score;
	}

}
