package cl.dcc.cc5303;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class ScoreBoardGUI extends JPanel implements ScoreBoard {
	private static final long serialVersionUID = -9079400188590075139L;
	
	private ScoreBoardSimple score;
	private JLabel scores[];
	private JLabel message;
	private int myPlayerNum;
	
	public ScoreBoardGUI(boolean[] playing, int playerNum) {
		score = new ScoreBoardSimple();
		scores = new JLabel[4];
		message = new JLabel();
		
		this.setBackground(Color.DARK_GRAY);
		initScores(playing);
		add(message);
		for (int i=0; i<Pong.MAX_PLAYERS; i++){
			add(scores[i]);
		}
		this.setOpaque(true);
		this.myPlayerNum = playerNum;
	}
	
	private void initScores(boolean[] playing) {
		for (int i=0; i<4; i++) {
			scores[i] = new JLabel();
			scores[i].setText("P" + (i+1) + ": " + score.getScore(i));
			scores[i].setFont(new Font("Monospaced", Font.BOLD, 15));
			scores[i].setBorder(new EmptyBorder(0, 20, 0, 20));
			
			if(!playing[i]){
				scores[i].setForeground(Color.DARK_GRAY);
				continue;
			}
			if(i == myPlayerNum){
				scores[i].setForeground(Color.RED);
			}
			else{
				scores[i].setForeground(Color.WHITE);
			}
		}
		message.setText("Esperando jugadores ...");
		message.setFont(new Font("Monospaced", Font.BOLD, 15));
		message.setBorder(new EmptyBorder(0, 20, 0, 20));
		message.setForeground(Color.WHITE);
	}
	
	private void resetScores(){
		for (int i=0; i<4; i++) {
			scores[i].setText("P" + (i+1) + ": " + score.getScore(i));
		}
		message.setText("");
	}
	
	public void updateScores(boolean[] playing) {
		for (int i=0; i<4; i++) {
			if(!playing[i]){
				scores[i].setBackground(Color.DARK_GRAY);
				scores[i].setForeground(Color.DARK_GRAY);
			}
			else{
				if(myPlayerNum == i){
					scores[i].setForeground(Color.RED);
				}
				else{
					scores[i].setForeground(Color.WHITE);
				}
				scores[i].setText("P" + (i+1) + ": " + score.getScore(i));
			}
		}
	}
	
	@Override
	public void sumPoint(int playerNum, boolean[] playing) {
		score.sumPoint(playerNum, playing);
		updateScores(playing);
	}

	@Override
	public int[] getScores() {
		return score.getScores();
	}

	@Override
	public void setScores(int[] scores, boolean[] playing) {
		score.setScores(scores, playing);
		if(score.getWinner() < 0){
			updateScores(playing);
		}
	}
	
	public void setWinner(int[] scores, int winner, boolean[] playing){
		System.out.println("winner: " + winner);
		score.setScores(scores, playing);
		score.setWinner(winner);
	}

	@Override
	public int getWinner() {
		return score.getWinner();
	}
	
	@Override
	public boolean isAWinner(){
		return getWinner()>= 0;
	}
	
	@Override
	public void reset(boolean[] playing) {
		resetScores();
		score.reset(playing);
	}

	public void showWinner() {
		for (int i=0; i< Pong.MAX_PLAYERS; i++) {
			scores[i].setText("");
		}
		int winner = score.getWinner();
		if(winner == myPlayerNum){
			message.setForeground(Color.RED);
			message.setText("HAS GANADO LA PARTIDA!");
		}
		else{
			message.setForeground(Color.WHITE);
			message.setText("JUGADOR " + (score.getWinner() + 1) + " HA GANADO!");
		}
	}
	
	public void showPause(String m){
		message.setText(m);
		message.setForeground(Color.WHITE);
	}
}
