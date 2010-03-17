package org.nees.uiuc.simcor.transaction;

import org.nees.uiuc.simcor.listener.ClientId;
import org.nees.uiuc.simcor.tcp.TcpError;
import org.nees.uiuc.simcor.transaction.SimCorMsg.MsgType;

public class TriggerResponse extends SimCorMsg {

	private ClientId remoteId;
	protected TcpError error;

	public TriggerResponse(Msg2Tcp msg) {
		super(msg.getMsg());
	}

	public synchronized ClientId getRemoteId() {
		return remoteId;
	}

	public synchronized void setRemoteId(ClientId cid) {
		this.remoteId = cid;
	}

	public TcpError getError() {
		return error;
	}

	public void setError(TcpError error) {
		this.error = error;
	}
	@Override
	public String toString() {
		String result = "/client=" + getRemoteId();
		result += super.toString();
		return result;
	}
}
