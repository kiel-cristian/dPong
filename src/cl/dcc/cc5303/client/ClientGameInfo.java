package cl.dcc.cc5303.client;

import java.io.Serializable;

public class ClientGameInfo implements Serializable {
	private static final long serialVersionUID = -583134786708400086L;
	public int playerNum;
	public int matchID;
	
	public ClientGameInfo(int matchID, int playerNum) {
		this.matchID = matchID;
		this.playerNum = playerNum;
	}
}
