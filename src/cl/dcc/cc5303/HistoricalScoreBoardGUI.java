package cl.dcc.cc5303;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class HistoricalScoreBoardGUI extends JPanel implements HistoricalScoreBoard{
	private static final long serialVersionUID = 8916186821186929334L;
	private JLabel[] labels;
	private JLabel myLabel;
	private HistoricalScoreBoardSimple board;
	private int myPlayerNum;
	
	public HistoricalScoreBoardGUI(int playerNum){
		myPlayerNum = playerNum + 1;
		board = new HistoricalScoreBoardSimple();
		labels = new JLabel[4];
		myLabel = new JLabel();

		this.setBackground(Color.DARK_GRAY);
		this.setOpaque(true);
		this.add(myLabel);
		
		for(int i = 0; i < PongClient.MAX_PLAYERS; i++){
			labels[i] = new JLabel();
			labels[i].setFont(new Font("Monospaced", Font.BOLD, 15));
			labels[i].setBorder(new EmptyBorder(0, 20, 0, 20));
			labels[i].setForeground(Color.WHITE);
			this.add(labels[i]);
		}
		
		myLabel.setFont(new Font("Monospaced", Font.BOLD, 15));
		myLabel.setBorder(new EmptyBorder(0, 20, 0, 20));
		myLabel.setForeground(Color.RED);
		
		showScores();
	}

	@Override
	public void addWinner(int winner) {
		board.addWinner(winner);
	}

	@Override
	public void removePlayer(int player) {
		board.removePlayer(player);
	}

	public void showScores() {
		int[] scores = board.getScores();
				
		for(int i = 0; i < PongClient.MAX_PLAYERS; i++){
			if(myPlayerNum != (i+1)){
				labels[i].setText("P" + (i +1) + ": " + scores[i]);
			}
			else{
				labels[i].setText("");
			}
		}
		
		int myScore = board.getPlayerScore(myPlayerNum);
		myLabel.setText("N° victorias: " +  + myScore + "      ->" );
	}

	@Override
	public int[] getScores() {
		return board.getScores();
	}
	
}
