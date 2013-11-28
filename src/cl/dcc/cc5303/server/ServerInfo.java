package cl.dcc.cc5303.server;

import java.io.Serializable;

public class ServerInfo implements Serializable {
	private static final long serialVersionUID = 1484395313255285563L;
	public int matches;
	public int inmigrations;
	public int serverID;
	
	public ServerInfo(int matches, int inmigrations, int serverID){
		this.matches = matches;
		this.inmigrations = inmigrations;
		this.serverID     = serverID;
	}
}
