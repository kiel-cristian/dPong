package cl.dcc.cc5303.server;

import java.io.Serializable;

import cl.dcc.cc5303.GameStateInfo;

public class ServerMatchMigrationInfo implements Serializable{
	private static final long serialVersionUID = 5610712579073974689L;
	public GameStateInfo state;
	public String matchID;
	
	public ServerMatchMigrationInfo(GameStateInfo state, String matchID){
		this.state = state;
		this.matchID = matchID;
	}

}
