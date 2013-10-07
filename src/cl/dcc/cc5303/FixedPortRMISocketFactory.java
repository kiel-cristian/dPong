package cl.dcc.cc5303;

import java.rmi.server.RMISocketFactory;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;

public class FixedPortRMISocketFactory extends RMISocketFactory {

	public Socket createSocket(String host, int port) throws IOException {
		System.out.println("creating socket to host : " + host + " on port "
				+ port);
		return new Socket(host, port);
	}

	public ServerSocket createServerSocket(int port) throws IOException {
		port = (port == 0 ? 1111 : port);
		System.out.println("creating ServerSocket on port " + port);
		return new ServerSocket(port);

	}
}