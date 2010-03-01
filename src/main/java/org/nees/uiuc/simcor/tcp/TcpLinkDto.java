package org.nees.uiuc.simcor.tcp;

import java.net.InetAddress;
import java.net.Socket;

public class TcpLinkDto {
	private static int idCount = 0;
	private int id;
	private String remoteHost;
	private Socket socket;
	public TcpLinkDto() {
		super();
		id = ++idCount;
	}
	
	public TcpLinkDto(TcpLinkDto dto) {
		super();
		remoteHost = dto.remoteHost;
		socket = dto.socket;
		id = dto.id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof TcpLinkDto) {
			return ((TcpLinkDto)obj).id == id;
		}
		return super.equals(obj);
	}

	public void extractRemoteHost() {
		InetAddress address = socket.getInetAddress();
		remoteHost = address.getCanonicalHostName();
	}
	public String getRemoteHost() {
		return remoteHost;
	}
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}
}
