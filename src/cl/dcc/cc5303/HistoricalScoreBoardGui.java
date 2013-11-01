package cl.dcc.cc5303;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class HistoricalScoreBoardGui extends JPanel implements HistoricalScoreBoard{
	private static final long serialVersionUID = 8916186821186929334L;
	private JLabel label;
	private HistoricalScoreBoardSimple board;
	
	public HistoricalScoreBoardGui(){
		board = new HistoricalScoreBoardSimple();
		label = new JLabel();
		this.setBackground(Color.DARK_GRAY);
		label.setFont(new Font("Monospaced", Font.BOLD, 15));
		label.setBorder(new EmptyBorder(0, 50, 0, 200));
		label.setForeground(Color.WHITE);
		label.setText("");
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
		label.setText(board.getScores());
	}

	public void hideScores() {
		label.setText("");
	}
	
}
