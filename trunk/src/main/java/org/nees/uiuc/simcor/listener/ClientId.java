package org.nees.uiuc.simcor.listener;

import org.nees.uiuc.simcor.tcp.Connection;

public class ClientId {
	public final Connection connection;
	public final String system;
	public final String remoteHost;

	public ClientId(Connection connection, String system,
			String remoteHost) {
		this.system = system;
		this.remoteHost = remoteHost;
		this.connection = connection;
	}
}
