package org.nees.uiuc.simcor.listener;

import org.nees.uiuc.simcor.tcp.Connection;

public class ClientConnection {
	public final Connection connection;
	public final String system;
	public final String remoteHost;

	public ClientConnection(Connection connection, String system,
			String remoteHost) {
		this.system = system;
		this.remoteHost = remoteHost;
		this.connection = connection;
	}
}
