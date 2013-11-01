package cl.dcc.cc5303;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class ScoreBoardGUI extends JPanel implements ScoreBoard {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9079400188590075139L;
	
	private ScoreBoardSimple score;
	private JLabel scores[];
	
	public ScoreBoardGUI() {
		score = new ScoreBoardSimple();
		scores = new JLabel[4];
		
		this.setBackground(Color.DARK_GRAY);
		initScores();
		for (int i=0; i<4; i++) add(scores[i]);
		this.setOpaque(true);
	}
	
	private void initScores() {
		for (int i=0; i<4; i++) {
			scores[i] = new JLabel();
			scores[i].setText("P" + (i+1) + ": " + score.getScore(i));
			scores[i].setFont(new Font("Monospaced", Font.BOLD, 15));
			scores[i].setBorder(new EmptyBorder(0, 20, 0, 20));
			scores[i].setForeground(Color.WHITE);
		}
	}
	
	public void updateScores() {
		for (int i=0; i<4; i++) {
			scores[i].setText("P" + (i+1) + ": " + score.getScore(i));
		}
	}
	
	@Override
	public void sumPoint(int playerNum) {
		score.sumPoint(playerNum);
		updateScores();
	}

	@Override
	public int[] getScores() {
		return score.getScores();
	}

	@Override
	public void setScores(int[] scores) {
		score.setScores(scores);
		if(score.getWinner() <0)
			updateScores();
	}
	
	public void setWinner(int[] scores, int winner){
		score.setScores(scores);
		score.setWinner(winner);
	}

	@Override
	public int getWinner() {
		return score.getWinner();
	}

	public void showWinner() {
		for (int i=0; i<4; i++) {
			scores[i].setText("");
		}
		scores[0].setText("JUGADOR " + (score.getWinner() + 1) + " HA GANADO!");		
	}

	@Override
	public void reset() {
		initScores();
		score.reset();
	}
}
