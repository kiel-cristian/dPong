package cl.dcc.cc5303;

import java.io.Serializable;

public class GameInfo implements Serializable {
	private static final long serialVersionUID = -583134786708400086L;
	public int playerNum;
	public int matchID;
	
	public GameInfo(int matchID, int playerNum) {
		this.matchID = matchID;
		this.playerNum = playerNum;
	}
}
