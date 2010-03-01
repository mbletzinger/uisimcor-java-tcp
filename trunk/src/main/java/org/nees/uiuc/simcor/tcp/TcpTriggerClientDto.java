package org.nees.uiuc.simcor.tcp;

public class TcpTriggerClientDto extends TcpActionsDto {

	private String remoteHost;

	public TcpTriggerClientDto() {
		super();
	}
	public TcpTriggerClientDto(TcpActionsDto tdto) {
		super(tdto);
	}

	public TcpTriggerClientDto(TcpTriggerClientDto tdto) {
		super(tdto);
		remoteHost = tdto.remoteHost;
	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

}
