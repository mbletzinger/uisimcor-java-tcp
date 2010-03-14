package org.nees.uiuc.simcor.listener;

import org.nees.uiuc.simcor.tcp.Connection;

public class ClientIdWithConnection {
	public final Connection connection;

	public final String remoteHost;

	public final String system;
	public ClientIdWithConnection(Connection connection, String system,
			String remoteHost) {
		this.system = system;
		this.remoteHost = remoteHost;
		this.connection = connection;
	}
	@Override
	public int hashCode() {
		int result = 0;
		if(system != null) {
			result += system.hashCode();
		}
		if(remoteHost != null) {
			result += remoteHost.hashCode();
		}
		return result;
	}

	@Override
	public String toString() {
		String result = "";
		if(system != null) {
			result += system;
		} else {
			result += "null";
		}
		if(remoteHost != null) {
			result += " at " + remoteHost.hashCode();
		} else {
			result += " at null";
		}
		return result;
	}
}
