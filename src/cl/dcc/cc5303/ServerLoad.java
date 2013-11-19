package cl.dcc.cc5303;

import cl.dcc.cc5303.Utils.Pair;

public class ServerLoad extends Pair<Integer, Integer> implements Comparable<ServerLoad> {

	public ServerLoad(Integer load, Integer serverID) {
		super(load, serverID);
	}

	@Override
	public int compareTo(ServerLoad s) {
		return this.left().compareTo(s.left());
	}
}