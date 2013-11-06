package cl.dcc.cc5303;

import cl.dcc.cc5303.Utils.Pair;

public class ServerLoad extends Pair<Integer, IServer> implements Comparable<ServerLoad> {

	public ServerLoad(Integer load, IServer server) {
		super(load, server);
	}

	@Override
	public int compareTo(ServerLoad s) {
		return this.left().compareTo(s.left());
	}
}