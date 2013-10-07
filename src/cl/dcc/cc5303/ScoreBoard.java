package cl.dcc.cc5303;


import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;


public class ScoreBoard extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9079400188590075139L;
	//variables
	private int p1_score;
	private int p2_score;
	private int p3_score;
	private int p4_score;
	
	//final score
	private final int WINNING_SCORE = 10;
	
	//determine the winners
	private boolean p1_wins = false;
	private boolean p2_wins = false;
	private boolean p3_wins = false;
	private boolean p4_wins = false;
	
	private JLabel score1, score2, score3, score4;
	
	//constructor
	public ScoreBoard(){
		p1_score = 0;
		p2_score = 0;
		p3_score = 0;
		p4_score = 0;
		
		score1 = new JLabel();
		score1.setText("P1: " + p1_score);
		score1.setFont(new Font("Monospaced",Font.BOLD,25));
		score1.setForeground(Color.WHITE);
		
		score2 = new JLabel();
		score2.setText("P2: " + p2_score);
		score2.setFont(new Font("Monospaced",Font.BOLD,25));
		score2.setForeground(Color.WHITE);
		
		score3 = new JLabel();
		score3.setText("P3: " + p3_score);
		score3.setFont(new Font("Monospaced",Font.BOLD,25));
		score3.setForeground(Color.WHITE);
		
		score4 = new JLabel();
		score4.setText("P4: " + p4_score);
		score4.setFont(new Font("Monospaced",Font.BOLD,25));
		score4.setForeground(Color.WHITE);
		
		
		this.setOpaque(false);
		this.add(score1);
		this.add(score2);
		this.add(score3);
		this.add(score4);
	}
	
	public void pointP1(){
		p1_score++;
		//if player 1 score enough points to win
		if(p1_score == WINNING_SCORE){
			p1_wins = true;
		}
		score1.setText("P1: " + p1_score);
	}
	
	public void pointP2(){
		p2_score++;
		//if player 2 scores enough points to win
		if(p2_score == WINNING_SCORE){
			p2_wins = true;
		}
		score2.setText("P2: " + p2_score);
	}
	
	public void pointP3(){
		p3_score++;
		//if player 3 score enough points to win
		if(p3_score == WINNING_SCORE){
			p3_wins = true;
		}
		score3.setText("P3: " + p3_score);
	}
	
	public void pointP4(){
		p4_score++;
		//if player 4 scores enough points to win
		if(p4_score == WINNING_SCORE){
			p4_wins = true;
		}
		score4.setText("P4: " + p4_score);
	}
	
	
	
	public int getScoreP1(){
		return p1_score;
	}
	
	public int getScoreP2(){
		return p2_score;
	}
	
	public int getScoreP3(){
		return p3_score;
	}
	
	public int getScoreP4(){
		return p4_score;
	}
	
	
	public boolean p1Wins(){
		return p1_wins;
	}
	
	public boolean p2Wins(){
		return p2_wins;
	}
	
	public boolean p3Wins(){
		return p3_wins;
	}
	
	public boolean p4Wins(){
		return p4_wins;
	}
}
