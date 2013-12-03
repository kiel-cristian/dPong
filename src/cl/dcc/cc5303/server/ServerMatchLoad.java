package cl.dcc.cc5303.server;

import cl.dcc.cc5303.Utils.Pair;

public class ServerMatchLoad extends Pair<Integer, String> implements Comparable<ServerMatchLoad> {

	public ServerMatchLoad(Integer load, String matchID) {
		super(load, matchID);
	}

	@Override
	public int compareTo(ServerMatchLoad s) {
		return this.left().compareTo(s.left());
	}
}