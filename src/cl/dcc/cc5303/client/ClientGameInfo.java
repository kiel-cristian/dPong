package cl.dcc.cc5303.client;

import java.io.Serializable;

public class ClientGameInfo implements Serializable {
	private static final long serialVersionUID = -583134786708400086L;
	private static int gClientID = 0;
	
	public int playerNum;
	public int serverID;
	public String matchID;
	public int clientID;
	
	public ClientGameInfo(int serverID, String match, int playerNum) {
		this.serverID = serverID;
		this.matchID = match;
		this.playerNum = playerNum;
		this.clientID  = ClientGameInfo.nextID();
	}
	
	private static int nextID(){
		return ClientGameInfo.gClientID++;
	}
}
