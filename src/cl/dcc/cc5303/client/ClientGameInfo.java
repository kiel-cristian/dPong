package cl.dcc.cc5303.client;

import java.io.Serializable;

public class ClientGameInfo implements Serializable {
	private static final long serialVersionUID = -583134786708400086L;
	public int playerNum;
	public String matchID;
	
	public ClientGameInfo(String string, int playerNum) {
		this.matchID = string;
		this.playerNum = playerNum;
	}
}
