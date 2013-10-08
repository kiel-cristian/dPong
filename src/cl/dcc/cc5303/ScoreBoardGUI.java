package cl.dcc.cc5303;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class ScoreBoardGUI extends JPanel implements ScoreBoard {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9079400188590075139L;
	
	private ScoreBoardSimple score;
	private JLabel score1, score2, score3, score4;
	
	//constructor
	public ScoreBoardGUI() {
		score = new ScoreBoardSimple();
		
		score1 = new JLabel();
		score1.setText("P1: " + score.getScore(0));
		score1.setFont(new Font("Monospaced",Font.BOLD,25));
		score1.setForeground(Color.WHITE);
		
		score2 = new JLabel();
		score2.setText("P2: " + score.getScore(1));
		score2.setFont(new Font("Monospaced",Font.BOLD,25));
		score2.setForeground(Color.WHITE);
		
		score3 = new JLabel();
		score3.setText("P3: " + score.getScore(2));
		score3.setFont(new Font("Monospaced",Font.BOLD,25));
		score3.setForeground(Color.WHITE);
		
		score4 = new JLabel();
		score4.setText("P4: " + score.getScore(3));
		score4.setFont(new Font("Monospaced",Font.BOLD,25));
		score4.setForeground(Color.WHITE);	
		
		this.setOpaque(false);
		this.add(score1);
		this.add(score2);
		this.add(score3);
		this.add(score4);
	}
	
	public void updateScores() {
		score1.setText("P1: " + score.getScore(0));
		score2.setText("P2: " + score.getScore(1));
		score3.setText("P3: " + score.getScore(2));
		score4.setText("P4: " + score.getScore(3));
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
		score1.setText("");
		score2.setText("");
		score3.setText("");
		score4.setText("");

		int winner = score.getWinner();
		System.out.println("GANADOR!!!" + score.getWinner());
		
		switch(winner){
			case(0):{
				score1.setText("JUGADOR 1 A GANADO!");
			}
			case(1):{
				score2.setText("JUGADOR 2 A GANADO!");
			}
			case(2):{
				score3.setText("JUGADOR 3 A GANADO!");
			}
			case(3):{
				score4.setText("JUGADOR 4 A GANADO!");
				
			}
		}
		
	}

	@Override
	public void reset() {
		score1.setText("P1: " + score.getScore(0));
		score1.setFont(new Font("Monospaced",Font.BOLD,25));
		score1.setForeground(Color.WHITE);
		
		score2.setText("P2: " + score.getScore(1));
		score2.setFont(new Font("Monospaced",Font.BOLD,25));
		score2.setForeground(Color.WHITE);
		
		score3.setText("P3: " + score.getScore(2));
		score3.setFont(new Font("Monospaced",Font.BOLD,25));
		score3.setForeground(Color.WHITE);
		
		score4.setText("P4: " + score.getScore(3));
		score4.setFont(new Font("Monospaced",Font.BOLD,25));
		score4.setForeground(Color.WHITE);
		
		score.reset();
		
	}
}
