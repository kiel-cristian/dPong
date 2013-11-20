package cl.dcc.cc5303.server;

public class ServerInfo {
	public int matches;
	public int inmigrations;
	public int serverID;
	
	public ServerInfo(int matches, int inmigrations, int serverID){
		this.matches = matches;
		this.inmigrations = inmigrations;
		this.serverID     = serverID;
	}
}
